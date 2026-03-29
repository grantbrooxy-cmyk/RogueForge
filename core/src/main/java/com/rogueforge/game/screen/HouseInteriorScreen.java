package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight interior map for houses, NPC conversation, and chest looting.
 */
public class HouseInteriorScreen implements Screen {
    private static final float PLAYER_SIZE = 40f;
    private static final float PLAYER_SPEED = 180f;
    private static final float CHEST_SIZE = 32f;
    private static final float NPC_SIZE = 22f;
    private static final float FEATURE_SIZE = 36f;

    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final GameScreen gameScreen;
    private final GameScreen.House house;
    private final OrthographicCamera camera;
    private final OrthographicCamera uiCamera;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Vector2 playerPos = new Vector2(210f, 72f);
    private final Texture floorTile;
    private final Texture wallTile;
    private final Texture doorTexture;
    private final Texture chestTexture;
    private final Texture shadowTexture;
    private final TextureRegion playerSprite;
    private final TextureRegion[] npcSprites;

    private String activeSpeaker;
    private String activeDialog;
    private String lootMessage;
    private float lootTimer;
    private final String openingSpeaker;
    private final String openingDialog;
    private final List<com.rogueforge.game.world.DialogueSystem.DialoguePage> activeDialogueSequence = new ArrayList<>();
    private int activeDialogueSequenceIndex;
    private int dialogPageIndex;
    private String dialogPageTrackingText;
    private String dialogPageTrackingSpeaker;

    public HouseInteriorScreen(RogueForgeGame game, ScreenManager screenManager, GameScreen gameScreen, GameScreen.House house) {
        this(game, screenManager, gameScreen, house, null, null);
    }

    public HouseInteriorScreen(RogueForgeGame game, ScreenManager screenManager, GameScreen gameScreen,
                               GameScreen.House house, String openingSpeaker, String openingDialog) {
        this.game = game;
        this.screenManager = screenManager;
        this.gameScreen = gameScreen;
        this.house = house;
        this.openingSpeaker = openingSpeaker;
        this.openingDialog = openingDialog;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 420f, 300f);
        this.uiCamera = new OrthographicCamera();
        this.uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.15f);
        this.floorTile = loadTexture("2 Dungeon Tileset/1 Tiles/Tile_03.png");
        this.wallTile = loadTexture("2 Dungeon Tileset/1 Tiles/Tile_57.png");
        this.doorTexture = loadTexture("2 Dungeon Tileset/3 Animated objects/Door_S.png");
        this.chestTexture = loadTexture("2 Dungeon Tileset/3 Animated objects/Chest1_S.png");
        this.shadowTexture = loadTexture("1 Characters/Other/Shadow.png");
        this.playerSprite = loadStripFrame("1 Characters/1/D_Idle.png", 32, 32);
        this.npcSprites = new TextureRegion[] {
            loadStripFrame("1 Characters/2/S_Idle.png", 32, 32),
            loadStripFrame("1 Characters/3/S_Idle.png", 32, 32),
            loadStripFrame("1 Characters/1/S_Idle.png", 32, 32)
        };
    }

    private Texture loadTexture(String relativePath) {
        Texture texture = new Texture(Gdx.files.internal(relativePath));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    private TextureRegion loadStripFrame(String relativePath, int frameWidth, int frameHeight) {
        Texture texture = loadTexture(relativePath);
        return new TextureRegion(texture, 0, 0, Math.min(frameWidth, texture.getWidth()), Math.min(frameHeight, texture.getHeight()));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        if (openingDialog != null && !openingDialog.isEmpty()) {
            activeSpeaker = openingSpeaker != null && !openingSpeaker.isEmpty() ? openingSpeaker : house.name;
            activeDialog = openingDialog;
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.18f, 0.15f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        drawInterior();
        drawUi();
    }

    private void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            gameScreen.pauseGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            gameScreen.openWorkshop();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (!advanceDialogPage()) {
                if (!advanceDialogueSequence()) {
                    clearActiveDialog();
                }
            }
            lootMessage = null;
            lootTimer = 0f;
        }

        float dx = 0f;
        float dy = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) dy += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) dy -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += 1f;

        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0f) {
            dx /= len;
            dy /= len;
        }

        playerPos.x = clamp(playerPos.x + dx * PLAYER_SPEED * delta, 36f, 384f);
        playerPos.y = clamp(playerPos.y + dy * PLAYER_SPEED * delta, 48f, 264f);

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (hasActiveDialog()) {
                if (!advanceDialogPage() && !advanceDialogueSequence()) {
                    clearActiveDialog();
                }
                return;
            }
            if (!tryExit()) {
                if (!tryOpenChest()) {
                    if (!tryInteractWithFeature()) {
                        interactWithNpc();
                    }
                }
            }
        }

        if (lootTimer > 0f) {
            lootTimer -= delta;
            if (lootTimer <= 0f) {
                lootMessage = null;
            }
        }
    }

    private boolean tryExit() {
        Vector2 exitDoor = new Vector2(210f, 52f);
        if (playerPos.dst(exitDoor) <= 34f) {
            screenManager.pop();
            return true;
        }
        return false;
    }

    private boolean tryOpenChest() {
        for (GameScreen.Chest chest : house.chests) {
            if (playerPos.dst(chest.pos) <= 34f && !chest.opened) {
                chest.opened = true;
                gameScreen.markChestOpened(house.zoneId, house.id, chest.id);
                if (chest.goldReward > 0) {
                    gameScreen.addGold(chest.goldReward);
                }
                if (chest.potionReward > 0) {
                    gameScreen.addHealingPotions(chest.potionReward);
                }
                gameScreen.onInteriorChestOpened(chest.id);
                activeSpeaker = "Chest";
                activeDialog = "You found " + chest.goldReward + " gold and " + chest.potionReward + " repair kit(s).";
                lootMessage = "Loot secured from " + house.name + ".";
                lootTimer = 2.5f;
                return true;
            }
        }
        return false;
    }

    private void interactWithNpc() {
        GameScreen.InteriorNpc closestNpc = null;
        float nearest = 48f;

        for (GameScreen.InteriorNpc npc : house.interiorNpcs) {
            float dist = playerPos.dst(npc.pos);
            if (dist < nearest) {
                nearest = dist;
                closestNpc = npc;
            }
        }

        if (closestNpc != null) {
            if (closestNpc.shopId != null && !closestNpc.shopId.isEmpty()) {
                if ("forge".equals(closestNpc.shopId)) {
                    screenManager.push(new ForgeScreen(game, screenManager, gameScreen));
                    return;
                }
                screenManager.push(new ShopScreen(game, screenManager, gameScreen, closestNpc.name, gameScreen.createShopInventory(closestNpc.shopId)));
                return;
            }
            com.rogueforge.game.world.DialogueSystem.DialogueResult result =
                gameScreen.interactWithInteriorNpc(closestNpc.id, closestNpc.name);
            showDialogueSequence(result.pages, result.speaker != null ? result.speaker : closestNpc.name,
                result.text != null && !result.text.isEmpty() ? result.text : closestNpc.dialog);
        } else {
            clearActiveDialog();
        }
    }

    private boolean tryInteractWithFeature() {
        GameScreen.InteriorFeature closestFeature = null;
        float nearest = 44f;
        for (GameScreen.InteriorFeature feature : house.interiorFeatures) {
            float dist = playerPos.dst(feature.pos);
            if (dist < nearest) {
                nearest = dist;
                closestFeature = feature;
            }
        }
        if (closestFeature == null) {
            return false;
        }
        if ("forge".equals(closestFeature.actionType)) {
            activeSpeaker = "Forge";
            activeDialog = closestFeature.dialog;
            screenManager.push(new ForgeScreen(game, screenManager, gameScreen));
            return true;
        }
        activeSpeaker = closestFeature.label;
        activeDialog = closestFeature.dialog;
        return true;
    }

    private void drawInterior() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (float x = 20f; x < 400f; x += 24f) {
            for (float y = 20f; y < 270f; y += 24f) {
                batch.draw(floorTile, x, y, 24f, 24f);
            }
        }
        for (float x = 20f; x < 400f; x += 24f) {
            batch.draw(wallTile, x, 20f, 24f, 24f);
            batch.draw(wallTile, x, 246f, 24f, 24f);
        }
        for (float y = 44f; y < 246f; y += 24f) {
            batch.draw(wallTile, 20f, y, 24f, 24f);
            batch.draw(wallTile, 376f, y, 24f, 24f);
        }
        batch.draw(doorTexture, 185f, 20f, 50f, 24f);
        for (GameScreen.Chest chest : house.chests) {
            if (chest.hidden && !chest.opened && playerPos.dst(chest.pos) > 60f) {
                continue;
            }
            batch.setColor(1f, 1f, 1f, chest.opened ? 0.55f : 1f);
            batch.draw(chestTexture, chest.pos.x - CHEST_SIZE / 2f, chest.pos.y - CHEST_SIZE / 2f, CHEST_SIZE, CHEST_SIZE);
        }
        for (GameScreen.InteriorFeature feature : house.interiorFeatures) {
            drawShadow(feature.pos.x, feature.pos.y, 50f, 18f, 0.45f);
            if ("forge".equals(feature.actionType)) {
                batch.setColor(0.88f, 0.46f, 0.18f, 1f);
                batch.draw(wallTile, feature.pos.x - FEATURE_SIZE / 2f, feature.pos.y - FEATURE_SIZE / 2f, FEATURE_SIZE, FEATURE_SIZE);
                batch.setColor(1f, 0.85f, 0.35f, 1f);
                batch.draw(doorTexture, feature.pos.x - 14f, feature.pos.y + 4f, 28f, 14f);
                batch.setColor(Color.WHITE);
            } else {
                batch.setColor(Color.LIGHT_GRAY);
                batch.draw(wallTile, feature.pos.x - FEATURE_SIZE / 2f, feature.pos.y - FEATURE_SIZE / 2f, FEATURE_SIZE, FEATURE_SIZE);
                batch.setColor(Color.WHITE);
            }
        }
        batch.setColor(Color.WHITE);
        int npcIndex = 0;
        for (GameScreen.InteriorNpc npc : house.interiorNpcs) {
            drawShadow(npc.pos.x, npc.pos.y, 40f, 14f, 0.4f);
            batch.draw(npcSprites[npcIndex % npcSprites.length], npc.pos.x - NPC_SIZE, npc.pos.y - NPC_SIZE, 44f, 44f);
            npcIndex++;
        }
        drawShadow(playerPos.x, playerPos.y, 46f, 18f, 0.5f);
        batch.draw(playerSprite, playerPos.x - PLAYER_SIZE / 2f, playerPos.y - PLAYER_SIZE / 2f, PLAYER_SIZE, PLAYER_SIZE);
        font.setColor(Color.WHITE);
        font.draw(batch, house.name, 34f, 286f);
        font.draw(batch, "I: Roster  Esc: Pause", 226f, 286f);
        font.draw(batch, "Exit", 192f, 54f);
        for (GameScreen.InteriorNpc npc : house.interiorNpcs) {
            font.draw(batch, npc.name, npc.pos.x - 18f, npc.pos.y + 30f);
            if (playerPos.dst(npc.pos) <= 48f) {
                String prompt = "E: Talk";
                if (npc.shopId != null) {
                    prompt = "E: Shop";
                }
                font.draw(batch, prompt, npc.pos.x - 22f, npc.pos.y - 24f);
            }
        }
        for (GameScreen.InteriorFeature feature : house.interiorFeatures) {
            font.draw(batch, feature.label, feature.pos.x - 18f, feature.pos.y + 36f);
            if (playerPos.dst(feature.pos) <= 44f) {
                String prompt = "forge".equals(feature.actionType) ? "E: Use Forge" : "E: Interact";
                font.draw(batch, prompt, feature.pos.x - 30f, feature.pos.y - 28f);
            }
        }
        for (GameScreen.Chest chest : house.chests) {
            if (!chest.opened && playerPos.dst(chest.pos) <= 40f) {
                font.draw(batch, "E: Open", chest.pos.x - 20f, chest.pos.y - 24f);
            }
        }
        batch.end();
    }

    private void drawShadow(float x, float y, float width, float height, float alpha) {
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(shadowTexture, x - width / 2f, y - height / 2f - 10f, width, height);
        batch.setColor(Color.WHITE);
    }

    private void drawUi() {
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (activeDialog != null && activeSpeaker != null) {
            resetDialogPageIfNeeded();
            List<String> pages = paginateDialog(activeDialog, 86, 3);
            String currentPage = pages.get(Math.min(dialogPageIndex, pages.size() - 1));
            List<String> wrapped = wrapTextLines(currentPage, 86);
            shapeRenderer.setProjectionMatrix(uiCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.05f, 0.06f, 0.1f, 0.92f);
            shapeRenderer.rect(28f, 28f, Gdx.graphics.getWidth() - 56f, 124f);
            shapeRenderer.end();

            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            font.setColor(Color.WHITE);
            font.draw(batch, activeSpeaker, 44f, 128f);
            float y = 96f;
            for (String line : wrapped) {
                font.draw(batch, line, 44f, y);
                y -= 22f;
                if (y < 52f) {
                    break;
                }
            }
            font.draw(batch,
                dialogPageIndex + 1 < pages.size() ? "E / Enter: Continue" : "E / Enter: Close",
                44f, 44f);
            batch.end();
        }

        if (lootMessage != null) {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            font.setColor(1f, 0.9f, 0.35f, 1f);
            font.draw(batch, lootMessage, 34f, Gdx.graphics.getHeight() - 24f);
            batch.end();
        }
    }


    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private List<String> wrapTextLines(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();
        for (String word : text.split("\\s+")) {
            if (currentLine.length() == 0) {
                currentLine.append(word);
                continue;
            }
            if (currentLine.length() + 1 + word.length() > maxChars) {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentLine.append(word);
            } else {
                currentLine.append(' ').append(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    private List<String> paginateDialog(String text, int maxChars, int linesPerPage) {
        List<String> wrappedLines = wrapTextLines(text, maxChars);
        List<String> pages = new ArrayList<>();
        if (wrappedLines.isEmpty()) {
            pages.add("");
            return pages;
        }
        StringBuilder currentPage = new StringBuilder();
        int lineCount = 0;
        for (String line : wrappedLines) {
            if (currentPage.length() > 0) {
                currentPage.append('\n');
            }
            currentPage.append(line);
            lineCount++;
            if (lineCount >= linesPerPage) {
                pages.add(currentPage.toString());
                currentPage.setLength(0);
                lineCount = 0;
            }
        }
        if (currentPage.length() > 0) {
            pages.add(currentPage.toString());
        }
        return pages;
    }

    private boolean hasActiveDialog() {
        return activeSpeaker != null && activeDialog != null && !activeDialog.isEmpty();
    }

    private void resetDialogPageIfNeeded() {
        if (!hasActiveDialog()) {
            dialogPageIndex = 0;
            dialogPageTrackingText = null;
            dialogPageTrackingSpeaker = null;
            return;
        }
        if (!activeDialog.equals(dialogPageTrackingText) || !activeSpeaker.equals(dialogPageTrackingSpeaker)) {
            dialogPageIndex = 0;
            dialogPageTrackingText = activeDialog;
            dialogPageTrackingSpeaker = activeSpeaker;
        }
    }

    private boolean advanceDialogPage() {
        if (!hasActiveDialog()) {
            return false;
        }
        resetDialogPageIfNeeded();
        List<String> pages = paginateDialog(activeDialog, 86, 3);
        if (dialogPageIndex + 1 < pages.size()) {
            dialogPageIndex++;
            return true;
        }
        return false;
    }

    private boolean advanceDialogueSequence() {
        if (activeDialogueSequenceIndex + 1 < activeDialogueSequence.size()) {
            activeDialogueSequenceIndex++;
            showCurrentDialogueSequencePage();
            return true;
        }
        return false;
    }

    private void showDialogueSequence(List<com.rogueforge.game.world.DialogueSystem.DialoguePage> pages,
                                      String fallbackSpeaker, String fallbackText) {
        activeDialogueSequence.clear();
        activeDialogueSequenceIndex = 0;
        if (pages != null) {
            for (com.rogueforge.game.world.DialogueSystem.DialoguePage page : pages) {
                if (page == null || page.text == null || page.text.isEmpty()) {
                    continue;
                }
                activeDialogueSequence.add(page);
            }
        }
        if (activeDialogueSequence.isEmpty()) {
            activeDialogueSequence.add(new com.rogueforge.game.world.DialogueSystem.DialoguePage(fallbackSpeaker, fallbackText));
        }
        showCurrentDialogueSequencePage();
    }

    private void showCurrentDialogueSequencePage() {
        if (activeDialogueSequence.isEmpty()) {
            clearActiveDialog();
            return;
        }
        com.rogueforge.game.world.DialogueSystem.DialoguePage page =
            activeDialogueSequence.get(Math.min(activeDialogueSequenceIndex, activeDialogueSequence.size() - 1));
        activeSpeaker = page.speaker;
        activeDialog = page.text;
        dialogPageIndex = 0;
        dialogPageTrackingText = activeDialog;
        dialogPageTrackingSpeaker = activeSpeaker;
    }

    private void clearActiveDialog() {
        activeSpeaker = null;
        activeDialog = null;
        activeDialogueSequence.clear();
        activeDialogueSequenceIndex = 0;
        dialogPageIndex = 0;
        dialogPageTrackingText = null;
        dialogPageTrackingSpeaker = null;
    }

    @Override
    public void resize(int width, int height) {
        uiCamera.setToOrtho(false, width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        floorTile.dispose();
        wallTile.dispose();
        doorTexture.dispose();
        chestTexture.dispose();
        shadowTexture.dispose();
        playerSprite.getTexture().dispose();
        for (TextureRegion npcSprite : npcSprites) {
            npcSprite.getTexture().dispose();
        }
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
