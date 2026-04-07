package com.rogueforge.game.engine.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks active chunk regions around the player for expansive frontier zones.
 */
public class FrontierChunkManager {
    private static final int DEFAULT_REGION_SIZE_CHUNKS = 4;
    private final int chunkSizeTiles;
    private final int activeRadiusChunks;
    private final int regionSizeChunks;
    private final Set<String> activeChunks = new LinkedHashSet<>();
    private final Set<String> enteredChunks = new LinkedHashSet<>();
    private final Set<String> exitedChunks = new LinkedHashSet<>();
    private final Set<String> activeRegions = new LinkedHashSet<>();
    private final Rectangle activeWorldBounds = new Rectangle();
    private ChunkCoordinate centerChunk = new ChunkCoordinate(0, 0);

    public FrontierChunkManager() {
        this(32, 2, DEFAULT_REGION_SIZE_CHUNKS);
    }

    public FrontierChunkManager(int chunkSizeTiles, int activeRadiusChunks) {
        this(chunkSizeTiles, activeRadiusChunks, DEFAULT_REGION_SIZE_CHUNKS);
    }

    public FrontierChunkManager(int chunkSizeTiles, int activeRadiusChunks, int regionSizeChunks) {
        this.chunkSizeTiles = Math.max(8, chunkSizeTiles);
        this.activeRadiusChunks = Math.max(1, activeRadiusChunks);
        this.regionSizeChunks = Math.max(1, regionSizeChunks);
    }

    public void update(Vector2 playerPosition, int tileWidth, int tileHeight) {
        Set<String> previousChunks = new LinkedHashSet<>(activeChunks);
        activeChunks.clear();
        activeRegions.clear();
        enteredChunks.clear();
        exitedChunks.clear();
        if (playerPosition == null) {
            activeWorldBounds.set(0f, 0f, 0f, 0f);
            return;
        }
        int chunkWorldWidth = chunkSizeTiles * Math.max(1, tileWidth);
        int chunkWorldHeight = chunkSizeTiles * Math.max(1, tileHeight);
        int playerChunkX = (int) Math.floor(playerPosition.x / chunkWorldWidth);
        int playerChunkY = (int) Math.floor(playerPosition.y / chunkWorldHeight);
        centerChunk = new ChunkCoordinate(playerChunkX, playerChunkY);
        int minChunkX = playerChunkX - activeRadiusChunks;
        int maxChunkX = playerChunkX + activeRadiusChunks;
        int minChunkY = playerChunkY - activeRadiusChunks;
        int maxChunkY = playerChunkY + activeRadiusChunks;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
                activeChunks.add(chunkKey(chunkX, chunkY));
                activeRegions.add(regionKeyForChunk(chunkX, chunkY));
            }
        }
        for (String activeChunk : activeChunks) {
            if (!previousChunks.contains(activeChunk)) {
                enteredChunks.add(activeChunk);
            }
        }
        for (String previousChunk : previousChunks) {
            if (!activeChunks.contains(previousChunk)) {
                exitedChunks.add(previousChunk);
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

    public ChunkCoordinate getCenterChunk() {
        return centerChunk;
    }

    public Set<String> getActiveChunks() {
        return Collections.unmodifiableSet(activeChunks);
    }

    public Set<String> getEnteredChunks() {
        return Collections.unmodifiableSet(enteredChunks);
    }

    public Set<String> getExitedChunks() {
        return Collections.unmodifiableSet(exitedChunks);
    }

    public Set<String> getActiveRegions() {
        return Collections.unmodifiableSet(activeRegions);
    }

    public Map<String, Rectangle> buildRegionBounds(int tileWidth, int tileHeight) {
        Map<String, Rectangle> boundsByRegion = new LinkedHashMap<>();
        int chunkWorldWidth = chunkSizeTiles * Math.max(1, tileWidth);
        int chunkWorldHeight = chunkSizeTiles * Math.max(1, tileHeight);
        int regionWorldWidth = chunkWorldWidth * regionSizeChunks;
        int regionWorldHeight = chunkWorldHeight * regionSizeChunks;
        for (String regionKey : activeRegions) {
            RegionCoordinate coordinate = parseRegionKey(regionKey);
            boundsByRegion.put(regionKey, new Rectangle(
                coordinate.regionX * regionWorldWidth,
                coordinate.regionY * regionWorldHeight,
                regionWorldWidth,
                regionWorldHeight
            ));
        }
        return boundsByRegion;
    }

    public RegionCoordinate getRegionForWorldPosition(float worldX, float worldY, int tileWidth, int tileHeight) {
        int chunkWorldWidth = chunkSizeTiles * Math.max(1, tileWidth);
        int chunkWorldHeight = chunkSizeTiles * Math.max(1, tileHeight);
        int chunkX = (int) Math.floor(worldX / chunkWorldWidth);
        int chunkY = (int) Math.floor(worldY / chunkWorldHeight);
        return new RegionCoordinate(floorDiv(chunkX, regionSizeChunks), floorDiv(chunkY, regionSizeChunks));
    }

    private String chunkKey(int chunkX, int chunkY) {
        return chunkX + ":" + chunkY;
    }

    private String regionKeyForChunk(int chunkX, int chunkY) {
        return floorDiv(chunkX, regionSizeChunks) + ":" + floorDiv(chunkY, regionSizeChunks);
    }

    private RegionCoordinate parseRegionKey(String key) {
        if (key == null || key.isEmpty()) {
            return new RegionCoordinate(0, 0);
        }
        int separator = key.indexOf(':');
        if (separator <= 0 || separator >= key.length() - 1) {
            return new RegionCoordinate(0, 0);
        }
        int regionX = Integer.parseInt(key.substring(0, separator));
        int regionY = Integer.parseInt(key.substring(separator + 1));
        return new RegionCoordinate(regionX, regionY);
    }

    private int floorDiv(int value, int divisor) {
        return (int) Math.floor((double) value / Math.max(1, divisor));
    }

    public static final class ChunkCoordinate {
        public final int chunkX;
        public final int chunkY;

        public ChunkCoordinate(int chunkX, int chunkY) {
            this.chunkX = chunkX;
            this.chunkY = chunkY;
        }
    }

    public static final class RegionCoordinate {
        public final int regionX;
        public final int regionY;

        public RegionCoordinate(int regionX, int regionY) {
            this.regionX = regionX;
            this.regionY = regionY;
        }
    }
}
