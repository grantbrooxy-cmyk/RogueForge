package com.rogueforge.game.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.rogueforge.game.screen.SplashScreen;

/**
 * Main game class for Rogue Forge.
 * Implements ApplicationListener and delegates screen lifecycle to ScreenManager.
 */
public class RogueForgeGame implements ApplicationListener {
    private AssetManager assetManager;
    private ScreenManager screenManager;
    private static EventBus eventBus;

    @Override
    public void create() {
        assetManager = new AssetManager();
        eventBus = new EventBus();
        screenManager = new ScreenManager();

        assetManager.finishLoading();

        screenManager.push(new SplashScreen(this, screenManager));
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        if (screenManager.current() != null) {
            screenManager.current().render(delta);
        }
        // Dispose old screens after the frame is fully drawn
        screenManager.flushDispose();
    }

    @Override
    public void resize(int width, int height) {
        screenManager.resize(width, height);
    }

    @Override
    public void pause() {
        if (screenManager.current() != null) {
            screenManager.current().pause();
        }
    }

    @Override
    public void resume() {
        if (screenManager.current() != null) {
            screenManager.current().resume();
        }
    }

    @Override
    public void dispose() {
        if (screenManager != null) screenManager.dispose();
        if (assetManager != null) assetManager.dispose();
    }

    public static EventBus getEventBus() {
        if (eventBus == null) eventBus = new EventBus();
        return eventBus;
    }

    public AssetManager getAssets() { return assetManager; }
    public ScreenManager getScreenManager() { return screenManager; }
}
