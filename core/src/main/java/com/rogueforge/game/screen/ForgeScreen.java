package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import com.rogueforge.game.data.EquipmentItem;
import com.rogueforge.game.data.ForgeRecipeDefinition;
import java.util.List;

/**
 * Dedicated town forge screen for crafting weapons and robot parts,
 * and for fusing same-tier same-slot items into the next tier.
 */
public class ForgeScreen implements Screen {
    private static final float EXIT_W = 140f;
    private static final float EXIT_H = 42f;

    // Tab strip – matches WorkshopScreen dimensions so both screens feel consistent.
    private static final String[] TAB_LABELS = {"Craft", "Fuse"};
    private static final float TAB_W = 120f;
    private static final float TAB_H = 42f;
    private static final float TAB_GAP = 10f;

    private final ScreenManager screenManager;
    private final GameScreen gameScreen;
    private final SpriteBatch batch;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final BitmapFont buttonFont;
    private final GlyphLayout layout;
    private final OrthographicCamera camera;
    private final Texture backgroundTexture;
    private final Texture uiTexture;

    // ── Shared state ───────────────────────────────────────────────────────────
    private int currentTab = 0;
    private boolean hoveredExit;
    private String statusMessage;
    private float closeInputBlockTimer;

    // ── Craft tab state ────────────────────────────────────────────────────────
    private int hoveredRecipe = -1;
    private int hoveredShard = -1;
    private int hoveredComponent = -1;

    // ── Fuse tab state ─────────────────────────────────────────────────────────
    private int hoveredFusionItem = -1;
    private int selectedFusionItem1 = -1;  // index into owned list
    private int selectedFusionItem2 = -1;
    private boolean hoveredFuseButton = false;

    public ForgeScreen(RogueForgeGame game, ScreenManager screenManager, GameScreen gameScreen) {
        this.screenManager = screenManager;
        this.gameScreen = gameScreen;
        this.batch = new SpriteBatch();
        this.titleFont = new BitmapFont();
        this.bodyFont = new BitmapFont();
        this.buttonFont = new BitmapFont();
        this.layout = new GlyphLayout();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.backgroundTexture = loadTexture("Backgrounds/background 2/orig_big.png");
        this.uiTexture = createUiTexture();
        this.titleFont.getData().setScale(3f);
        this.bodyFont.getData().setScale(1.2f);
        this.buttonFont.getData().setScale(1.15f);
    }

    private Texture loadTexture(String relativePath) {
        Texture texture = new Texture(Gdx.files.internal(relativePath));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    private Texture createUiTexture() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture texture = new Texture(pm);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        pm.dispose();
        return texture;
    }

    @Override
    public void show() {
        closeInputBlockTimer = 0.18f;
        selectedFusionItem1 = -1;
        selectedFusionItem2 = -1;
        statusMessage = null;
    }

    @Override
    public void render(float delta) {
        handleInput();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();
        hoveredExit = isExitButtonHit(mx, my, w, h);

        Gdx.gl.glClearColor(0.07f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(backgroundTexture, 0f, 0f, w, h);
        batch.setColor(0.09f, 0.09f, 0.12f, 0.92f);
        batch.draw(uiTexture, 28f, 24f, w - 56f, h - 48f);
        batch.setColor(Color.WHITE);
        drawHeader(w, h);
        if (currentTab == 0) {
            drawForgeContent(w, h, mx, my);
        } else {
            drawFusionContent(w, h, mx, my);
        }
        batch.end();
    }

    // ── Input ──────────────────────────────────────────────────────────────────

    private void handleInput() {
        closeInputBlockTimer = Math.max(0f, closeInputBlockTimer - Gdx.graphics.getDeltaTime());
        if (closeInputBlockTimer <= 0f
            && (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.I))) {
            close();
            return;
        }

        if (!Gdx.input.justTouched()) {
            return;
        }

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();

        if (isExitButtonHit(mx, my, w, h)) {
            close();
            return;
        }

        // Tab strip
        for (int i = 0; i < TAB_LABELS.length; i++) {
            float tx = 48f + i * (TAB_W + TAB_GAP);
            if (mx >= tx && mx <= tx + TAB_W && my >= h - 140f && my <= h - 140f + TAB_H) {
                if (currentTab != i) {
                    currentTab = i;
                    statusMessage = null;
                    selectedFusionItem1 = -1;
                    selectedFusionItem2 = -1;
                }
                return;
            }
        }

        if (currentTab == 0) {
            handleCraftInput(mx, my, w, h);
        } else {
            handleFuseInput(mx, my, w, h);
        }
    }

    private void handleCraftInput(float mx, float my, float w, float h) {
        List<ForgeRecipeDefinition> recipes = gameScreen.getForgeRecipes();
        String[] sellableGrades = visibleShardGrades();
        String[] sellableComponents = gameScreen.getForgeSellableComponentIds().toArray(new String[0]);
        for (int i = 0; i < recipes.size(); i++) {
            float cardX = recipeCardX();
            float cardY = recipeCardY(h, i);
            float cardW = recipeCardWidth(w);
            if (mx >= cardX && mx <= cardX + cardW && my >= cardY && my <= cardY + 78f) {
                statusMessage = gameScreen.forgeRecipe(i);
                return;
            }
        }
        for (int i = 0; i < sellableGrades.length; i++) {
            float lineY = shardLineY(h, i);
            if (mx >= 56f && mx <= 328f && my >= lineY - 18f && my <= lineY + 6f) {
                statusMessage = gameScreen.sellShard(sellableGrades[i]);
                return;
            }
        }
        for (int i = 0; i < sellableComponents.length; i++) {
            float lineY = componentLineY(h, i);
            if (mx >= 56f && mx <= 328f && my >= lineY - 18f && my <= lineY + 6f) {
                statusMessage = gameScreen.sellForgeComponent(sellableComponents[i]);
                return;
            }
        }
    }

    private void handleFuseInput(float mx, float my, float w, float h) {
        List<EquipmentItem> owned = gameScreen.getEquipmentCatalog();

        // Fuse button: only active when both slots filled
        if (selectedFusionItem1 >= 0 && selectedFusionItem2 >= 0
                && selectedFusionItem1 < owned.size() && selectedFusionItem2 < owned.size()) {
            float btnX = fuseButtonX();
            float btnY = fuseButtonY(h);
            if (mx >= btnX && mx <= btnX + 160f && my >= btnY && my <= btnY + 38f) {
                String id1 = owned.get(selectedFusionItem1).getId();
                String id2 = owned.get(selectedFusionItem2).getId();
                statusMessage = gameScreen.fuseEquipment(id1, id2);
                selectedFusionItem1 = -1;
                selectedFusionItem2 = -1;
                return;
            }
        }

        // Item list (left column of fusion panel)
        int maxItems = Math.min(owned.size(), 12);
        for (int i = 0; i < maxItems; i++) {
            float lineY = fusionItemLineY(h, i);
            if (mx >= 56f && mx <= 360f && my >= lineY - 18f && my <= lineY + 6f) {
                if (i == selectedFusionItem1) {
                    // Deselect item 1
                    selectedFusionItem1 = -1;
                } else if (i == selectedFusionItem2) {
                    // Deselect item 2
                    selectedFusionItem2 = -1;
                } else if (selectedFusionItem1 < 0) {
                    selectedFusionItem1 = i;
                } else if (selectedFusionItem2 < 0) {
                    selectedFusionItem2 = i;
                } else {
                    // Both slots full – replace item 2 with new selection
                    selectedFusionItem2 = i;
                }
                return;
            }
        }
    }

    // ── Drawing: header ────────────────────────────────────────────────────────

    private void drawHeader(float w, float h) {
        // Tab strip (left side, same Y as exit button)
        for (int i = 0; i < TAB_LABELS.length; i++) {
            float tx = 48f + i * (TAB_W + TAB_GAP);
            batch.setColor(i == currentTab
                ? new Color(0.74f, 0.48f, 0.2f, 1f)
                : new Color(0.28f, 0.28f, 0.36f, 1f));
            batch.draw(uiTexture, tx, h - 140f, TAB_W, TAB_H);
        }
        // Exit button (right side)
        float exitX = exitButtonX(w);
        float exitY = h - 140f;
        batch.setColor(hoveredExit ? new Color(0.68f, 0.28f, 0.24f, 1f) : new Color(0.32f, 0.18f, 0.18f, 1f));
        batch.draw(uiTexture, exitX, exitY, EXIT_W, EXIT_H);

        batch.setColor(Color.WHITE);
        titleFont.setColor(1f, 0.88f, 0.55f, 1f);
        titleFont.draw(batch, "TOWN FORGE", 48f, h - 52f);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Toma can rebuild enemy salvage into weapons and frame parts.", 48f, h - 86f);

        // Tab labels
        buttonFont.setColor(Color.WHITE);
        for (int i = 0; i < TAB_LABELS.length; i++) {
            float tx = 48f + i * (TAB_W + TAB_GAP);
            layout.setText(buttonFont, TAB_LABELS[i]);
            buttonFont.setColor(i == currentTab ? new Color(1f, 0.9f, 0.55f, 1f) : Color.WHITE);
            buttonFont.draw(batch, TAB_LABELS[i], tx + (TAB_W - layout.width) / 2f, h - 112f);
        }

        // Exit label
        layout.setText(buttonFont, "Exit");
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(batch, "Exit", exitX + (EXIT_W - layout.width) / 2f, h - 112f);
    }

    // ── Drawing: Craft tab ─────────────────────────────────────────────────────

    private void drawForgeContent(float w, float h, float mx, float my) {
        List<String> shardLines = gameScreen.getShardInventoryLines();
        String[] sellableGrades = visibleShardGrades();
        List<String> componentLines = gameScreen.getForgeInventoryLines();
        String[] sellableComponents = gameScreen.getForgeSellableComponentIds().toArray(new String[0]);
        List<ForgeRecipeDefinition> recipes = gameScreen.getForgeRecipes();
        hoveredRecipe = -1;
        hoveredShard = -1;
        hoveredComponent = -1;

        float leftX = 56f;
        float topY = h - 190f;

        batch.setColor(new Color(0.12f, 0.14f, 0.2f, 0.96f));
        batch.draw(uiTexture, 44f, 80f, 300f, h - 260f);
        batch.draw(uiTexture, 370f, 80f, w - 414f, h - 260f);
        batch.setColor(Color.WHITE);

        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Shard Stock", leftX, topY);
        bodyFont.draw(batch, "Gold: " + gameScreen.getGold(), leftX, topY - 30f);
        for (int i = 0; i < shardLines.size() && i < 8; i++) {
            float lineY = shardLineY(h, i);
            if (i < sellableGrades.length && mx >= 56f && mx <= 328f && my >= lineY - 18f && my <= lineY + 6f) {
                hoveredShard = i;
            }
            bodyFont.setColor(i == hoveredShard ? new Color(1f, 0.9f, 0.55f, 1f) : Color.WHITE);
            bodyFont.draw(batch, shardLines.get(i), leftX, lineY);
        }
        bodyFont.setColor(Color.WHITE);
        float componentTitleY = topY - 280f;
        bodyFont.draw(batch, "Forge Components", leftX, componentTitleY);
        for (int i = 0; i < componentLines.size() && i < 8; i++) {
            float lineY = componentLineY(h, i);
            if (i < sellableComponents.length && mx >= 56f && mx <= 328f && my >= lineY - 18f && my <= lineY + 6f) {
                hoveredComponent = i;
            }
            bodyFont.setColor(i == hoveredComponent ? new Color(1f, 0.9f, 0.55f, 1f) : Color.WHITE);
            bodyFont.draw(batch, componentLines.get(i), leftX, lineY);
        }
        bodyFont.setColor(Color.WHITE);

        float recipeX = 392f;
        bodyFont.draw(batch, "Forge Patterns", recipeX, topY);
        if (statusMessage != null) {
            bodyFont.setColor(1f, 0.9f, 0.55f, 1f);
            bodyFont.draw(batch, statusMessage, recipeX, topY - 30f);
            bodyFont.setColor(Color.WHITE);
        } else {
            bodyFont.setColor(Color.LIGHT_GRAY);
            bodyFont.draw(batch, "Select a pattern to craft it instantly.", recipeX, topY - 30f);
            bodyFont.setColor(Color.WHITE);
        }

        for (int i = 0; i < recipes.size(); i++) {
            ForgeRecipeDefinition recipe = recipes.get(i);
            float cardX = recipeCardX();
            float cardY = recipeCardY(h, i);
            float cardW = recipeCardWidth(w);
            if (mx >= cardX && mx <= cardX + cardW && my >= cardY && my <= cardY + 78f) {
                hoveredRecipe = i;
            }
            boolean craftable = gameScreen.canForgeRecipe(recipe);
            batch.setColor(i == hoveredRecipe
                ? new Color(0.3f, 0.34f, 0.46f, 1f)
                : (craftable ? new Color(0.2f, 0.28f, 0.24f, 1f) : new Color(0.18f, 0.2f, 0.28f, 1f)));
            batch.draw(uiTexture, cardX, cardY, cardW, 78f);
            batch.setColor(Color.WHITE);
            bodyFont.draw(batch, recipe.getName() + "  [" + recipe.getCategory() + "]", cardX + 12f, cardY + 58f);
            bodyFont.draw(batch, recipe.getDescription(), cardX + 12f, cardY + 38f);
            bodyFont.draw(batch, gameScreen.buildForgeRequirementLine(recipe)
                + (craftable ? "  READY" : "  LOCKED"), cardX + 12f, cardY + 16f);
        }
    }

    // ── Drawing: Fuse tab ──────────────────────────────────────────────────────

    private void drawFusionContent(float w, float h, float mx, float my) {
        List<EquipmentItem> owned = gameScreen.getEquipmentCatalog();
        hoveredFusionItem = -1;
        hoveredFuseButton = false;

        float leftPanelX = 44f;
        float rightPanelX = 370f;
        float topY = h - 190f;

        // Background panels
        batch.setColor(new Color(0.12f, 0.14f, 0.2f, 0.96f));
        batch.draw(uiTexture, leftPanelX, 80f, 320f, h - 260f);
        batch.draw(uiTexture, rightPanelX, 80f, w - 414f, h - 260f);
        batch.setColor(Color.WHITE);

        // ── Left panel: item list ──────────────────────────────────────────────
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Equipment Collection", 56f, topY);
        bodyFont.setColor(Color.LIGHT_GRAY);
        bodyFont.draw(batch, "Click to select (2 items to fuse)", 56f, topY - 26f);
        bodyFont.setColor(Color.WHITE);

        if (owned.isEmpty()) {
            bodyFont.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
            bodyFont.draw(batch, "No equipment owned.", 56f, topY - 56f);
            bodyFont.setColor(Color.WHITE);
        } else {
            int maxItems = Math.min(owned.size(), 12);
            for (int i = 0; i < maxItems; i++) {
                EquipmentItem item = owned.get(i);
                float lineY = fusionItemLineY(h, i);
                boolean isHovered = mx >= 56f && mx <= 360f
                        && my >= lineY - 18f && my <= lineY + 6f;
                if (isHovered) {
                    hoveredFusionItem = i;
                }
                boolean isSel1 = (i == selectedFusionItem1);
                boolean isSel2 = (i == selectedFusionItem2);

                // Highlight background for selected items
                if (isSel1 || isSel2) {
                    batch.setColor(new Color(0.3f, 0.3f, 0.18f, 0.9f));
                    batch.draw(uiTexture, 52f, lineY - 18f, 308f, 26f);
                    batch.setColor(Color.WHITE);
                } else if (isHovered) {
                    batch.setColor(new Color(0.2f, 0.2f, 0.3f, 0.8f));
                    batch.draw(uiTexture, 52f, lineY - 18f, 308f, 26f);
                    batch.setColor(Color.WHITE);
                }

                String selTag = isSel1 ? " [1]" : isSel2 ? " [2]" : "";
                String label = "T" + item.getTier() + " [" + item.getSlotType() + "] "
                        + item.getName() + selTag;
                if (isSel1 || isSel2) {
                    bodyFont.setColor(new Color(1f, 0.9f, 0.55f, 1f));
                } else if (isHovered) {
                    bodyFont.setColor(new Color(0.8f, 0.85f, 1f, 1f));
                } else {
                    bodyFont.setColor(Color.WHITE);
                }
                bodyFont.draw(batch, label, 56f, lineY);
            }
            bodyFont.setColor(Color.WHITE);
            if (owned.size() > 12) {
                bodyFont.setColor(Color.LIGHT_GRAY);
                bodyFont.draw(batch, "+" + (owned.size() - 12) + " more items", 56f, fusionItemLineY(h, 12) - 4f);
                bodyFont.setColor(Color.WHITE);
            }
        }

        // ── Right panel: fusion station ────────────────────────────────────────
        float rX = rightPanelX + 22f;
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Fusion Station", rX, topY);
        bodyFont.setColor(Color.LIGHT_GRAY);
        bodyFont.draw(batch, "Combine two same-tier same-slot items into the next tier.", rX, topY - 26f);
        bodyFont.setColor(Color.WHITE);

        // Slot 1
        float slotY1 = topY - 80f;
        drawFusionSlot(rX, slotY1, w, "Item 1",
                selectedFusionItem1 >= 0 && selectedFusionItem1 < owned.size()
                        ? owned.get(selectedFusionItem1) : null);

        // Slot 2
        float slotY2 = topY - 170f;
        drawFusionSlot(rX, slotY2, w, "Item 2",
                selectedFusionItem2 >= 0 && selectedFusionItem2 < owned.size()
                        ? owned.get(selectedFusionItem2) : null);

        // Gold cost preview
        float costY = topY - 260f;
        if (selectedFusionItem1 >= 0 && selectedFusionItem2 >= 0
                && selectedFusionItem1 < owned.size() && selectedFusionItem2 < owned.size()) {
            EquipmentItem it1 = owned.get(selectedFusionItem1);
            int cost = it1.getTier() * 100;
            bodyFont.setColor(Color.WHITE);
            bodyFont.draw(batch, "Gold: " + gameScreen.getGold()
                    + "   Cost: " + cost + "g", rX, costY);

            // Fuse button
            float btnX = fuseButtonX();
            float btnY = fuseButtonY(h);
            hoveredFuseButton = mx >= btnX && mx <= btnX + 160f && my >= btnY && my <= btnY + 38f;
            batch.setColor(hoveredFuseButton
                    ? new Color(0.6f, 0.42f, 0.14f, 1f)
                    : new Color(0.36f, 0.26f, 0.1f, 1f));
            batch.draw(uiTexture, btnX, btnY, 160f, 38f);
            batch.setColor(Color.WHITE);
            layout.setText(buttonFont, "FUSE");
            buttonFont.setColor(new Color(1f, 0.9f, 0.55f, 1f));
            buttonFont.draw(batch, "FUSE", btnX + (160f - layout.width) / 2f, btnY + 28f);
            buttonFont.setColor(Color.WHITE);
        } else {
            bodyFont.setColor(Color.LIGHT_GRAY);
            bodyFont.draw(batch, "Select two items from the list.", rX, costY);
            bodyFont.setColor(Color.WHITE);
        }

        // Status message
        if (statusMessage != null) {
            bodyFont.setColor(1f, 0.9f, 0.55f, 1f);
            bodyFont.draw(batch, statusMessage, rX, topY - 330f);
            bodyFont.setColor(Color.WHITE);
        }
    }

    /** Draws a single fusion input slot with item info or an empty placeholder. */
    private void drawFusionSlot(float x, float y, float w, String label, EquipmentItem item) {
        // Slot spans from (x - 10) to (w - 48), leaving a right margin.
        float slotW = w - x - 38f;
        batch.setColor(item != null
                ? new Color(0.22f, 0.28f, 0.22f, 1f)
                : new Color(0.18f, 0.18f, 0.26f, 1f));
        batch.draw(uiTexture, x - 10f, y - 54f, slotW, 64f);
        batch.setColor(Color.WHITE);
        bodyFont.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        bodyFont.draw(batch, label + ":", x, y);
        if (item != null) {
            bodyFont.setColor(new Color(1f, 0.9f, 0.55f, 1f));
            bodyFont.draw(batch, item.getName(), x, y - 24f);
            bodyFont.setColor(Color.LIGHT_GRAY);
            bodyFont.draw(batch, "T" + item.getTier() + "  " + item.getSlotType(), x + 180f, y - 24f);
        } else {
            bodyFont.setColor(new Color(0.5f, 0.5f, 0.55f, 1f));
            bodyFont.draw(batch, "- empty -", x, y - 24f);
        }
        bodyFont.setColor(Color.WHITE);
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    private void close() {
        screenManager.pop();
    }

    // ── Geometry helpers ───────────────────────────────────────────────────────

    private float exitButtonX(float width) {
        return width - 48f - EXIT_W;
    }

    private boolean isExitButtonHit(float mx, float my, float width, float height) {
        float x = exitButtonX(width);
        float y = height - 140f;
        return mx >= x && mx <= x + EXIT_W && my >= y && my <= y + EXIT_H;
    }

    private float recipeCardWidth(float w) {
        return w - 438f;
    }

    private float recipeCardX() {
        return 392f;
    }

    private float recipeCardY(float h, int index) {
        return h - 320f - (index * 86f);
    }

    private float shardLineY(float h, int index) {
        return h - 262f - (index * 24f);
    }

    private float componentLineY(float h, int index) {
        return h - 542f - (index * 24f);
    }

    private float fusionItemLineY(float h, int index) {
        return h - 262f - (index * 26f);
    }

    private float fuseButtonX() {
        return 370f + 22f;
    }

    private float fuseButtonY(float h) {
        return h - 190f - 310f;
    }

    private String[] visibleShardGrades() {
        String[] ordered = {"S", "A", "B", "C", "D", "E", "F", "G"};
        java.util.List<String> visible = new java.util.ArrayList<>();
        for (String grade : ordered) {
            if (gameScreen.getShardCount(grade) > 0) {
                visible.add(grade);
            }
        }
        return visible.toArray(new String[0]);
    }

    // ── Screen interface ───────────────────────────────────────────────────────

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        bodyFont.dispose();
        buttonFont.dispose();
        backgroundTexture.dispose();
        uiTexture.dispose();
    }
}
