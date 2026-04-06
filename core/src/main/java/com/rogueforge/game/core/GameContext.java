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
public class GameContext {
    private final RogueForgeGame game;
    private final AssetManager assetManager;
    private final ScreenManager screenManager;
    private final EventBus eventBus;
    private final GameEngineServices engineServices;
    private final SaveManager saveManager;
    private final SettingsManager settingsManager;
    private final MetaProgressionManager metaProgressionManager;

    public GameContext(RogueForgeGame game, AssetManager assetManager, ScreenManager screenManager, EventBus eventBus,
                       GameEngineServices engineServices, SaveManager saveManager,
                       SettingsManager settingsManager, MetaProgressionManager metaProgressionManager) {
        this.game = game;
        this.assetManager = assetManager;
        this.screenManager = screenManager;
        this.eventBus = eventBus;
        this.engineServices = engineServices;
        this.saveManager = saveManager;
        this.settingsManager = settingsManager;
        this.metaProgressionManager = metaProgressionManager;
    }

    public RogueForgeGame getGame() {
        return game;
    }

    public AssetManager getAssets() {
        return assetManager;
    }

    public ScreenManager getScreenManager() {
        return screenManager;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public GameEngineServices getEngineServices() {
        return engineServices;
    }

    public SaveManager getSaveManager() {
        return saveManager;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public MetaProgressionManager getMetaProgressionManager() {
        return metaProgressionManager;
    }

    public synchronized <T> void queueAsset(String path, Class<T> assetType) {
        if (!assetManager.isLoaded(path, assetType) && !assetManager.contains(path)) {
            assetManager.load(path, assetType);
        }
    }

    public synchronized boolean updateAssetLoading() {
        return assetManager.update();
    }

    public synchronized float getLoadingProgress() {
        return assetManager.getProgress();
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
}
