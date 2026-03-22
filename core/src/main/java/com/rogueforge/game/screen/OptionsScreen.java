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
import com.rogueforge.game.persistence.SettingsManager;

/**
 * Options screen for adjusting game settings.
 * Rendered using ShapeRenderer for buttons and BitmapFont for text.
 * Displays music volume, SFX volume, and fullscreen toggle as text with placeholder controls.
 */
public class OptionsScreen implements Screen {
    private static final String[] DIFFICULTY_MODES = {"EASY", "NORMAL", "HARD", "HELL"};
    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final SettingsManager settingsManager;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont labelFont;
    private final BitmapFont buttonFont;
    private final OrthographicCamera camera;
    private final GlyphLayout layout;
    private final Texture backgroundTexture;

    private static final float BTN_W = 240f;
    private static final float BTN_H = 50f;

    // Settings values
    private float musicVolume = 0.5f;
    private float sfxVolume = 0.7f;
    private boolean fullscreen = false;
    private String difficultyMode = "NORMAL";

    private int hoveredButton = -1;

    public OptionsScreen(RogueForgeGame game, ScreenManager screenManager) {
        this.game = game;
        this.screenManager = screenManager;
        this.settingsManager = new SettingsManager();
        this.settingsManager.load();
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.layout = new GlyphLayout();
        this.backgroundTexture = loadTexture("Backgrounds/background 2/orig_big.png");
        this.musicVolume = settingsManager.getSettings().getMusicVolume();
        this.sfxVolume = settingsManager.getSettings().getSfxVolume();
        this.fullscreen = settingsManager.getSettings().isFullscreen();
        this.difficultyMode = settingsManager.getSettings().getDifficultyMode();

        // Setup fonts
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);
        titleFont.setColor(Color.WHITE);

        labelFont = new BitmapFont();
        labelFont.getData().setScale(1.5f);
        labelFont.setColor(Color.WHITE);

        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.8f);
        buttonFont.setColor(Color.WHITE);
    }

    private Texture loadTexture(String relativePath) {
        Texture texture = new Texture(Gdx.files.internal(relativePath));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0.02f, 0.02f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Mouse position (flip Y for our camera)
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();

        // Title area
        float titleY = h - 80f;

        // Settings start position
        float settingsX = 100f;
        float settingsStartY = titleY - 150f;
        float settingLineHeight = 80f;

        float difficultyButtonX = settingsX + 310f;
        float difficultyButtonY = settingsStartY - 2 * settingLineHeight - 34f;

        // Back button position
        float backButtonX = (w - BTN_W) / 2f;
        float backButtonY = 50f;

        // Detect hover on back button
        hoveredButton = -1;
        if (mx >= difficultyButtonX && mx <= difficultyButtonX + 220f && my >= difficultyButtonY && my <= difficultyButtonY + BTN_H) {
            hoveredButton = 1;
        }
        if (mx >= backButtonX && mx <= backButtonX + BTN_W && my >= backButtonY && my <= backButtonY + BTN_H) {
            hoveredButton = 0;
        }

        // Handle click
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            onBack();
            return;
        }
        if (Gdx.input.justTouched()) {
            if (hoveredButton == 0) {
                onBack();
            } else if (hoveredButton == 1) {
                cycleDifficulty();
            }
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0f, 0f, w, h);
        batch.end();

        // Draw back button
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(hoveredButton == 1 ? 0.9f : 0.2f, hoveredButton == 1 ? 0.45f : 0.2f, hoveredButton == 1 ? 0.1f : 0.28f, hoveredButton == 1 ? 0.9f : 0.85f);
        shapeRenderer.rect(difficultyButtonX, difficultyButtonY, 220f, BTN_H);
        if (hoveredButton == 0) {
            shapeRenderer.setColor(0.9f, 0.45f, 0.1f, 0.9f);
        } else {
            shapeRenderer.setColor(0.2f, 0.2f, 0.28f, 0.85f);
        }
        shapeRenderer.rect(backButtonX, backButtonY, BTN_W, BTN_H);
        shapeRenderer.end();

        // Draw text
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        layout.setText(titleFont, "OPTIONS");
        titleFont.setColor(1f, 0.85f, 0.4f, 1f);
        titleFont.draw(batch, "OPTIONS", (w - layout.width) / 2f, titleY);

        // Settings labels
        labelFont.setColor(Color.WHITE);

        String musicText = "Music Volume: " + Math.round(musicVolume * 100) + "%";
        labelFont.draw(batch, musicText, settingsX, settingsStartY);

        String sfxText = "SFX Volume: " + Math.round(sfxVolume * 100) + "%";
        labelFont.draw(batch, sfxText, settingsX, settingsStartY - settingLineHeight);

        String fullscreenText = "Fullscreen: " + (fullscreen ? "On" : "Off");
        labelFont.draw(batch, fullscreenText, settingsX, settingsStartY - 2 * settingLineHeight);

        String difficultyText = "Difficulty: " + difficultyMode;
        labelFont.draw(batch, difficultyText, settingsX, settingsStartY - 3 * settingLineHeight);
        layout.setText(buttonFont, "Change");
        buttonFont.draw(batch, "Change",
            difficultyButtonX + (220f - layout.width) / 2f,
            difficultyButtonY + (BTN_H + layout.height) / 2f);

        // Back button text
        layout.setText(buttonFont, "Back");
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(batch, "Back",
            backButtonX + (BTN_W - layout.width) / 2f,
            backButtonY + (BTN_H + layout.height) / 2f);

        batch.end();
    }

    private void onBack() {
        saveSettings();
        screenManager.pop();
    }

    private void cycleDifficulty() {
        for (int i = 0; i < DIFFICULTY_MODES.length; i++) {
            if (DIFFICULTY_MODES[i].equals(difficultyMode)) {
                difficultyMode = DIFFICULTY_MODES[(i + 1) % DIFFICULTY_MODES.length];
                return;
            }
        }
        difficultyMode = "NORMAL";
    }

    private void saveSettings() {
        settingsManager.getSettings().setMusicVolume(musicVolume);
        settingsManager.getSettings().setSfxVolume(sfxVolume);
        settingsManager.getSettings().setFullscreen(fullscreen);
        settingsManager.getSettings().setDifficultyMode(difficultyMode);
        settingsManager.save();
        settingsManager.applySettings();
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
        labelFont.dispose();
        buttonFont.dispose();
        backgroundTexture.dispose();
    }
}
