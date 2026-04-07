package com.rogueforge.game.engine.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.data.ZoneDefinition;

/**
 * Bridges authored zone definitions to both gameplay metadata and the shared
 * AssetManager-backed TMX map instance used for rendering.
 */
public class ZoneLoader {
    private final RogueForgeGame game;
    private final TmxWorldLoader worldLoader;

    public ZoneLoader(RogueForgeGame game, TmxWorldLoader worldLoader) {
        if (game == null) {
            throw new IllegalArgumentException("Game is required");
        }
        this.game = game;
        this.worldLoader = worldLoader != null ? worldLoader : new TmxWorldLoader();
    }

    public LoadedZoneContent load(ZoneDefinition definition) {
        return load(definition, 0L, null, null);
    }

    public LoadedZoneContent load(ZoneDefinition definition, long worldSeed, FrontierZoneGenerator frontierZoneGenerator) {
        return load(definition, worldSeed, frontierZoneGenerator, null);
    }

    public LoadedZoneContent load(ZoneDefinition definition, long worldSeed,
                                  FrontierZoneGenerator frontierZoneGenerator,
                                  WorldGenerator worldGenerator) {
        if (definition == null) {
            throw new IllegalArgumentException("Zone definition is required");
        }

        TmxWorldLoader.LoadedZone loadedZone = worldLoader.load(definition);
        if (definition.isExpansiveFrontier() && frontierZoneGenerator != null) {
            loadedZone = frontierZoneGenerator.generate(definition, loadedZone, worldSeed);
            if (worldGenerator != null) {
                loadedZone = worldGenerator.decorate(definition, loadedZone, worldSeed);
            }
        }
        String tilemapPath = definition.getTilemapPath();
        TiledMap tiledMap = null;
        if (tilemapPath != null && !tilemapPath.isEmpty()) {
            try {
                tiledMap = game.loadAsset(tilemapPath, TiledMap.class);
            } catch (RuntimeException ignored) {
                tiledMap = null;
            }
        }
        return new LoadedZoneContent(definition, loadedZone, tiledMap, tilemapPath);
    }

    public void unload(String tilemapPath) {
        if (tilemapPath != null && !tilemapPath.isEmpty()) {
            game.unloadAsset(tilemapPath);
        }
    }

    public static final class LoadedZoneContent {
        private final ZoneDefinition definition;
        private final TmxWorldLoader.LoadedZone zone;
        private final TiledMap tiledMap;
        private final String tilemapPath;

        public LoadedZoneContent(ZoneDefinition definition, TmxWorldLoader.LoadedZone zone,
                                 TiledMap tiledMap, String tilemapPath) {
            this.definition = definition;
            this.zone = zone;
            this.tiledMap = tiledMap;
            this.tilemapPath = tilemapPath;
        }

        public ZoneDefinition getDefinition() {
            return definition;
        }

        public TmxWorldLoader.LoadedZone getZone() {
            return zone;
        }

        public TiledMap getTiledMap() {
            return tiledMap;
        }

        public String getTilemapPath() {
            return tilemapPath;
        }
    }
}
