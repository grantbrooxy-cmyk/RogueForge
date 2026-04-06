package com.rogueforge.game.engine.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tracks active chunk regions around the player for expansive frontier zones.
 */
public class FrontierChunkManager {
    private final int chunkSizeTiles;
    private final int activeRadiusChunks;
    private final Set<String> activeChunks = new LinkedHashSet<>();
    private final Rectangle activeWorldBounds = new Rectangle();

    public FrontierChunkManager() {
        this(32, 2);
    }

    public FrontierChunkManager(int chunkSizeTiles, int activeRadiusChunks) {
        this.chunkSizeTiles = Math.max(8, chunkSizeTiles);
        this.activeRadiusChunks = Math.max(1, activeRadiusChunks);
    }

    public void update(Vector2 playerPosition, int tileWidth, int tileHeight) {
        activeChunks.clear();
        if (playerPosition == null) {
            activeWorldBounds.set(0f, 0f, 0f, 0f);
            return;
        }
        int chunkWorldWidth = chunkSizeTiles * Math.max(1, tileWidth);
        int chunkWorldHeight = chunkSizeTiles * Math.max(1, tileHeight);
        int playerChunkX = (int) Math.floor(playerPosition.x / chunkWorldWidth);
        int playerChunkY = (int) Math.floor(playerPosition.y / chunkWorldHeight);
        int minChunkX = playerChunkX - activeRadiusChunks;
        int maxChunkX = playerChunkX + activeRadiusChunks;
        int minChunkY = playerChunkY - activeRadiusChunks;
        int maxChunkY = playerChunkY + activeRadiusChunks;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
                activeChunks.add(chunkKey(chunkX, chunkY));
            }
        }
        activeWorldBounds.set(
            minChunkX * chunkWorldWidth,
            minChunkY * chunkWorldHeight,
            (maxChunkX - minChunkX + 1) * chunkWorldWidth,
            (maxChunkY - minChunkY + 1) * chunkWorldHeight
        );
    }

    public boolean isActiveWorldPosition(float worldX, float worldY, int tileWidth, int tileHeight) {
        int chunkWorldWidth = chunkSizeTiles * Math.max(1, tileWidth);
        int chunkWorldHeight = chunkSizeTiles * Math.max(1, tileHeight);
        int chunkX = (int) Math.floor(worldX / chunkWorldWidth);
        int chunkY = (int) Math.floor(worldY / chunkWorldHeight);
        return activeChunks.contains(chunkKey(chunkX, chunkY));
    }

    public Rectangle getActiveWorldBounds() {
        return new Rectangle(activeWorldBounds);
    }

    public Set<String> getActiveChunks() {
        return Collections.unmodifiableSet(activeChunks);
    }

    private String chunkKey(int chunkX, int chunkY) {
        return chunkX + ":" + chunkY;
    }
}
