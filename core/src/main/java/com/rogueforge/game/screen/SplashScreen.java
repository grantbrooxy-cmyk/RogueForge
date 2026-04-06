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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rogueforge.game.core.GameContext;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;

/**
 * Splash screen — shows game title with a fade-in effect, then transitions to main menu.
 */
public class SplashScreen implements Screen {
    static final String BACKGROUND_TEXTURE_PATH = "Backgrounds/background 1/orig_big.png";
    private final GameContext context;
    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final SpriteBatch batch;
    private final BitmapFont titleFont;
    private final BitmapFont subtitleFont;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;
    private final GlyphLayout layout;
    private final Texture backgroundTexture;

    private float elapsedTime = 0f;
    private static final float SPLASH_DURATION = 3f;

    public SplashScreen(RogueForgeGame game, ScreenManager screenManager) {
        this(game.getContext());
    }

    public SplashScreen(GameContext context) {
        this.context = context;
        this.game = context.getGame();
        this.screenManager = context.getScreenManager();
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.layout = new GlyphLayout();
        this.backgroundTexture = loadTexture(BACKGROUND_TEXTURE_PATH);

        // Title font — scale up the default bitmap font
        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);
        titleFont.setColor(Color.WHITE);

        subtitleFont = new BitmapFont();
        subtitleFont.getData().setScale(1.5f);
        subtitleFont.setColor(new Color(0.7f, 0.7f, 0.8f, 1f));
    }

    private Texture loadTexture(String relativePath) {
        return game.loadTexture(relativePath);
    }

    @Override
    public void show() {
        elapsedTime = 0f;
    }

    @Override
    public void render(float delta) {
        elapsedTime += delta;

        // Fade alpha: ramp up over first second, hold, then fade last 0.5s
        float alpha;
        if (elapsedTime < 1f) {
            alpha = elapsedTime; // fade in
        } else if (elapsedTime < SPLASH_DURATION - 0.5f) {
            alpha = 1f; // hold
        } else {
            alpha = Math.max(0, (SPLASH_DURATION - elapsedTime) / 0.5f); // fade out
        }

        Gdx.gl.glClearColor(0.03f, 0.03f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(backgroundTexture, 0f, 0f, w, h);
        batch.end();

        // Draw accent line
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.03f, 0.03f, 0.06f, alpha * 0.72f);
        shapeRenderer.rect(w * 0.18f, h * 0.26f, w * 0.64f, h * 0.42f);
        shapeRenderer.setColor(0.9f, 0.4f, 0.1f, alpha * 0.8f);
        shapeRenderer.rect(w * 0.3f, h * 0.48f, w * 0.4f, 3f);
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        titleFont.setColor(1f, 1f, 1f, alpha);
        layout.setText(titleFont, "ROGUE FORGE");
        titleFont.draw(batch, "ROGUE FORGE",
            (w - layout.width) / 2f,
            h * 0.58f);

        // Subtitle
        subtitleFont.setColor(0.7f, 0.7f, 0.8f, alpha * 0.7f);
        layout.setText(subtitleFont, "Build. Battle. Evolve.");
        subtitleFont.draw(batch, "Build. Battle. Evolve.",
            (w - layout.width) / 2f,
            h * 0.42f);

        batch.end();

        // Transition
        if (elapsedTime >= SPLASH_DURATION) {
            screenManager.replace(new MainMenuScreen(context));
        }
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
        titleFont.dispose();
        subtitleFont.dispose();
        shapeRenderer.dispose();
        game.unloadTexture(BACKGROUND_TEXTURE_PATH);
    }
}
