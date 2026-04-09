package com.rogueforge.game.core;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.rogueforge.game.engine.GameEngineServices;
import com.rogueforge.game.persistence.MetaProgressionManager;
import com.rogueforge.game.persistence.SaveManager;
import com.rogueforge.game.persistence.SettingsManager;

/**
 * Lightweight runtime context shared across screens and systems.
 */
public class GameContext implements AssetContext, EventContext, ScreenContext, WorldContext {
    private final RogueForgeGame game;
    private final AssetManager assetManager;
    private final ScreenManager screenManager;
    private final EventBus eventBus;
    private final GameEngineServices engineServices;

    public GameContext(RogueForgeGame game, AssetManager assetManager, ScreenManager screenManager, EventBus eventBus,
                       GameEngineServices engineServices, SaveManager saveManager,
                       SettingsManager settingsManager, MetaProgressionManager metaProgressionManager) {
        this.game = game;
        this.assetManager = assetManager;
        this.screenManager = screenManager;
        this.eventBus = eventBus;
        this.engineServices = engineServices;
    }

    public RogueForgeGame getGame() {
        return game;
    }

    @Override
    public AssetManager getAssets() {
        return assetManager;
    }

    @Override
    public ScreenManager getScreenManager() {
        return screenManager;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public GameEngineServices getEngineServices() {
        return engineServices;
    }

    @Override
    public <T> T getService(Class<T> type) {
        return engineServices.getService(type);
    }

    public SaveManager getSaveManager() {
        return engineServices.getSaveManager();
    }

    public SettingsManager getSettingsManager() {
        return engineServices.getSettingsManager();
    }

    public MetaProgressionManager getMetaProgressionManager() {
        return engineServices.getMetaProgressionManager();
    }

    @Override
    public synchronized <T> void queueAsset(String path, Class<T> assetType) {
        assetManager.load(path, assetType);
    }

    @Override
    public synchronized boolean updateAssetLoading() {
        return assetManager.update();
    }

    @Override
    public synchronized float getLoadingProgress() {
        return assetManager.getProgress();
    }

    @Override
    public synchronized <T> T loadAsset(String path, Class<T> assetType) {
        assetManager.load(path, assetType);
        assetManager.finishLoadingAsset(path);
        return assetManager.get(path, assetType);
    }

    @Override
    public synchronized <T> T getAsset(String path, Class<T> assetType) {
        return assetManager.get(path, assetType);
    }

    @Override
    public synchronized <T> boolean isAssetLoaded(String path, Class<T> assetType) {
        return assetManager.isLoaded(path, assetType);
    }

    @Override
    public synchronized void unloadAsset(String path) {
        if (assetManager.isLoaded(path)) {
            assetManager.unload(path);
        }
    }

    @Override
    public synchronized Texture loadTexture(String path) {
        Texture texture = loadAsset(path, Texture.class);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }
}
