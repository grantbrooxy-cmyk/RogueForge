package com.rogueforge.game.engine.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.data.ZoneDefinition;
import java.util.Random;

/**
 * Adds seeded procedural overlay content on top of authored TMX zones.
 */
public class WorldGenerator {
    private static final int TREE_CLUSTER_COUNT = 18;
    private static final int ORE_CLUSTER_COUNT = 10;
    private static final int HIDDEN_CAVE_COUNT = 4;

    public TmxWorldLoader.LoadedZone decorate(ZoneDefinition definition, TmxWorldLoader.LoadedZone zone, long worldSeed) {
        if (definition == null || zone == null || !definition.isExpansiveFrontier()) {
            return zone;
        }
        FrontierTerrainSampler terrainSampler = new FrontierTerrainSampler(worldSeed);
        FrontierBiomeCatalog biomeCatalog = new FrontierBiomeCatalog();
        Random treeRandom = new Random(mixSeed(worldSeed, definition.getId(), 71));
        Random oreRandom = new Random(mixSeed(worldSeed, definition.getId(), 89));
        Random caveRandom = new Random(mixSeed(worldSeed, definition.getId(), 113));

        addTreeClusters(zone, treeRandom, terrainSampler, biomeCatalog);
        addOreVeins(zone, oreRandom, terrainSampler, biomeCatalog);
        addHiddenCaves(zone, caveRandom, terrainSampler, biomeCatalog);
        return zone;
    }

    private void addTreeClusters(TmxWorldLoader.LoadedZone zone, Random random,
                                 FrontierTerrainSampler terrainSampler, FrontierBiomeCatalog biomeCatalog) {
        for (int i = 0; i < TREE_CLUSTER_COUNT; i++) {
            Rectangle bounds = findRect(zone, random, terrainSampler, 88f, 88f,
                sample -> sample.type == FrontierTerrainSampler.TerrainType.GROVE
                    || sample.type == FrontierTerrainSampler.TerrainType.MEADOW);
            addOverlayFeature(zone, bounds, terrainSampler, biomeCatalog,
                "tree_cluster_" + i, "tree_cluster", "Wild Thicket",
                "cut_vines", "Dense growth blocks the cleanest path forward.",
                "The thicket has already been cleared.", true);
        }
    }

    private void addOreVeins(TmxWorldLoader.LoadedZone zone, Random random,
                             FrontierTerrainSampler terrainSampler, FrontierBiomeCatalog biomeCatalog) {
        for (int i = 0; i < ORE_CLUSTER_COUNT; i++) {
            Rectangle bounds = findRect(zone, random, terrainSampler, 64f, 64f,
                sample -> sample.type == FrontierTerrainSampler.TerrainType.STONE_FLATS
                    || sample.type == FrontierTerrainSampler.TerrainType.RUIN_FIELD);
            TmxWorldLoader.Feature feature = addOverlayFeature(zone, bounds, terrainSampler, biomeCatalog,
                "ore_vein_" + i, "ore_vein", "Ore Vein",
                "mine_ore", "A compact seam of forge-grade ore runs through this outcrop.",
                "Only fractured stone remains.", true);
            feature.resourceId = "ore";
            feature.resourceAmount = 3;
        }
    }

    private void addHiddenCaves(TmxWorldLoader.LoadedZone zone, Random random,
                                FrontierTerrainSampler terrainSampler, FrontierBiomeCatalog biomeCatalog) {
        for (int i = 0; i < HIDDEN_CAVE_COUNT; i++) {
            Rectangle bounds = findRect(zone, random, terrainSampler, 132f, 132f,
                sample -> sample.type == FrontierTerrainSampler.TerrainType.RUIN_FIELD
                    || sample.type == FrontierTerrainSampler.TerrainType.SCRUB
                    || sample.ruggedness > 0.2f);
            addOverlayFeature(zone, bounds, terrainSampler, biomeCatalog,
                "hidden_cave_" + i, "hidden_cave", "Collapsed Hollow",
                "hack_ruins", "There may be a sealed cave pocket behind this collapsed ruin face.",
                "The collapse has already been pried open.", false);
        }
    }

    private TmxWorldLoader.Feature addOverlayFeature(
        TmxWorldLoader.LoadedZone zone,
        Rectangle bounds,
        FrontierTerrainSampler terrainSampler,
        FrontierBiomeCatalog biomeCatalog,
        String id,
        String kind,
        String label,
        String interactionType,
        String interactionMessage,
        String blockedMessage,
        boolean blocksMovement
    ) {
        Vector2 center = new Vector2(bounds.x + bounds.width * 0.5f, bounds.y + bounds.height * 0.5f);
        FrontierTerrainSampler.TerrainSample sample = terrainSampler.sampleWorld(center.x, center.y, zone.tileWidth, zone.tileHeight);
        FrontierBiomeDefinition biome = biomeCatalog.resolve(sample.type);
        TmxWorldLoader.Feature feature = new TmxWorldLoader.Feature();
        feature.id = id;
        feature.kind = kind;
        feature.label = label;
        feature.bounds = bounds;
        feature.interactionType = interactionType;
        feature.interactionMessage = interactionMessage;
        feature.blockedMessage = blockedMessage;
        feature.blocksMovement = blocksMovement;
        feature.persistentStateId = id;
        feature.terrainType = sample.type.name();
        feature.biomeId = biome.getId();
        feature.biomeGroundAssetFolder = biome.getGroundAssetFolder();
        feature.biomeObjectAssetFolder = biome.getObjectAssetFolder();
        zone.features.add(feature);
        return feature;
    }

    private Rectangle findRect(TmxWorldLoader.LoadedZone zone, Random random, FrontierTerrainSampler terrainSampler,
                               float width, float height, TerrainPredicate predicate) {
        Rectangle best = new Rectangle(zone.tileWidth * 8f, zone.tileHeight * 8f, width, height);
        float bestScore = Float.NEGATIVE_INFINITY;
        for (int attempt = 0; attempt < 80; attempt++) {
            float x = zone.tileWidth * 6f + random.nextFloat() * Math.max(zone.tileWidth, zone.pixelWidth - width - zone.tileWidth * 12f);
            float y = zone.tileHeight * 6f + random.nextFloat() * Math.max(zone.tileHeight, zone.pixelHeight - height - zone.tileHeight * 12f);
            Rectangle candidate = new Rectangle(x, y, width, height);
            Vector2 center = new Vector2(candidate.x + candidate.width * 0.5f, candidate.y + candidate.height * 0.5f);
            if (zone.isSafeAt(center) || overlapsExisting(zone, candidate)) {
                continue;
            }
            FrontierTerrainSampler.TerrainSample sample = terrainSampler.sampleWorld(center.x, center.y, zone.tileWidth, zone.tileHeight);
            float score = predicate.allows(sample) ? 1f : -1f;
            score += sample.moisture * 0.15f;
            score += sample.ruggedness * 0.1f;
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
            if (predicate.allows(sample)) {
                return candidate;
            }
        }
        return best;
    }

    private boolean overlapsExisting(TmxWorldLoader.LoadedZone zone, Rectangle candidate) {
        for (Rectangle collision : zone.collisions) {
            if (collision != null && collision.overlaps(candidate)) {
                return true;
            }
        }
        for (TmxWorldLoader.Feature feature : zone.features) {
            if (feature != null && feature.bounds != null && feature.bounds.overlaps(candidate)) {
                return true;
            }
        }
        return false;
    }

    private long mixSeed(long worldSeed, String id, int salt) {
        long value = worldSeed != 0L ? worldSeed : 1L;
        value ^= (long) (id != null ? id.hashCode() : 0) * 0x9E3779B97F4A7C15L;
        value ^= (long) salt * 0xC2B2AE3D27D4EB4FL;
        return value;
    }

    @FunctionalInterface
    private interface TerrainPredicate {
        boolean allows(FrontierTerrainSampler.TerrainSample sample);
    }
}
