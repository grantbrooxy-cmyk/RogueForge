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
import com.rogueforge.game.data.SaveFile;
import com.rogueforge.game.persistence.SaveManager;
import com.rogueforge.game.persistence.SettingsManager;

/**
 * Main menu — rendered without external skin/atlas files.
 * Uses ShapeRenderer for button backgrounds and BitmapFont for text.
 */
public class MainMenuScreen implements Screen {
    private static final String[] DIFFICULTY_OPTIONS = {"Easy", "Normal", "Hard", "Hell"};
    private static final float BACK_W = 180f;
    private static final float BACK_H = 44f;
    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont buttonFont;
    private final OrthographicCamera camera;
    private final GlyphLayout layout;
    private final SaveManager saveManager;
    private final SettingsManager settingsManager;
    private final Texture backgroundTexture;
    private final Texture uiTexture;

    private static final String[] BUTTON_LABELS = {"New Game", "Continue", "Options", "Quit"};
    private static final float BTN_W = 240f;
    private static final float BTN_H = 50f;
    private static final float BTN_GAP = 16f;

    private int hoveredButton = -1;
    private int hoveredDifficulty = -1;
    private boolean hoveredDifficultyBack;
    private boolean selectingDifficulty = false;

    public MainMenuScreen(RogueForgeGame game, ScreenManager screenManager) {
        this.game = game;
        this.screenManager = screenManager;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.layout = new GlyphLayout();
        this.saveManager = new SaveManager();
        this.settingsManager = new SettingsManager();
        this.settingsManager.load();
        this.backgroundTexture = loadTexture("Backgrounds/background 1/orig_big.png");
        this.uiTexture = createUiTexture();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);
        titleFont.setColor(Color.WHITE);

        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.8f);
        buttonFont.setColor(Color.WHITE);
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
    public void show() {}

    @Override
    public void render(float delta) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0.02f, 0.02f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Mouse position (flip Y for our camera)
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();

        // Button layout — centered, stacked below title
        float totalBtnHeight = BUTTON_LABELS.length * BTN_H + (BUTTON_LABELS.length - 1) * BTN_GAP;
        float startY = (h - totalBtnHeight) / 2f - 30f;

        hoveredButton = -1;
        hoveredDifficulty = -1;
        hoveredDifficultyBack = false;
        if (selectingDifficulty) {
            for (int i = 0; i < DIFFICULTY_OPTIONS.length; i++) {
                float bx = (w - BTN_W) / 2f;
                float by = startY + (DIFFICULTY_OPTIONS.length - 1 - i) * (BTN_H + BTN_GAP);
                if (mx >= bx && mx <= bx + BTN_W && my >= by && my <= by + BTN_H) {
                    hoveredDifficulty = i;
                }
            }
            float backX = (w - BACK_W) / 2f;
            float backY = startY - 54f;
            hoveredDifficultyBack = mx >= backX && mx <= backX + BACK_W && my >= backY && my <= backY + BACK_H;
        } else {
            for (int i = 0; i < BUTTON_LABELS.length; i++) {
                float bx = (w - BTN_W) / 2f;
                float by = startY + (BUTTON_LABELS.length - 1 - i) * (BTN_H + BTN_GAP);
                if (mx >= bx && mx <= bx + BTN_W && my >= by && my <= by + BTN_H) {
                    hoveredButton = i;
                }
            }
        }

        // Handle click
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && selectingDifficulty) {
            selectingDifficulty = false;
        }
        if (Gdx.input.justTouched()) {
            if (selectingDifficulty && hoveredDifficulty >= 0) {
                startNewGameWithDifficulty(DIFFICULTY_OPTIONS[hoveredDifficulty]);
            } else if (selectingDifficulty && hoveredDifficultyBack) {
                selectingDifficulty = false;
            } else if (!selectingDifficulty && hoveredButton >= 0) {
                onButtonClicked(hoveredButton);
            }
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(backgroundTexture, 0f, 0f, w, h);
        batch.setColor(0.08f, 0.1f, 0.15f, 0.92f);
        batch.draw(uiTexture, (w - 440f) / 2f, startY - 50f, 440f, totalBtnHeight + 200f);
        if (selectingDifficulty) {
            batch.setColor(0.08f, 0.08f, 0.12f, 0.9f);
            batch.draw(uiTexture, (w - 440f) / 2f, startY - 50f, 440f, DIFFICULTY_OPTIONS.length * (BTN_H + BTN_GAP) + 190f);
            for (int i = 0; i < DIFFICULTY_OPTIONS.length; i++) {
                float bx = (w - BTN_W) / 2f;
                float by = startY + (DIFFICULTY_OPTIONS.length - 1 - i) * (BTN_H + BTN_GAP);
                batch.setColor(i == hoveredDifficulty ? new Color(0.72f, 0.46f, 0.2f, 1f) : new Color(0.2f, 0.22f, 0.3f, 1f));
                batch.draw(uiTexture, bx, by, BTN_W, BTN_H);
            }
            float backX = (w - BACK_W) / 2f;
            float backY = startY - 54f;
            batch.setColor(hoveredDifficultyBack ? new Color(0.68f, 0.28f, 0.24f, 1f) : new Color(0.2f, 0.22f, 0.3f, 1f));
            batch.draw(uiTexture, backX, backY, BACK_W, BACK_H);
        } else {
            for (int i = 0; i < BUTTON_LABELS.length; i++) {
                float bx = (w - BTN_W) / 2f;
                float by = startY + (BUTTON_LABELS.length - 1 - i) * (BTN_H + BTN_GAP);
                batch.setColor(i == hoveredButton ? new Color(0.72f, 0.46f, 0.2f, 1f) : new Color(0.2f, 0.22f, 0.3f, 1f));
                batch.draw(uiTexture, bx, by, BTN_W, BTN_H);
            }
        }
        batch.setColor(Color.WHITE);

        // Title
        layout.setText(titleFont, "ROGUE FORGE");
        titleFont.setColor(1f, 0.85f, 0.4f, 1f);
        titleFont.draw(batch, "ROGUE FORGE", (w - layout.width) / 2f, startY + totalBtnHeight + 160f);

        // Button labels
        if (selectingDifficulty) {
            layout.setText(buttonFont, "Choose Difficulty");
            buttonFont.draw(batch, "Choose Difficulty", (w - layout.width) / 2f, startY + DIFFICULTY_OPTIONS.length * (BTN_H + BTN_GAP) + 66f);
            layout.setText(buttonFont, "Esc to cancel");
            buttonFont.draw(batch, "Esc to cancel", (w - layout.width) / 2f, startY - 22f);
            for (int i = 0; i < DIFFICULTY_OPTIONS.length; i++) {
                float bx = (w - BTN_W) / 2f;
                float by = startY + (DIFFICULTY_OPTIONS.length - 1 - i) * (BTN_H + BTN_GAP);
                layout.setText(buttonFont, DIFFICULTY_OPTIONS[i]);
                buttonFont.draw(batch, DIFFICULTY_OPTIONS[i],
                    bx + (BTN_W - layout.width) / 2f,
                    by + (BTN_H + layout.height) / 2f);
            }
            layout.setText(buttonFont, "Back");
            buttonFont.draw(batch, "Back", (w - layout.width) / 2f, startY - 24f - BACK_H / 2f + 22f);
        } else {
            for (int i = 0; i < BUTTON_LABELS.length; i++) {
                float bx = (w - BTN_W) / 2f;
                float by = startY + (BUTTON_LABELS.length - 1 - i) * (BTN_H + BTN_GAP);
                layout.setText(buttonFont, BUTTON_LABELS[i]);
                buttonFont.setColor(Color.WHITE);
                buttonFont.draw(batch, BUTTON_LABELS[i],
                    bx + (BTN_W - layout.width) / 2f,
                    by + (BTN_H + layout.height) / 2f);
            }
        }

        batch.end();
    }

    private void onButtonClicked(int index) {
        switch (index) {
            case 0: // New Game
                selectingDifficulty = true;
                break;
            case 1: // Continue
                SaveFile saveFile = saveManager.loadLatestSave();
                screenManager.replace(new GameScreen(game, screenManager, saveFile));
                break;
            case 2: // Options
                screenManager.push(new OptionsScreen(game, screenManager));
                break;
            case 3: // Quit
                Gdx.app.exit();
                break;
        }
    }

    private void startNewGameWithDifficulty(String difficultyLabel) {
        settingsManager.getSettings().setDifficultyMode(difficultyLabel);
        settingsManager.save();
        settingsManager.applySettings();
        selectingDifficulty = false;
        screenManager.replace(new GameScreen(game, screenManager));
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
        buttonFont.dispose();
        backgroundTexture.dispose();
        uiTexture.dispose();
    }
}
