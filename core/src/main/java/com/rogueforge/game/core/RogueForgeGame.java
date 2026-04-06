package com.rogueforge.game.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.rogueforge.game.screen.SplashScreen;

/**
 * Main game class for Rogue Forge.
 * Implements ApplicationListener and delegates screen lifecycle to ScreenManager.
 */
public class RogueForgeGame implements ApplicationListener {
    private AssetManager assetManager;
    private ScreenManager screenManager;
    private final EventBus eventBus;

    public RogueForgeGame() {
        this(new EventBus());
    }

    public RogueForgeGame(EventBus eventBus) {
        this.eventBus = eventBus != null ? eventBus : new EventBus();
    }

    @Override
    public void create() {
        assetManager = new AssetManager();
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        screenManager = new ScreenManager();

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

    public EventBus getEventBus() {
        return eventBus;
    }

    public synchronized <T> T loadAsset(String path, Class<T> assetType) {
        if (!assetManager.isLoaded(path, assetType)) {
            assetManager.load(path, assetType);
            assetManager.finishLoadingAsset(path);
        }
        return assetManager.get(path, assetType);
    }

    public synchronized <T> T getAsset(String path, Class<T> assetType) {
        return assetManager.get(path, assetType);
    }

    public synchronized <T> boolean isAssetLoaded(String path, Class<T> assetType) {
        return assetManager.isLoaded(path, assetType);
    }

    public synchronized void unloadAsset(String path) {
        if (assetManager.isLoaded(path)) {
            assetManager.unload(path);
        }
    }

    public synchronized Texture loadTexture(String path) {
        Texture texture = loadAsset(path, Texture.class);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    public synchronized void unloadTexture(String path) {
        unloadAsset(path);
    }

    public AssetManager getAssets() { return assetManager; }
    public ScreenManager getScreenManager() { return screenManager; }
}
