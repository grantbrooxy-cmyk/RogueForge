package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.rogueforge.game.core.AssetContext;
import com.rogueforge.game.core.GameContext;
import com.rogueforge.game.core.ScreenContext;

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

    private final AssetContext assetContext;
    private final ScreenContext screenContext;
    private final GameContext gameContext;
    private final Stage stage;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final Texture whitePixel;
    private final Label titleLabel;
    private final Label progressLabel;
    private final Label queueLabel;
    private final Label statusLabel;
    private final ProgressBar progressBar;

    public AssetLoadingScreen(GameContext context) {
        this((AssetContext) context, (ScreenContext) context, context);
    }

    public AssetLoadingScreen(AssetContext assetContext, ScreenContext screenContext, GameContext gameContext) {
        this.assetContext = assetContext;
        this.screenContext = screenContext;
        this.gameContext = gameContext;
        this.stage = new Stage(new ScreenViewport());
        this.titleFont = new BitmapFont();
        this.bodyFont = new BitmapFont();
        this.titleFont.getData().setScale(2.6f);
        this.bodyFont.getData().setScale(1.2f);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        this.whitePixel = new Texture(pixmap);
        pixmap.dispose();

        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, com.badlogic.gdx.graphics.Color.WHITE);
        Label.LabelStyle bodyStyle = new Label.LabelStyle(bodyFont, new com.badlogic.gdx.graphics.Color(0.78f, 0.8f, 0.88f, 1f));
        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(whitePixel)
            .tint(new com.badlogic.gdx.graphics.Color(0.09f, 0.1f, 0.15f, 1f));
        barStyle.knobBefore = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(whitePixel)
            .tint(new com.badlogic.gdx.graphics.Color(0.88f, 0.48f, 0.14f, 1f));

        this.titleLabel = new Label("Preparing Rogue Forge", titleStyle);
        this.progressLabel = new Label("0%", bodyStyle);
        this.queueLabel = new Label("", bodyStyle);
        this.statusLabel = new Label("Queueing textures and menu assets...", bodyStyle);
        this.progressBar = new ProgressBar(0f, 1f, 0.01f, false, barStyle);
        this.progressBar.setAnimateDuration(0.12f);

        Table root = new Table();
        root.setFillParent(true);
        root.center();
        root.add(titleLabel).padBottom(28f);
        root.row();
        root.add(progressBar).width(520f).height(28f).padBottom(18f);
        root.row();
        root.add(progressLabel).padBottom(10f);
        root.row();
        root.add(queueLabel).padBottom(8f);
        root.row();
        root.add(statusLabel);
        stage.addActor(root);
    }

    @Override
    public void show() {
        for (String texturePath : STARTUP_TEXTURES) {
            assetContext.queueAsset(texturePath, com.badlogic.gdx.graphics.Texture.class);
        }
    }

    @Override
    public void render(float delta) {
        boolean loaded = assetContext.updateAssetLoading();
        float progress = assetContext.getLoadingProgress();
        int queuedAssets = assetContext.getAssets().getQueuedAssets();
        int loadedAssets = assetContext.getAssets().getLoadedAssets();
        int totalAssets = Math.max(loadedAssets, loadedAssets + queuedAssets);

        Gdx.gl.glClearColor(0.03f, 0.03f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        progressBar.setValue(progress);
        progressLabel.setText(Math.round(progress * 100f) + "%");
        queueLabel.setText("Loaded " + loadedAssets + " / " + Math.max(1, totalAssets) + " startup assets");
        statusLabel.setText(loaded ? "Finalizing startup..." : "Queueing textures and menu assets...");
        stage.act(delta);
        stage.draw();

        if (loaded) {
            screenContext.getScreenManager().replace(new SplashScreen(gameContext));
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        titleFont.dispose();
        bodyFont.dispose();
        whitePixel.dispose();
    }
}
