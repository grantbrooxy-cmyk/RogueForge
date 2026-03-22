package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import com.rogueforge.game.data.EquipmentItem;
import java.util.List;
import java.util.Map;

/**
 * In-game roster and equipment menu opened with I.
 */
public class WorkshopScreen implements Screen {
    private static final String[] TAB_LABELS = {"Roster", "Equipment"};
    private static final float TAB_W = 150f;
    private static final float TAB_H = 42f;
    private static final float TAB_GAP = 10f;
    private static final float EXIT_W = 140f;
    private static final float EXIT_H = 42f;
    private static final float RESERVE_BUTTON_W = 220f;
    private static final float RESERVE_BUTTON_H = 42f;
    private static final float RESERVE_PANEL_X = 650f;
    private static final float RESERVE_PANEL_Y = 500f;

    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final GameScreen gameScreen;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final BitmapFont buttonFont;
    private final GlyphLayout layout;
    private final OrthographicCamera camera;
    private final Texture backgroundTexture;
    private final Texture panelTexture;
    private final Texture buttonTexture;

    private int currentTab = 0;
    private int selectedPartyMember = 0;
    private int hoveredTab = -1;
    private int hoveredRobot = -1;
    private int hoveredEquipment = -1;
    private int hoveredReserve = -1;
    private boolean hoveredMoveToReserve;
    private boolean hoveredExit;
    private String statusMessage;
    private float closeInputBlockTimer;

    public WorkshopScreen(RogueForgeGame game, ScreenManager screenManager, GameScreen gameScreen) {
        this.game = game;
        this.screenManager = screenManager;
        this.gameScreen = gameScreen;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.titleFont = new BitmapFont();
        this.bodyFont = new BitmapFont();
        this.buttonFont = new BitmapFont();
        this.layout = new GlyphLayout();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.backgroundTexture = loadTexture("Backgrounds/background 2/orig_big.png");
        this.panelTexture = loadTexture("4 GUI/1 Frames/Interface windows.png");
        this.buttonTexture = loadTexture("4 GUI/6 Buttons/ButtonMap4.png");
        this.titleFont.getData().setScale(3f);
        this.bodyFont.getData().setScale(1.2f);
        this.buttonFont.getData().setScale(1.15f);
    }

    private Texture loadTexture(String relativePath) {
        Texture texture = new Texture(Gdx.files.internal(relativePath));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    @Override
    public void show() {
        closeInputBlockTimer = 0.18f;
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        handleInput();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();

        Gdx.gl.glClearColor(0.07f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(backgroundTexture, 0f, 0f, w, h);
        batch.setColor(0.14f, 0.14f, 0.18f, 0.68f);
        batch.draw(buttonTexture, 28f, 24f, w - 56f, h - 48f);
        batch.setColor(Color.WHITE);
        batch.end();

        hoveredTab = -1;
        for (int i = 0; i < TAB_LABELS.length; i++) {
            float tx = 48f + i * (TAB_W + TAB_GAP);
            if (mx >= tx && mx <= tx + TAB_W && my >= h - 140f && my <= h - 140f + TAB_H) {
                hoveredTab = i;
            }
        }
        hoveredExit = isExitButtonHit(mx, my, w, h);
        hoveredMoveToReserve = isMoveToReserveButtonHit(mx, my, w, h);

        drawTabs(h);
        drawRosterPanel(h, mx, my);
        if (currentTab == 0) {
            drawStatsPanel(w, h);
        } else {
            drawEquipmentPanel(w, h, mx, my);
        }
    }

    private void handleInput() {
        closeInputBlockTimer = Math.max(0f, closeInputBlockTimer - Gdx.graphics.getDeltaTime());
        if (closeInputBlockTimer <= 0f
            && (Gdx.input.isKeyJustPressed(Input.Keys.I) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))) {
            closeMenu();
            return;
        }

        float width = w();
        float h = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();

        if (Gdx.input.justTouched()) {
            if (isExitButtonHit(mx, my, width, h)) {
                closeMenu();
                return;
            }

            for (int i = 0; i < TAB_LABELS.length; i++) {
                float tx = 48f + i * (TAB_W + TAB_GAP);
                if (mx >= tx && mx <= tx + TAB_W && my >= h - 140f && my <= h - 140f + TAB_H) {
                    currentTab = i;
                    return;
                }
            }

            for (int i = 0; i < gameScreen.getRobotCount() + 1; i++) {
                float ry = h - 214f - (i * 62f);
                if (mx >= 52f && mx <= 280f && my >= ry && my <= ry + 52f) {
                    selectedPartyMember = i;
                    return;
                }
            }

            if (currentTab == 1) {
                List<EquipmentItem> catalog = gameScreen.getEquipmentCatalog();
                for (int i = 0; i < catalog.size(); i++) {
                    float cardX = equipmentCardX(w(), i);
                    float cardY = equipmentCardY(h, i);
                    float cardW = equipmentCardWidth(w());
                    if (mx >= cardX && mx <= cardX + cardW && my >= cardY && my <= cardY + 64f) {
                        boolean equipped = selectedPartyMember == 0
                            ? gameScreen.equipPlayerItem(catalog.get(i))
                            : gameScreen.equipRobotItem(selectedPartyMember - 1, catalog.get(i));
                        statusMessage = equipped
                            ? "Equipped " + catalog.get(i).getName() + "."
                            : "Grade requirement not met for " + catalog.get(i).getName() + ".";
                        return;
                    }
                }
            } else {
                if (isMoveToReserveButtonHit(mx, my, width, h)) {
                    if (selectedPartyMember == 0) {
                        statusMessage = "The player always stays in the active party.";
                    } else {
                        statusMessage = gameScreen.moveRobotToReserve(selectedPartyMember - 1);
                    }
                    return;
                }
                int reserveIndex = reserveIndexAt(mx, my, width, h);
                if (reserveIndex >= 0) {
                    if (selectedPartyMember == 0) {
                        statusMessage = "Select a robot slot before deploying a reserve frame.";
                    } else {
                        statusMessage = gameScreen.deployReserveRobotToSlot(selectedPartyMember - 1, reserveIndex);
                    }
                    return;
                }
            }
        }
    }

    private void drawTabs(float h) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (int i = 0; i < TAB_LABELS.length; i++) {
            float tx = 48f + i * (TAB_W + TAB_GAP);
            batch.setColor(i == currentTab ? Color.WHITE : (i == hoveredTab ? new Color(0.9f, 0.9f, 0.9f, 1f) : new Color(0.78f, 0.78f, 0.78f, 1f)));
            batch.draw(buttonTexture, tx, h - 140f, TAB_W, TAB_H);
        }
        float exitX = exitButtonX(w());
        float exitY = h - 140f;
        batch.setColor(hoveredExit ? Color.WHITE : new Color(0.78f, 0.78f, 0.78f, 1f));
        batch.draw(buttonTexture, exitX, exitY, EXIT_W, EXIT_H);
        batch.setColor(Color.WHITE);
        titleFont.setColor(1f, 0.88f, 0.55f, 1f);
        titleFont.draw(batch, "PARTY MENU", 48f, h - 50f);
        for (int i = 0; i < TAB_LABELS.length; i++) {
            float tx = 48f + i * (TAB_W + TAB_GAP);
            layout.setText(buttonFont, TAB_LABELS[i]);
            buttonFont.setColor(Color.WHITE);
            buttonFont.draw(batch, TAB_LABELS[i], tx + (TAB_W - layout.width) / 2f, h - 112f);
        }
        layout.setText(buttonFont, "Exit");
        buttonFont.draw(batch, "Exit", exitX + (EXIT_W - layout.width) / 2f, h - 112f);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Press I or Esc to close", w() - 260f, h - 60f);
        batch.end();
    }

    private void closeMenu() {
        screenManager.pop();
    }

    private float exitButtonX(float width) {
        return width - 48f - EXIT_W;
    }

    private boolean isExitButtonHit(float mx, float my, float width, float height) {
        float x = exitButtonX(width);
        float y = height - 140f;
        return mx >= x && mx <= x + EXIT_W && my >= y && my <= y + EXIT_H;
    }

    private void drawRosterPanel(float h, float mx, float my) {
        hoveredRobot = -1;
        batch.begin();
        batch.draw(panelTexture, 40f, 80f, 250f, h - 250f);

        for (int i = 0; i < gameScreen.getRobotCount() + 1; i++) {
            float ry = h - 214f - (i * 62f);
            if (mx >= 52f && mx <= 280f && my >= ry && my <= ry + 52f) {
                hoveredRobot = i;
            }
            batch.setColor(i == selectedPartyMember ? Color.WHITE : (i == hoveredRobot ? new Color(0.92f, 0.92f, 0.92f, 1f) : new Color(0.8f, 0.8f, 0.8f, 1f)));
            batch.draw(buttonTexture, 52f, ry, 228f, 52f);
        }
        batch.setColor(Color.WHITE);
        for (int i = 0; i < gameScreen.getRobotCount() + 1; i++) {
            GameScreen.RobotStatBlock stats = i == 0 ? gameScreen.getPlayerStats() : gameScreen.getRobotStats(i - 1);
            bodyFont.setColor(Color.WHITE);
            String name = i == 0 ? gameScreen.getPlayerName() : gameScreen.getRobotName(i - 1);
            bodyFont.draw(batch, name, 66f, h - 182f - (i * 62f));
            bodyFont.draw(batch, "HP " + (int) stats.currentHealth + "/" + (int) stats.maxHealth,
                66f, h - 206f - (i * 62f));
        }
        batch.end();
    }

    private void drawStatsPanel(float w, float h) {
        boolean playerSelected = selectedPartyMember == 0;
        GameScreen.RobotStatBlock stats = playerSelected
            ? gameScreen.getPlayerStats()
            : gameScreen.getRobotStats(selectedPartyMember - 1);
        Map<String, String> equipped = playerSelected
            ? gameScreen.getPlayerEquipmentSlots()
            : gameScreen.getRobotEquipmentSlots(selectedPartyMember - 1);

        batch.begin();
        batch.draw(panelTexture, 320f, 80f, w - 360f, h - 250f);
        bodyFont.setColor(Color.WHITE);
        float x = 350f;
        float y = h - 190f;
        bodyFont.draw(batch, playerSelected ? gameScreen.getPlayerName() : gameScreen.getRobotName(selectedPartyMember - 1), x, y);
        bodyFont.draw(batch, playerSelected
            ? "Level " + gameScreen.getPlayerLevel() + "  Unlock Grade " + gameScreen.getUnlockedGrade()
            : "Level " + gameScreen.getRobotLevel(selectedPartyMember - 1)
                + "  Grade " + gameScreen.getRobotGrade(selectedPartyMember - 1)
                + "  Evo " + gameScreen.getRobotEvolutionTier(selectedPartyMember - 1),
            x, y - 24f);
        boolean activeRobotSelected = !playerSelected && gameScreen.hasActiveRobotAt(selectedPartyMember - 1);
        if (!playerSelected && !activeRobotSelected) {
            bodyFont.draw(batch, "This party slot is currently empty.", x, y - 58f);
            bodyFont.draw(batch, "Choose a reserve frame on the right to deploy it here.", x, y - 93f);
        } else {
            bodyFont.draw(batch, "HP: " + (int) stats.currentHealth + "/" + (int) stats.maxHealth, x, y - 58f);
            bodyFont.draw(batch, "Agility: " + (int) stats.agility, x, y - 93f);
            bodyFont.draw(batch, "Strength: " + (int) stats.strength, x, y - 128f);
            bodyFont.draw(batch, "Intelligence: " + (int) stats.intelligence, x, y - 163f);
            bodyFont.draw(batch, "Stamina: " + (int) stats.stamina, x, y - 198f);
        }
        if (playerSelected) {
            bodyFont.draw(batch, "XP: " + gameScreen.getPlayerExperience() + "/" + gameScreen.getExperienceForNextLevel(), x, y - 233f);
        } else if (activeRobotSelected) {
            bodyFont.draw(batch, "Robot XP: " + gameScreen.getRobotExperience(selectedPartyMember - 1), x, y - 233f);
        }

        bodyFont.draw(batch, "Equipped:", x, y - 280f);
        int row = 0;
        for (Map.Entry<String, String> entry : equipped.entrySet()) {
            EquipmentItem item = gameScreen.findEquipmentItem(entry.getValue());
            String itemName = item != null ? item.getName() : entry.getValue();
            bodyFont.draw(batch, entry.getKey() + ": " + itemName, x, y - 315f - (row * 28f));
            row++;
        }
        if (equipped.isEmpty()) {
            bodyFont.draw(batch, activeRobotSelected ? "No equipment assigned." : "Empty slots have no equipment.", x, y - 315f);
        }

        if (!playerSelected && activeRobotSelected) {
            List<String> abilityLines = gameScreen.getRobotAbilityProgressionLines(selectedPartyMember - 1);
            List<String> weaponLines = gameScreen.getRobotWeaponProgressionLines(selectedPartyMember - 1);
            float abilityX = x + 300f;
            bodyFont.draw(batch, "Abilities:", abilityX, y - 280f);
            if (abilityLines.isEmpty()) {
                bodyFont.draw(batch, "No learned abilities.", abilityX, y - 315f);
            } else {
                for (int i = 0; i < abilityLines.size() && i < 6; i++) {
                    bodyFont.draw(batch, abilityLines.get(i), abilityX, y - 315f - (i * 28f));
                }
            }
            float weaponY = y - 315f - (Math.min(6, abilityLines.size()) * 28f) - 20f;
            bodyFont.draw(batch, "Weapons:", abilityX, weaponY);
            if (weaponLines.isEmpty()) {
                bodyFont.draw(batch, "No weapon proficiency yet.", abilityX, weaponY - 28f);
            } else {
                for (int i = 0; i < weaponLines.size() && i < 4; i++) {
                    bodyFont.draw(batch, weaponLines.get(i), abilityX, weaponY - 28f - (i * 28f));
                }
            }
        }

        if (!playerSelected && activeRobotSelected) {
            float buttonX = x;
            float buttonY = 112f;
            batch.setColor(hoveredMoveToReserve ? Color.WHITE : new Color(0.82f, 0.82f, 0.82f, 1f));
            batch.draw(buttonTexture, buttonX, buttonY, RESERVE_BUTTON_W, RESERVE_BUTTON_H);
            batch.setColor(Color.WHITE);
            layout.setText(buttonFont, "Move To Reserve");
            buttonFont.setColor(Color.WHITE);
            buttonFont.draw(batch, "Move To Reserve", buttonX + (RESERVE_BUTTON_W - layout.width) / 2f, buttonY + 28f);
        }

        List<String> questLines = gameScreen.getQuestJournalLines();
        List<String> settlementLines = gameScreen.getSettlementUpgradeLines();
        List<String> reserveLines = gameScreen.getReserveRobotLines();
        hoveredReserve = reserveIndexAt(Gdx.input.getX(), h - Gdx.input.getY(), w, h);
        float journalX = x;
        float journalY = y - 470f;
        bodyFont.draw(batch, "Journal:", journalX, journalY);
        if (questLines.isEmpty()) {
            bodyFont.draw(batch, "No active objectives.", journalX, journalY - 28f);
        } else {
            for (int i = 0; i < questLines.size() && i < 3; i++) {
                bodyFont.draw(batch, questLines.get(i), journalX, journalY - 28f - (i * 28f));
            }
        }
        float settlementY = journalY - 28f - (Math.min(3, questLines.size()) * 28f) - 18f;
        bodyFont.draw(batch, "Settlement:", journalX, settlementY);
        if (settlementLines.isEmpty()) {
            bodyFont.draw(batch, "Ironhaven remains unchanged.", journalX, settlementY - 28f);
        } else {
            for (int i = 0; i < settlementLines.size() && i < 3; i++) {
                bodyFont.draw(batch, settlementLines.get(i), journalX, settlementY - 28f - (i * 28f));
            }
        }
        float reserveX = RESERVE_PANEL_X;
        float reserveY = Math.min(h - 220f, RESERVE_PANEL_Y);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Reserve Frames:", reserveX, reserveY);
        bodyFont.setColor(Color.LIGHT_GRAY);
        if (playerSelected) {
            bodyFont.draw(batch, "Select one of the robot slots on the left to manage deployment.", reserveX, reserveY - 28f);
        } else {
            bodyFont.draw(batch, "Selected slot: " + gameScreen.getRobotName(selectedPartyMember - 1), reserveX, reserveY - 28f);
            bodyFont.draw(batch, "Click a reserve frame below to swap it into this slot.", reserveX, reserveY - 56f);
            bodyFont.draw(batch, "The current active robot will move to reserve automatically.", reserveX, reserveY - 84f);
        }
        float reserveListY = reserveY - (playerSelected ? 56f : 112f);
        if (reserveLines.isEmpty()) {
            bodyFont.draw(batch, "No reserve robots collected yet.", reserveX, reserveListY);
            bodyFont.draw(batch, "Recruit additional frames through quests and world events.", reserveX, reserveListY - 28f);
        } else {
            for (int i = 0; i < reserveLines.size() && i < 3; i++) {
                bodyFont.setColor(i == hoveredReserve ? new Color(1f, 0.9f, 0.55f, 1f) : Color.WHITE);
                bodyFont.draw(batch, reserveLines.get(i), reserveX, reserveListY - (i * 28f));
            }
        }
        bodyFont.setColor(Color.WHITE);
        batch.end();
    }

    private int reserveIndexAt(float mx, float my, float width, float height) {
        if (currentTab != 0) {
            return -1;
        }
        float x = RESERVE_PANEL_X;
        List<String> reserveLines = gameScreen.getReserveRobotLines();
        float reserveY = Math.min(height - 220f, RESERVE_PANEL_Y);
        float reserveListY = reserveY - (selectedPartyMember == 0 ? 56f : 112f);
        for (int i = 0; i < reserveLines.size() && i < 3; i++) {
            float lineY = reserveListY - (i * 28f);
            if (mx >= x && mx <= width - 80f && my >= lineY - 20f && my <= lineY + 8f) {
                return i;
            }
        }
        return -1;
    }

    private boolean isMoveToReserveButtonHit(float mx, float my, float width, float height) {
        if (currentTab != 0 || selectedPartyMember == 0 || !gameScreen.hasActiveRobotAt(selectedPartyMember - 1)) {
            return false;
        }
        float x = 350f;
        float y = 112f;
        return mx >= x && mx <= x + RESERVE_BUTTON_W && my >= y && my <= y + RESERVE_BUTTON_H;
    }

    private void drawEquipmentPanel(float w, float h, float mx, float my) {
        List<EquipmentItem> catalog = gameScreen.getEquipmentCatalog();
        boolean playerSelected = selectedPartyMember == 0;
        Map<String, String> equipped = playerSelected
            ? gameScreen.getPlayerEquipmentSlots()
            : gameScreen.getRobotEquipmentSlots(selectedPartyMember - 1);
        hoveredEquipment = -1;

        batch.begin();
        batch.draw(panelTexture, 320f, 80f, w - 360f, h - 250f);
        batch.setColor(Color.WHITE);
        bodyFont.setColor(Color.WHITE);
        String targetName = playerSelected ? gameScreen.getPlayerName() : gameScreen.getRobotName(selectedPartyMember - 1);
        bodyFont.draw(batch, "Equip items to " + targetName, 350f, h - 180f);
        bodyFont.draw(batch, playerSelected
            ? "Unlocked grade: " + gameScreen.getUnlockedGrade()
            : "Robot grade: " + gameScreen.getRobotGrade(selectedPartyMember - 1),
            350f, h - 208f);
        drawEquippedSlots(batch, equipped, 350f, h - 238f);
        if (statusMessage != null) {
            bodyFont.draw(batch, statusMessage, 350f, h - 326f);
        }

        for (int i = 0; i < catalog.size(); i++) {
            EquipmentItem item = catalog.get(i);
            float cardX = equipmentCardX(w, i);
            float cardY = equipmentCardY(h, i);
            float cardW = equipmentCardWidth(w);
            if (mx >= cardX && mx <= cardX + cardW && my >= cardY && my <= cardY + 64f) {
                hoveredEquipment = i;
            }
            boolean equippedNow = item.getId().equals(equipped.get(item.getSlotType()));
            batch.setColor(i == hoveredEquipment ? Color.WHITE : new Color(0.82f, 0.82f, 0.82f, 1f));
            batch.draw(buttonTexture, cardX, cardY, cardW, 64f);
            batch.setColor(Color.WHITE);
            bodyFont.draw(batch, item.getName() + "  [" + item.getSlotType() + "]", cardX + 12f, cardY + 48f);
            bodyFont.draw(batch, "Grade " + item.getGradeRequirement()
                + (equippedNow ? "  EQUIPPED" : ""), cardX + 12f, cardY + 28f);
            bodyFont.draw(batch, compactStatLine(item), cardX + 12f, cardY + 10f);
        }
        batch.end();
    }

    private void drawEquippedSlots(SpriteBatch spriteBatch, Map<String, String> equipped, float x, float startY) {
        if (equipped.isEmpty()) {
            bodyFont.draw(spriteBatch, "Current gear: none", x, startY);
            return;
        }

        bodyFont.draw(spriteBatch, "Current gear:", x, startY);
        int row = 1;
        for (Map.Entry<String, String> entry : equipped.entrySet()) {
            EquipmentItem item = gameScreen.findEquipmentItem(entry.getValue());
            bodyFont.draw(spriteBatch,
                entry.getKey() + ": " + (item != null ? item.getName() : entry.getValue()),
                x, startY - (row * 22f));
            row++;
        }
    }

    private String compactStatLine(EquipmentItem item) {
        StringBuilder sb = new StringBuilder();
        appendStat(sb, "HP", item.getHpBonus());
        appendStat(sb, "STR", item.getAttackBonus());
        appendStat(sb, "STA", item.getDefenseBonus());
        appendStat(sb, "AGI", item.getSpeedBonus());
        appendStat(sb, "INT", item.getIntelligenceBonus());
        if (!item.getUniqueBoost().isEmpty()) {
            if (sb.length() > 0) {
                sb.append("  ");
            }
            sb.append(item.getUniqueBoost());
        }
        return sb.toString();
    }

    private void appendStat(StringBuilder sb, String label, int value) {
        if (value == 0) {
            return;
        }
        if (sb.length() > 0) {
            sb.append("  ");
        }
        sb.append(label).append(value > 0 ? "+" : "").append(value);
    }

    private float equipmentCardWidth(float w) {
        return (w - 470f) / 2f;
    }

    private float equipmentCardX(float w, int index) {
        return 350f + (index % 2) * (equipmentCardWidth(w) + 18f);
    }

    private float equipmentCardY(float h, int index) {
        return h - 420f - (index / 2) * 74f;
    }

    private float w() {
        return Gdx.graphics.getWidth();
    }

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
        shapeRenderer.dispose();
        titleFont.dispose();
        bodyFont.dispose();
        buttonFont.dispose();
        backgroundTexture.dispose();
        panelTexture.dispose();
        buttonTexture.dispose();
    }
}
