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
    private static final String[] TAB_LABELS = {"Roster", "Equipment", "Command", "Archive"};
    private static final float TAB_W = 150f;
    private static final float TAB_H = 42f;
    private static final float TAB_GAP = 10f;
    private static final float EXIT_W = 140f;
    private static final float EXIT_H = 42f;
    private static final float RESERVE_BUTTON_W = 220f;
    private static final float RESERVE_BUTTON_H = 42f;
    private static final float RESERVE_PANEL_X = 720f;
    private static final float RESERVE_PANEL_Y = 470f;
    private static final float RESERVE_DROPDOWN_W = 280f;
    private static final float RESERVE_DROPDOWN_H = 42f;

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
    private final Texture uiTexture;

    private int currentTab = 0;
    private int selectedPartyMember = 0;
    private int hoveredTab = -1;
    private int hoveredRobot = -1;
    private int hoveredEquipment = -1;
    private int hoveredReserve = -1;
    private boolean hoveredMoveToReserve;
    private boolean hoveredReserveDropdown;
    private boolean hoveredExit;
    private String statusMessage;
    private float closeInputBlockTimer;
    private boolean showDetailView;
    private boolean reserveDropdownOpen;

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
        batch.setColor(0.08f, 0.1f, 0.14f, 0.9f);
        batch.draw(uiTexture, 28f, 24f, w - 56f, h - 48f);
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
        hoveredReserveDropdown = isReserveDropdownHit(mx, my, w, h);

        drawTabs(h);
        drawRosterPanel(h, mx, my);
        if (currentTab == 0) {
            drawStatsPanel(w, h);
        } else if (currentTab == 1) {
            drawEquipmentPanel(w, h, mx, my);
        } else if (currentTab == 2) {
            drawHubPanel(w, h);
        } else {
            drawArchivePanel(w, h);
        }
    }

    private void handleInput() {
        closeInputBlockTimer = Math.max(0f, closeInputBlockTimer - Gdx.graphics.getDeltaTime());
        if (closeInputBlockTimer <= 0f
            && (Gdx.input.isKeyJustPressed(Input.Keys.I) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))) {
            closeMenu();
            return;
        }
        if (currentTab == 0 && Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            showDetailView = !showDetailView;
            return;
        }
        if (currentTab == 0 && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            reserveDropdownOpen = !reserveDropdownOpen;
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
                    // Slot 0 is the player (always unlocked). Robot slots are
                    // gated by Forge Core level / grade — index i maps to robot
                    // party slot (i - 1), so lock when (i - 1) >= slotLimit.
                    if (i > 0 && (i - 1) >= gameScreen.getPartySlotLimit()) {
                        String req = gameScreen.getPartySlotNextGrade();
                        statusMessage = "Slot " + i + " is locked."
                            + (req != null ? " Requires " + req + "." : "");
                        return;
                    }
                    selectedPartyMember = i;
                    return;
                }
            }

            if (currentTab == 1) {
                List<EquipmentItem> catalog = gameScreen.getEquipmentCatalogForPartyMember(selectedPartyMember);
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
                            : "Cannot equip " + catalog.get(i).getName() + " right now.";
                        return;
                    }
                }
            } else if (currentTab == 0) {
                if (isReserveDropdownHit(mx, my, width, h)) {
                    reserveDropdownOpen = !reserveDropdownOpen;
                    return;
                }
                if (isMoveToReserveButtonHit(mx, my, width, h)) {
                    if (selectedPartyMember == 0) {
                        statusMessage = "The player always stays in the active party.";
                    } else {
                        statusMessage = gameScreen.moveRobotToReserve(selectedPartyMember - 1);
                    }
                    return;
                }
                int reserveIndex = reserveDropdownOpen ? reserveIndexAt(mx, my, width, h) : -1;
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
            batch.setColor(i == currentTab ? new Color(0.74f, 0.48f, 0.2f, 1f)
                : (i == hoveredTab ? new Color(0.34f, 0.38f, 0.5f, 1f) : new Color(0.2f, 0.22f, 0.3f, 1f)));
            batch.draw(uiTexture, tx, h - 140f, TAB_W, TAB_H);
        }
        float exitX = exitButtonX(w());
        float exitY = h - 140f;
        batch.setColor(hoveredExit ? new Color(0.68f, 0.28f, 0.24f, 1f) : new Color(0.32f, 0.18f, 0.18f, 1f));
        batch.draw(uiTexture, exitX, exitY, EXIT_W, EXIT_H);
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
        int slotLimit = gameScreen.getPartySlotLimit();
        batch.begin();
        batch.setColor(0.12f, 0.14f, 0.2f, 0.96f);
        batch.draw(uiTexture, 40f, 80f, 250f, h - 250f);

        for (int i = 0; i < gameScreen.getRobotCount() + 1; i++) {
            float ry = h - 214f - (i * 62f);
            boolean locked = i > 0 && (i - 1) >= slotLimit;
            if (!locked && mx >= 52f && mx <= 280f && my >= ry && my <= ry + 52f) {
                hoveredRobot = i;
            }
            if (locked) {
                // Locked slots: very dim, no highlight
                batch.setColor(0.10f, 0.11f, 0.16f, 1f);
            } else {
                batch.setColor(i == selectedPartyMember ? new Color(0.7f, 0.46f, 0.2f, 1f)
                    : (i == hoveredRobot ? new Color(0.3f, 0.34f, 0.46f, 1f) : new Color(0.18f, 0.2f, 0.28f, 1f)));
            }
            batch.draw(uiTexture, 52f, ry, 228f, 52f);
        }
        batch.setColor(Color.WHITE);
        for (int i = 0; i < gameScreen.getRobotCount() + 1; i++) {
            boolean locked = i > 0 && (i - 1) >= slotLimit;
            if (locked) {
                bodyFont.setColor(new Color(0.35f, 0.36f, 0.42f, 1f));
                bodyFont.draw(batch, "Slot " + i + " - Locked", 66f, h - 182f - (i * 62f));
                String req = gameScreen.getPartySlotNextGrade();
                bodyFont.draw(batch, req != null ? "Requires " + req : "", 66f, h - 206f - (i * 62f));
            } else {
                GameScreen.RobotStatBlock stats = i == 0 ? gameScreen.getPlayerStats() : gameScreen.getRobotStats(i - 1);
                bodyFont.setColor(Color.WHITE);
                String name = i == 0 ? gameScreen.getPlayerName() : gameScreen.getRobotName(i - 1);
                bodyFont.draw(batch, name, 66f, h - 182f - (i * 62f));
                bodyFont.draw(batch, "HP " + (int) stats.currentHealth + "/" + (int) stats.maxHealth,
                    66f, h - 206f - (i * 62f));
            }
        }
        bodyFont.setColor(Color.WHITE);
        batch.end();
    }

    private void drawStatsPanel(float w, float h) {
        if (showDetailView) {
            drawDetailPanel(w, h);
            return;
        }

        boolean playerSelected = selectedPartyMember == 0;
        GameScreen.RobotStatBlock stats = playerSelected
            ? gameScreen.getPlayerStats()
            : gameScreen.getRobotStats(selectedPartyMember - 1);
        batch.begin();
        batch.setColor(0.12f, 0.14f, 0.2f, 0.96f);
        batch.draw(uiTexture, 320f, 80f, w - 360f, h - 250f);
        batch.setColor(Color.WHITE);
        bodyFont.setColor(Color.WHITE);
        float x = 350f;
        float y = h - 190f;
        bodyFont.draw(batch, playerSelected ? gameScreen.getPlayerName() : gameScreen.getRobotName(selectedPartyMember - 1), x, y);
        bodyFont.draw(batch, playerSelected
            ? "Level " + gameScreen.getPlayerLevel() + "  Unlock Grade " + gameScreen.getUnlockedGrade()
            : "Level " + gameScreen.getRobotLevel(selectedPartyMember - 1)
                + "  Grade " + gameScreen.getRobotGrade(selectedPartyMember - 1)
                + "  Evo " + gameScreen.getRobotEvolutionTier(selectedPartyMember - 1)
                + "  Class " + gameScreen.getRobotClass(selectedPartyMember - 1),
            x, y - 24f);
        boolean activeRobotSelected = !playerSelected && gameScreen.hasActiveRobotAt(selectedPartyMember - 1);
        if (!playerSelected && !activeRobotSelected) {
            bodyFont.draw(batch, "This party slot is currently empty.", x, y - 58f);
            bodyFont.draw(batch, "Use the reserve dropdown on the right to deploy a robot here.", x, y - 93f);
        } else {
            bodyFont.draw(batch, "HP: " + (int) stats.currentHealth + "/" + (int) stats.maxHealth, x, y - 58f);
            bodyFont.draw(batch, "Agility: " + (int) stats.agility, x, y - 93f);
            bodyFont.draw(batch, "Strength: " + (int) stats.strength, x, y - 128f);
            bodyFont.draw(batch, "Intelligence: " + (int) stats.intelligence, x, y - 163f);
        }
        if (playerSelected) {
            bodyFont.draw(batch, "XP: " + gameScreen.getPlayerExperience() + "/" + gameScreen.getExperienceForNextLevel(), x, y - 233f);
        } else if (activeRobotSelected) {
            bodyFont.draw(batch, "Robot XP: " + gameScreen.getRobotExperience(selectedPartyMember - 1), x, y - 198f);
        }

        bodyFont.setColor(Color.LIGHT_GRAY);
        bodyFont.draw(batch, "Press D for detailed stats and progression.", x, 150f);
        bodyFont.draw(batch, "Press R to open or close the reserve list.", x, 122f);

        if (!playerSelected && activeRobotSelected) {
            float buttonX = x;
            float buttonY = 176f;
            batch.setColor(hoveredMoveToReserve ? new Color(0.7f, 0.42f, 0.2f, 1f) : new Color(0.24f, 0.26f, 0.36f, 1f));
            batch.draw(uiTexture, buttonX, buttonY, RESERVE_BUTTON_W, RESERVE_BUTTON_H);
            batch.setColor(Color.WHITE);
            layout.setText(buttonFont, "Move To Reserve");
            buttonFont.setColor(Color.WHITE);
            buttonFont.draw(batch, "Move To Reserve", buttonX + (RESERVE_BUTTON_W - layout.width) / 2f, buttonY + 28f);
        }

        List<String> reserveLines = gameScreen.getReserveRobotLines();
        float reserveX = RESERVE_PANEL_X;
        float reserveY = Math.min(h - 220f, RESERVE_PANEL_Y);
        hoveredReserve = reserveDropdownOpen ? reserveIndexAt(Gdx.input.getX(), h - Gdx.input.getY(), w, h) : -1;
        bodyFont.setColor(Color.WHITE);
        batch.setColor(hoveredReserveDropdown ? new Color(0.3f, 0.34f, 0.46f, 1f) : new Color(0.18f, 0.2f, 0.28f, 1f));
        batch.draw(uiTexture, reserveX, reserveY - 34f, RESERVE_DROPDOWN_W, RESERVE_DROPDOWN_H);
        batch.setColor(Color.WHITE);
        String dropdownLabel = reserveLines.isEmpty()
            ? "Reserve: none"
            : ("Reserve Frames (" + reserveLines.size() + ")" + (reserveDropdownOpen ? " -" : " +"));
        buttonFont.draw(batch, dropdownLabel, reserveX + 16f, reserveY - 6f);
        bodyFont.setColor(Color.LIGHT_GRAY);
        bodyFont.draw(batch, playerSelected
            ? "Select a robot slot, then choose a reserve robot."
            : "Selected slot: " + gameScreen.getRobotName(selectedPartyMember - 1),
            reserveX, reserveY - 58f);

        if (reserveDropdownOpen) {
            batch.setColor(new Color(0.08f, 0.1f, 0.16f, 0.98f));
            batch.draw(uiTexture, reserveX, reserveY - 190f, RESERVE_DROPDOWN_W, 132f);
            batch.setColor(Color.WHITE);
            float reserveListY = reserveY - 90f;
            if (reserveLines.isEmpty()) {
                bodyFont.draw(batch, "No reserve robots collected yet.", reserveX + 12f, reserveListY);
                bodyFont.draw(batch, "Recruit additional frames through quests and world events.", reserveX + 12f, reserveListY - 28f);
            } else {
                for (int i = 0; i < reserveLines.size() && i < 3; i++) {
                    bodyFont.setColor(i == hoveredReserve ? new Color(1f, 0.9f, 0.55f, 1f) : Color.WHITE);
                    bodyFont.draw(batch, reserveLines.get(i), reserveX + 12f, reserveListY - (i * 28f));
                }
            }
        }
        bodyFont.setColor(Color.WHITE);
        batch.end();
    }

    private void drawDetailPanel(float w, float h) {
        boolean playerSelected = selectedPartyMember == 0;
        boolean activeRobotSelected = !playerSelected && gameScreen.hasActiveRobotAt(selectedPartyMember - 1);
        GameScreen.RobotStatBlock stats = playerSelected
            ? gameScreen.getPlayerStats()
            : gameScreen.getRobotStats(selectedPartyMember - 1);
        Map<String, String> equipped = playerSelected
            ? gameScreen.getPlayerEquipmentSlots()
            : gameScreen.getRobotEquipmentSlots(selectedPartyMember - 1);
        List<String> questLines = gameScreen.getQuestJournalLines();
        List<String> settlementLines = gameScreen.getSettlementUpgradeLines();
        List<String> endgameLines = gameScreen.getEndgameProgressionLines();

        batch.begin();
        batch.setColor(0.12f, 0.14f, 0.2f, 0.96f);
        batch.draw(uiTexture, 320f, 80f, w - 360f, h - 250f);
        batch.setColor(Color.WHITE);
        bodyFont.setColor(Color.WHITE);
        float leftX = 350f;
        float topY = h - 170f;
        bodyFont.draw(batch, playerSelected ? gameScreen.getPlayerName() : gameScreen.getRobotName(selectedPartyMember - 1), leftX, topY);
        bodyFont.setColor(Color.LIGHT_GRAY);
        bodyFont.draw(batch, "Press D to return to party management.", leftX, topY - 28f);
        bodyFont.setColor(Color.WHITE);

        if (playerSelected || activeRobotSelected) {
            bodyFont.draw(batch, "HP: " + (int) stats.currentHealth + "/" + (int) stats.maxHealth, leftX, topY - 70f);
            bodyFont.draw(batch, "Agility: " + (int) stats.agility, leftX, topY - 100f);
            bodyFont.draw(batch, "Strength: " + (int) stats.strength, leftX, topY - 130f);
            bodyFont.draw(batch, "Intelligence: " + (int) stats.intelligence, leftX, topY - 160f);
            bodyFont.draw(batch, "Stamina: " + (int) stats.stamina, leftX, topY - 190f);
            if (!playerSelected) {
                bodyFont.draw(batch, "Derived Class: " + gameScreen.getRobotClass(selectedPartyMember - 1), leftX, topY - 220f);
            }
        } else {
            bodyFont.draw(batch, "This slot is empty.", leftX, topY - 70f);
        }

        bodyFont.draw(batch, "Equipment:", leftX, topY - 270f);
        if (equipped.isEmpty()) {
            bodyFont.draw(batch, "No equipment assigned.", leftX, topY - 300f);
        } else {
            int row = 0;
            for (Map.Entry<String, String> entry : equipped.entrySet()) {
                EquipmentItem item = gameScreen.findEquipmentItem(entry.getValue());
                String itemName = item != null ? item.getName() : entry.getValue();
                bodyFont.draw(batch, entry.getKey() + ": " + itemName, leftX, topY - 300f - (row * 26f));
                row++;
            }
        }

        float rightX = 690f;
        if (!playerSelected && activeRobotSelected) {
            List<String> abilityLines = gameScreen.getRobotAbilityProgressionLines(selectedPartyMember - 1);
            List<String> weaponLines = gameScreen.getRobotWeaponProgressionLines(selectedPartyMember - 1);
            bodyFont.draw(batch, "Abilities:", rightX, topY - 40f);
            for (int i = 0; i < abilityLines.size() && i < 5; i++) {
                bodyFont.draw(batch, abilityLines.get(i), rightX, topY - 70f - (i * 26f));
            }
            float weaponY = topY - 220f;
            bodyFont.draw(batch, "Weapons:", rightX, weaponY);
            if (weaponLines.isEmpty()) {
                bodyFont.draw(batch, "No weapon proficiency yet.", rightX, weaponY - 30f);
            } else {
                for (int i = 0; i < weaponLines.size() && i < 4; i++) {
                    bodyFont.draw(batch, weaponLines.get(i), rightX, weaponY - 30f - (i * 26f));
                }
            }
        }

        bodyFont.draw(batch, "Journal:", rightX, topY - 360f);
        if (questLines.isEmpty()) {
            bodyFont.draw(batch, "No active objectives.", rightX, topY - 390f);
        } else {
            for (int i = 0; i < questLines.size() && i < 3; i++) {
                bodyFont.draw(batch, questLines.get(i), rightX, topY - 390f - (i * 26f));
            }
        }
        float settlementY = topY - 500f;
        bodyFont.draw(batch, "Settlement:", rightX, settlementY);
        if (settlementLines.isEmpty()) {
            bodyFont.draw(batch, "Ironhaven remains unchanged.", rightX, settlementY - 30f);
        } else {
            for (int i = 0; i < settlementLines.size() && i < 3; i++) {
                bodyFont.draw(batch, settlementLines.get(i), rightX, settlementY - 30f - (i * 26f));
            }
        }
        float endgameY = topY - 620f;
        bodyFont.draw(batch, "Endgame:", rightX, endgameY);
        for (int i = 0; i < endgameLines.size() && i < 3; i++) {
            bodyFont.draw(batch, endgameLines.get(i), rightX, endgameY - 30f - (i * 26f));
        }
        batch.end();
    }

    private int reserveIndexAt(float mx, float my, float width, float height) {
        if (currentTab != 0 || !reserveDropdownOpen) {
            return -1;
        }
        float x = RESERVE_PANEL_X;
        List<String> reserveLines = gameScreen.getReserveRobotLines();
        float reserveY = Math.min(height - 220f, RESERVE_PANEL_Y);
        float reserveListY = reserveY - 90f;
        for (int i = 0; i < reserveLines.size() && i < 3; i++) {
            float lineY = reserveListY - (i * 28f);
            if (mx >= x && mx <= x + RESERVE_DROPDOWN_W && my >= lineY - 20f && my <= lineY + 8f) {
                return i;
            }
        }
        return -1;
    }

    private boolean isReserveDropdownHit(float mx, float my, float width, float height) {
        if (currentTab != 0) {
            return false;
        }
        float x = RESERVE_PANEL_X;
        float y = Math.min(height - 220f, RESERVE_PANEL_Y) - 34f;
        return mx >= x && mx <= x + RESERVE_DROPDOWN_W && my >= y && my <= y + RESERVE_DROPDOWN_H;
    }

    private boolean isMoveToReserveButtonHit(float mx, float my, float width, float height) {
        if (currentTab != 0 || selectedPartyMember == 0 || !gameScreen.hasActiveRobotAt(selectedPartyMember - 1)) {
            return false;
        }
        float x = 350f;
        float y = 176f;
        return mx >= x && mx <= x + RESERVE_BUTTON_W && my >= y && my <= y + RESERVE_BUTTON_H;
    }

    private void drawEquipmentPanel(float w, float h, float mx, float my) {
        boolean playerSelected = selectedPartyMember == 0;
        List<EquipmentItem> catalog = gameScreen.getEquipmentCatalogForPartyMember(selectedPartyMember);
        Map<String, String> equipped = playerSelected
            ? gameScreen.getPlayerEquipmentSlots()
            : gameScreen.getRobotEquipmentSlots(selectedPartyMember - 1);
        hoveredEquipment = -1;

        batch.begin();
        batch.setColor(0.12f, 0.14f, 0.2f, 0.96f);
        batch.draw(uiTexture, 320f, 80f, w - 360f, h - 250f);
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
            batch.setColor(i == hoveredEquipment ? new Color(0.3f, 0.34f, 0.46f, 1f) : new Color(0.18f, 0.2f, 0.28f, 1f));
            batch.draw(uiTexture, cardX, cardY, cardW, 64f);
            batch.setColor(Color.WHITE);
            bodyFont.draw(batch, item.getName() + "  [" + item.getSlotType() + "]", cardX + 12f, cardY + 48f);
            bodyFont.draw(batch, "Grade " + item.getGradeRequirement()
                + (equippedNow ? "  EQUIPPED" : ""), cardX + 12f, cardY + 28f);
            bodyFont.draw(batch, compactStatLine(item), cardX + 12f, cardY + 10f);
        }
        batch.end();
    }

    private void drawHubPanel(float w, float h) {
        List<String> settlementLines = gameScreen.getSettlementUpgradeLines();
        List<String> serviceLines = gameScreen.getUnlockedServiceLines();
        List<String> townChangeLines = gameScreen.getTownChangeLines();
        List<String> questLines = gameScreen.getQuestJournalLines();
        List<String> reserveLines = gameScreen.getReserveRobotLines();
        List<String> endgameLines = gameScreen.getEndgameProgressionLines();
        List<String> empireLines = gameScreen.getEmpireStatusLines();
        List<String> challengeLines = gameScreen.getChallengeUnlockLines();

        batch.begin();
        batch.setColor(0.12f, 0.14f, 0.2f, 0.96f);
        batch.draw(uiTexture, 320f, 80f, w - 360f, h - 250f);
        batch.setColor(Color.WHITE);
        bodyFont.setColor(Color.WHITE);

        float leftX = 350f;
        float topY = h - 170f;
        bodyFont.draw(batch, "Ironhaven Command", leftX, topY);
        bodyFont.setColor(Color.LIGHT_GRAY);
        bodyFont.draw(batch, "Town readiness, hangar logistics, training output, and current projects.", leftX, topY - 28f);
        bodyFont.setColor(Color.WHITE);

        bodyFont.draw(batch, "Settlement Upgrades:", leftX, topY - 74f);
        if (settlementLines.isEmpty()) {
            bodyFont.draw(batch, "No town upgrades secured yet.", leftX, topY - 104f);
        } else {
            for (int i = 0; i < settlementLines.size() && i < 5; i++) {
                bodyFont.draw(batch, settlementLines.get(i), leftX, topY - 104f - (i * 26f));
            }
        }

        float serviceY = topY - 250f;
        bodyFont.draw(batch, "Unlocked Services:", leftX, serviceY);
        if (serviceLines.isEmpty()) {
            bodyFont.draw(batch, "Ironhaven services are still basic.", leftX, serviceY - 30f);
        } else {
            for (int i = 0; i < serviceLines.size() && i < 4; i++) {
                bodyFont.draw(batch, serviceLines.get(i), leftX, serviceY - 30f - (i * 26f));
            }
        }

        float rightX = 720f;
        bodyFont.draw(batch, "Visible Town Changes:", rightX, topY - 10f);
        if (townChangeLines.isEmpty()) {
            bodyFont.draw(batch, "The town still looks unchanged.", rightX, topY - 40f);
        } else {
            for (int i = 0; i < townChangeLines.size() && i < 4; i++) {
                bodyFont.draw(batch, townChangeLines.get(i), rightX, topY - 40f - (i * 26f));
            }
        }

        float questY = topY - 200f;
        bodyFont.draw(batch, "Current Projects:", rightX, questY);
        if (questLines.isEmpty()) {
            bodyFont.draw(batch, "No active projects.", rightX, questY - 30f);
        } else {
            for (int i = 0; i < questLines.size() && i < 4; i++) {
                bodyFont.draw(batch, questLines.get(i), rightX, questY - 30f - (i * 26f));
            }
        }

        float reserveY = topY - 390f;
        bodyFont.draw(batch, "Reserve Frames:", rightX, reserveY);
        if (reserveLines.isEmpty()) {
            bodyFont.draw(batch, "No reserve robots recruited yet.", rightX, reserveY - 30f);
        } else {
            for (int i = 0; i < reserveLines.size() && i < 4; i++) {
                bodyFont.draw(batch, reserveLines.get(i), rightX, reserveY - 30f - (i * 26f));
            }
        }

        float commandY = topY - 390f;
        bodyFont.draw(batch, "Command Readiness:", leftX, commandY);
        List<String> commandLines = gameScreen.getActTwoCommandLines();
        if (commandLines.isEmpty()) {
            bodyFont.draw(batch, "Act 2 command systems are not online yet.", leftX, commandY - 30f);
        } else {
            for (int i = 0; i < commandLines.size() && i < 5; i++) {
                bodyFont.draw(batch, commandLines.get(i), leftX, commandY - 30f - (i * 26f));
            }
        }

        float legacyY = topY - 548f;
        bodyFont.draw(batch, "Forge Legacy:", leftX, legacyY);
        for (int i = 0; i < endgameLines.size() && i < 3; i++) {
            bodyFont.draw(batch, endgameLines.get(i), leftX, legacyY - 30f - (i * 26f));
        }

        float empireY = topY - 548f;
        bodyFont.draw(batch, "Empire Status:", rightX, empireY);
        for (int i = 0; i < empireLines.size() && i < 4; i++) {
            bodyFont.draw(batch, empireLines.get(i), rightX, empireY - 30f - (i * 26f));
        }

        float challengeY = topY - 700f;
        bodyFont.draw(batch, "Challenge Unlocks:", leftX, challengeY);
        for (int i = 0; i < challengeLines.size() && i < 4; i++) {
            bodyFont.draw(batch, challengeLines.get(i), leftX, challengeY - 30f - (i * 26f));
        }
        batch.end();
    }

    private void drawArchivePanel(float w, float h) {
        List<String> lines = gameScreen.getBestiaryArchiveLines();
        List<String> blueprintLines = gameScreen.getBlueprintFragmentInventoryLines();

        batch.begin();
        batch.setColor(0.12f, 0.14f, 0.2f, 0.96f);
        batch.draw(uiTexture, 320f, 80f, w - 360f, h - 250f);
        batch.setColor(Color.WHITE);

        float leftX = 350f;
        float topY = h - 170f;
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Bestiary Archive", leftX, topY);
        bodyFont.setColor(Color.LIGHT_GRAY);
        bodyFont.draw(batch, "Monsters revealed through Analyze, plus current research stock.", leftX, topY - 28f);

        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Research Stock:", leftX, topY - 72f);
        if (blueprintLines.isEmpty()) {
            bodyFont.draw(batch, "No blueprint fragments recovered yet.", leftX, topY - 100f);
        } else {
            for (int i = 0; i < blueprintLines.size() && i < 3; i++) {
                bodyFont.draw(batch, blueprintLines.get(i), leftX, topY - 100f - (i * 24f));
            }
        }

        if (lines.isEmpty()) {
            bodyFont.setColor(new Color(0.55f, 0.56f, 0.62f, 1f));
            bodyFont.draw(batch, "No data yet. Use Analyze in battle to scan enemies.", leftX, topY - 200f);
            bodyFont.draw(batch, "Scan level 1 reveals HP and stats.", leftX, topY - 230f);
            bodyFont.draw(batch, "Scan level 2 reveals speed and elemental data.", leftX, topY - 260f);
            bodyFont.draw(batch, "Scan level 3 reveals the enemy's gold reward.", leftX, topY - 290f);
        } else {
            // Two-column layout: left column and right column
            float col1X = leftX;
            float col2X = leftX + (w - 400f) / 2f;
            float lineH = 20f;
            float startY = topY - 200f;
            float maxY = 110f; // don't draw below this
            float col1Y = startY;
            float col2Y = startY;
            boolean useCol2 = false;

            for (String line : lines) {
                // Blank line = separator between entries; switch columns if col1 is getting long
                if (line.isEmpty()) {
                    if (!useCol2 && col1Y < startY - (h * 0.4f)) {
                        useCol2 = true;
                    }
                    if (useCol2) {
                        col2Y -= lineH * 0.5f;
                    } else {
                        col1Y -= lineH * 0.5f;
                    }
                    continue;
                }

                float drawX = useCol2 ? col2X : col1X;
                float drawY = useCol2 ? col2Y : col1Y;

                if (drawY < maxY) {
                    break; // ran out of vertical space
                }

                // Header lines (no leading spaces) drawn white; detail lines drawn light grey
                boolean isDetail = line.startsWith("  ");
                bodyFont.setColor(isDetail ? Color.LIGHT_GRAY : Color.WHITE);
                bodyFont.draw(batch, line.trim(), drawX, drawY);

                if (useCol2) {
                    col2Y -= lineH;
                } else {
                    col1Y -= lineH;
                }
            }
        }
        bodyFont.setColor(Color.WHITE);
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
            sb.append(formatUniqueBoost(item.getUniqueBoost()));
        }
        return sb.toString();
    }

    private String formatUniqueBoost(String uniqueBoost) {
        if (uniqueBoost == null || uniqueBoost.isEmpty()) {
            return "";
        }
        switch (uniqueBoost) {
            case "XP_BOOST":
                return "XP Boost";
            case "FIRST_STRIKE":
                return "First Strike";
            case "ARCANE_SURGE":
                return "Arcane Surge";
            case "LIFE_TAP":
                return "Life Tap";
            case "OVERDRIVE_LINK":
                return "Overdrive";
            case "COUNTER_FIELD":
                return "Counter Field";
            case "BARRIER_MATRIX":
                return "Barrier Matrix";
            case "AUTO_REPAIR":
                return "Auto Repair";
            default:
                return uniqueBoost;
        }
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
        uiTexture.dispose();
    }
}
