package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rogueforge.game.core.GameContext;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import com.rogueforge.game.persistence.SaveManager;
import com.rogueforge.game.data.SaveFile;

/**
 * Pause menu screen displayed as a semi-transparent overlay.
 * Rendered using ShapeRenderer for buttons and BitmapFont for text.
 * Does not clear the screen beneath, allowing the game to show through.
 */
public class PauseMenuScreen implements Screen {
    static final String BACKGROUND_TEXTURE_PATH = "Backgrounds/background 2/orig_big.png";
    private final GameContext context;
    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final GameScreen gameScreen;
    private final SaveManager saveManager;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont buttonFont;
    private final OrthographicCamera camera;
    private final GlyphLayout layout;
    private Texture overlayTexture;
    private final Texture backgroundTexture;
    private final Texture uiTexture;

    private static final String[] BUTTON_LABELS = {"Resume", "Save", "Options", "Quit to Menu"};
    private static final float BTN_W = 240f;
    private static final float BTN_H = 50f;
    private static final float BTN_GAP = 16f;

    private static final float SAVE_SLOT_BTN_W = 300f;
    private static final float SAVE_SLOT_BTN_H = 50f;
    private static final float SAVE_SLOT_BTN_GAP = 16f;
    private static final float BACK_W = 180f;
    private static final float BACK_H = 44f;

    private int hoveredButton = -1;
    private boolean showingSaveSlots = false;
    private int hoveredSaveSlot = -1;
    private boolean hoveredBack;
    private float closeInputBlockTimer;

    public PauseMenuScreen(RogueForgeGame game, ScreenManager screenManager, GameScreen gameScreen) {
        this(game.getContext(), gameScreen);
    }

    public PauseMenuScreen(GameContext context, GameScreen gameScreen) {
        this.context = context;
        this.game = context.getGame();
        this.screenManager = context.getScreenManager();
        this.gameScreen = gameScreen;
        this.saveManager = context.getSaveManager();
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.layout = new GlyphLayout();
        this.backgroundTexture = loadTexture(BACKGROUND_TEXTURE_PATH);
        this.uiTexture = createUiTexture();

        // Create semi-transparent overlay texture
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0.5f);
        pm.fill();
        overlayTexture = new Texture(pm);
        pm.dispose();

        // Setup fonts
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);
        titleFont.setColor(Color.WHITE);

        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.8f);
        buttonFont.setColor(Color.WHITE);
    }

    private Texture loadTexture(String relativePath) {
        return game.loadTexture(relativePath);
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
        Gdx.input.setInputProcessor(null);
        closeInputBlockTimer = 0.18f;
    }

    @Override
    public void render(float delta) {
        closeInputBlockTimer = Math.max(0f, closeInputBlockTimer - delta);
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        // Draw semi-transparent overlay without clearing
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, w, h);
        batch.draw(overlayTexture, 0, 0, w, h);
        batch.end();

        // Mouse position (flip Y for our camera)
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();

        if (closeInputBlockTimer <= 0f && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (showingSaveSlots) {
                showingSaveSlots = false;
                hoveredSaveSlot = -1;
            } else {
                gameScreen.resumeGame();
                screenManager.pop();
            }
            return;
        }

        if (showingSaveSlots) {
            renderSaveSlotPicker(w, h, mx, my);
        } else {
            renderMainMenu(w, h, mx, my);
        }
    }

    private void renderMainMenu(float w, float h, float mx, float my) {
        // Button layout — centered, stacked below title
        float totalBtnHeight = BUTTON_LABELS.length * BTN_H + (BUTTON_LABELS.length - 1) * BTN_GAP;
        float startY = (h - totalBtnHeight) / 2f - 30f;

        // Detect hover
        hoveredButton = -1;
        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            float bx = (w - BTN_W) / 2f;
            float by = startY + (BUTTON_LABELS.length - 1 - i) * (BTN_H + BTN_GAP);
            if (mx >= bx && mx <= bx + BTN_W && my >= by && my <= by + BTN_H) {
                hoveredButton = i;
            }
        }

        // Handle click
        if (Gdx.input.justTouched() && hoveredButton >= 0) {
            onButtonClicked(hoveredButton);
        }

        // Draw buttons
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(0.08f, 0.1f, 0.15f, 0.95f);
        batch.draw(uiTexture, (w - 420f) / 2f, startY - 50f, 420f, totalBtnHeight + 180f);
        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            float bx = (w - BTN_W) / 2f;
            float by = startY + (BUTTON_LABELS.length - 1 - i) * (BTN_H + BTN_GAP);
            batch.setColor(i == hoveredButton ? new Color(0.72f, 0.46f, 0.2f, 1f) : new Color(0.2f, 0.22f, 0.3f, 1f));
            batch.draw(uiTexture, bx, by, BTN_W, BTN_H);
        }
        batch.setColor(Color.WHITE);

        // Title
        layout.setText(titleFont, "PAUSED");
        titleFont.setColor(1f, 0.85f, 0.4f, 1f);
        titleFont.draw(batch, "PAUSED", (w - layout.width) / 2f, startY + totalBtnHeight + 90f);

        // Button labels
        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            float bx = (w - BTN_W) / 2f;
            float by = startY + (BUTTON_LABELS.length - 1 - i) * (BTN_H + BTN_GAP);
            layout.setText(buttonFont, BUTTON_LABELS[i]);
            buttonFont.setColor(Color.WHITE);
            buttonFont.draw(batch, BUTTON_LABELS[i],
                bx + (BTN_W - layout.width) / 2f,
                by + (BTN_H + layout.height) / 2f);
        }

        batch.end();
    }

    private void renderSaveSlotPicker(float w, float h, float mx, float my) {
        int numSlots = SaveManager.MAX_SLOTS + 1; // 0-3 = 4 slots
        float totalSlotHeight = numSlots * SAVE_SLOT_BTN_H + (numSlots - 1) * SAVE_SLOT_BTN_GAP;
        float startY = (h - totalSlotHeight) / 2f - 30f;

        // Detect hover
        hoveredSaveSlot = -1;
        hoveredBack = isSaveBackHit(mx, my, w, h);
        for (int i = 0; i < numSlots; i++) {
            float bx = (w - SAVE_SLOT_BTN_W) / 2f;
            float by = startY + (numSlots - 1 - i) * (SAVE_SLOT_BTN_H + SAVE_SLOT_BTN_GAP);
            if (mx >= bx && mx <= bx + SAVE_SLOT_BTN_W && my >= by && my <= by + SAVE_SLOT_BTN_H) {
                hoveredSaveSlot = i;
            }
        }

        // Handle click
        if (Gdx.input.justTouched() && hoveredBack) {
            showingSaveSlots = false;
            hoveredSaveSlot = -1;
            return;
        }
        if (Gdx.input.justTouched() && hoveredSaveSlot >= 0) {
            onSaveSlotClicked(hoveredSaveSlot);
        }

        // Detect cancel button (right-click or ESC)
        if (Gdx.input.justTouched() && hoveredSaveSlot < 0) {
            showingSaveSlots = false;
            hoveredSaveSlot = -1;
        }

        // Draw slot buttons
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(0.08f, 0.1f, 0.15f, 0.95f);
        batch.draw(uiTexture, (w - 500f) / 2f, startY - 50f, 500f, totalSlotHeight + 180f);
        for (int i = 0; i < numSlots; i++) {
            float bx = (w - SAVE_SLOT_BTN_W) / 2f;
            float by = startY + (numSlots - 1 - i) * (SAVE_SLOT_BTN_H + SAVE_SLOT_BTN_GAP);
            batch.setColor(i == hoveredSaveSlot ? new Color(0.72f, 0.46f, 0.2f, 1f) : new Color(0.2f, 0.22f, 0.3f, 1f));
            batch.draw(uiTexture, bx, by, SAVE_SLOT_BTN_W, SAVE_SLOT_BTN_H);
        }
        batch.setColor(Color.WHITE);
        float backX = (w - BACK_W) / 2f;
        float backY = startY - 32f;
        batch.setColor(hoveredBack ? new Color(0.68f, 0.28f, 0.24f, 1f) : new Color(0.2f, 0.22f, 0.3f, 1f));
        batch.draw(uiTexture, backX, backY, BACK_W, BACK_H);
        batch.setColor(Color.WHITE);

        // Title
        layout.setText(titleFont, "SELECT SAVE SLOT");
        titleFont.setColor(1f, 0.85f, 0.4f, 1f);
        titleFont.draw(batch, "SELECT SAVE SLOT", (w - layout.width) / 2f, startY + totalSlotHeight + 90f);

        // Slot labels
        for (int i = 0; i < numSlots; i++) {
            float bx = (w - SAVE_SLOT_BTN_W) / 2f;
            float by = startY + (numSlots - 1 - i) * (SAVE_SLOT_BTN_H + SAVE_SLOT_BTN_GAP);
            String slotLabel = buildSlotLabel(i);
            layout.setText(buttonFont, slotLabel);
            buttonFont.setColor(Color.WHITE);
            buttonFont.draw(batch, slotLabel,
                bx + (SAVE_SLOT_BTN_W - layout.width) / 2f,
                by + (SAVE_SLOT_BTN_H + layout.height) / 2f);
        }
        layout.setText(buttonFont, "Back");
        buttonFont.draw(batch, "Back", backX + (BACK_W - layout.width) / 2f, backY + 28f);

        batch.end();
    }

    private boolean isSaveBackHit(float mx, float my, float width, float height) {
        if (!showingSaveSlots) {
            return false;
        }
        int numSlots = SaveManager.MAX_SLOTS + 1;
        float totalSlotHeight = numSlots * SAVE_SLOT_BTN_H + (numSlots - 1) * SAVE_SLOT_BTN_GAP;
        float startY = (height - totalSlotHeight) / 2f - 30f;
        float backX = (width - BACK_W) / 2f;
        float backY = startY - 32f;
        return mx >= backX && mx <= backX + BACK_W && my >= backY && my <= backY + BACK_H;
    }

    private String buildSlotLabel(int slot) {
        String slotName;
        if (slot == 0) {
            slotName = "Autosave";
        } else {
            slotName = "Slot " + slot;
        }

        if (saveManager.hasSave(slot)) {
            SaveFile save = saveManager.load(slot);
            if (save != null) {
                return slotName + " - HP:" + save.getPlayerHp() + " Gold:" + save.getCurrencyBalance();
            }
        }

        return slotName + " - Empty";
    }

    private void onButtonClicked(int index) {
        switch (index) {
            case 0: // Resume
                gameScreen.resumeGame();
                screenManager.pop();
                break;
            case 1: // Save
                onSave();
                break;
            case 2: // Options
                screenManager.push(new OptionsScreen(context));
                break;
            case 3: // Quit to Menu
                screenManager.replace(new MainMenuScreen(context));
                break;
        }
    }

    private void onSave() {
        showingSaveSlots = true;
        hoveredSaveSlot = -1;
    }

    private void onSaveSlotClicked(int slot) {
        SaveFile saveData = gameScreen.buildSaveFile(slot);
        saveManager.save(saveData, slot);
        showingSaveSlots = false;
        hoveredSaveSlot = -1;
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        titleFont.dispose();
        buttonFont.dispose();
        if (overlayTexture != null) {
            overlayTexture.dispose();
        }
        game.unloadTexture(BACKGROUND_TEXTURE_PATH);
        uiTexture.dispose();
    }
}
