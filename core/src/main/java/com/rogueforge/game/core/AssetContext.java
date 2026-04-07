package com.rogueforge.game.core;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

/**
 * Narrow asset-loading view for screens that only need queued asset access.
 */
public interface AssetContext {
    AssetManager getAssets();
    <T> void queueAsset(String path, Class<T> assetType);
    boolean updateAssetLoading();
    float getLoadingProgress();
    <T> T loadAsset(String path, Class<T> assetType);
    <T> T getAsset(String path, Class<T> assetType);
    <T> boolean isAssetLoaded(String path, Class<T> assetType);
    void unloadAsset(String path);
    Texture loadTexture(String path);
}
