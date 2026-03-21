package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
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

/**
 * Game Over screen displayed when the player dies.
 * Shows final score/stats and options to retry or return to main menu.
 * Rendered using ShapeRenderer for buttons and BitmapFont for text.
 */
public class GameOverScreen implements Screen {
    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont statsFont;
    private final BitmapFont buttonFont;
    private final OrthographicCamera camera;
    private final GlyphLayout layout;
    private final Texture backgroundTexture;

    private static final String[] BUTTON_LABELS = {"Try Again", "Main Menu"};
    private static final float BTN_W = 240f;
    private static final float BTN_H = 50f;
    private static final float BTN_GAP = 16f;

    // Game over stats
    private int finalScore = 0;
    private int totalEnemiesDefeated = 0;
    private float survivalTime = 0f;

    private int hoveredButton = -1;

    public GameOverScreen(RogueForgeGame game, ScreenManager screenManager) {
        this.game = game;
        this.screenManager = screenManager;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.layout = new GlyphLayout();
        this.backgroundTexture = loadTexture("Backgrounds/background 3/orig_big.png");

        // Setup fonts
        titleFont = new BitmapFont();
        titleFont.getData().setScale(4.0f);
        titleFont.setColor(Color.WHITE);

        statsFont = new BitmapFont();
        statsFont.getData().setScale(1.5f);
        statsFont.setColor(Color.WHITE);

        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.8f);
        buttonFont.setColor(Color.WHITE);
    }

    private Texture loadTexture(String relativePath) {
        Texture texture = new Texture(Gdx.files.internal(relativePath));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    /**
     * Set the final game stats to display.
     */
    public void setGameStats(int score, int enemiesDefeated, float survivalTime) {
        this.finalScore = score;
        this.totalEnemiesDefeated = enemiesDefeated;
        this.survivalTime = survivalTime;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0.04f, 0.03f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Mouse position (flip Y for our camera)
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();

        // Button layout — centered, stacked
        float totalBtnHeight = BUTTON_LABELS.length * BTN_H + (BUTTON_LABELS.length - 1) * BTN_GAP;
        float startY = (h - totalBtnHeight) / 2f - 80f;

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

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(backgroundTexture, 0f, 0f, w, h);
        batch.end();

        // Draw buttons
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.04f, 0.04f, 0.07f, 0.72f);
        shapeRenderer.rect(56f, startY - 150f, w - 112f, totalBtnHeight + 290f);
        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            float bx = (w - BTN_W) / 2f;
            float by = startY + (BUTTON_LABELS.length - 1 - i) * (BTN_H + BTN_GAP);
            if (i == hoveredButton) {
                shapeRenderer.setColor(0.9f, 0.45f, 0.1f, 0.9f); // orange hover
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.28f, 0.85f); // dark idle
            }
            shapeRenderer.rect(bx, by, BTN_W, BTN_H);
        }
        shapeRenderer.end();

        // Draw text
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        layout.setText(titleFont, "GAME OVER");
        titleFont.setColor(1f, 0.2f, 0.2f, 1f); // red title
        titleFont.draw(batch, "GAME OVER", (w - layout.width) / 2f, startY + totalBtnHeight + 100f);

        // Stats display
        float statsY = startY + totalBtnHeight;
        statsFont.setColor(Color.WHITE);
        statsFont.draw(batch, "Score: " + finalScore, 100f, statsY);
        statsFont.draw(batch, "Enemies: " + totalEnemiesDefeated, 100f, statsY - 50f);
        statsFont.draw(batch, "Time: " + String.format("%.1f", survivalTime) + "s", 100f, statsY - 100f);

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

    private void onButtonClicked(int index) {
        switch (index) {
            case 0: // Try Again
                screenManager.replace(new GameScreen(game, screenManager));
                break;
            case 1: // Main Menu
                screenManager.replace(new MainMenuScreen(game, screenManager));
                break;
        }
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
        statsFont.dispose();
        buttonFont.dispose();
        backgroundTexture.dispose();
    }
}
