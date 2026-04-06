package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rogueforge.game.core.GameContext;

/**
 * Queues startup assets and transitions into the normal splash/menu flow.
 */
public class AssetLoadingScreen implements Screen {
    private static final String[] STARTUP_TEXTURES = {
        SplashScreen.BACKGROUND_TEXTURE_PATH,
        MainMenuScreen.BACKGROUND_TEXTURE_PATH,
        OptionsScreen.BACKGROUND_TEXTURE_PATH,
        PauseMenuScreen.BACKGROUND_TEXTURE_PATH,
        GameOverScreen.BACKGROUND_TEXTURE_PATH
    };

    private final GameContext context;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final OrthographicCamera camera;
    private final GlyphLayout layout;

    public AssetLoadingScreen(GameContext context) {
        this.context = context;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.titleFont = new BitmapFont();
        this.bodyFont = new BitmapFont();
        this.camera = new OrthographicCamera();
        this.layout = new GlyphLayout();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.titleFont.getData().setScale(2.6f);
        this.bodyFont.getData().setScale(1.2f);
    }

    @Override
    public void show() {
        for (String texturePath : STARTUP_TEXTURES) {
            context.queueAsset(texturePath, com.badlogic.gdx.graphics.Texture.class);
        }
    }

    @Override
    public void render(float delta) {
        boolean loaded = context.updateAssetLoading();
        float progress = context.getLoadingProgress();
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0.03f, 0.03f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.09f, 0.1f, 0.15f, 1f);
        shapeRenderer.rect(w * 0.2f, h * 0.4f, w * 0.6f, 28f);
        shapeRenderer.setColor(0.88f, 0.48f, 0.14f, 1f);
        shapeRenderer.rect(w * 0.2f, h * 0.4f, w * 0.6f * progress, 28f);
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        titleFont.setColor(Color.WHITE);
        layout.setText(titleFont, "Preparing Rogue Forge");
        titleFont.draw(batch, layout, (w - layout.width) / 2f, h * 0.58f);
        bodyFont.setColor(0.78f, 0.8f, 0.88f, 1f);
        String progressText = Math.round(progress * 100f) + "%";
        layout.setText(bodyFont, progressText);
        bodyFont.draw(batch, layout, (w - layout.width) / 2f, h * 0.36f);
        batch.end();

        if (loaded) {
            context.getScreenManager().replace(new SplashScreen(context));
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
        shapeRenderer.dispose();
        titleFont.dispose();
        bodyFont.dispose();
    }
}
