package com.rogueforge.game.engine.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.data.ZoneDefinition;
import java.util.Random;

/**
 * Expands a handcrafted zone template into a large frontier playfield with a
 * safe starter radius, harvest nodes, and future base-claim sites.
 */
public class FrontierZoneGenerator {
    private static final int ENEMY_SPAWN_COUNT = 80;
    private static final int DEFAULT_WIDTH_TILES = 512;
    private static final int DEFAULT_HEIGHT_TILES = 512;
    private static final int DEFAULT_SAFE_RADIUS_TILES = 24;
    private static final int RESOURCE_CLUSTER_COUNT = 28;
    private static final int BASE_SITE_COUNT = 6;
    private static final int LANDMARK_PREFAB_COUNT = 5;
    private static final float STARTER_CORRIDOR_LENGTH_TILES = 18f;
    private static final float STARTER_CORRIDOR_HALF_HEIGHT_TILES = 3.5f;

    public TmxWorldLoader.LoadedZone generate(ZoneDefinition definition, TmxWorldLoader.LoadedZone template, long worldSeed) {
        if (definition == null || template == null || !definition.isExpansiveFrontier()) {
            return template;
        }

        int targetWidthTiles = Math.max(template.mapWidthTiles, defaultIfZero(definition.getExpansiveWidthTiles(), DEFAULT_WIDTH_TILES));
        int targetHeightTiles = Math.max(template.mapHeightTiles, defaultIfZero(definition.getExpansiveHeightTiles(), DEFAULT_HEIGHT_TILES));
        template.mapWidthTiles = targetWidthTiles;
        template.mapHeightTiles = targetHeightTiles;
        template.pixelWidth = targetWidthTiles * template.tileWidth;
        template.pixelHeight = targetHeightTiles * template.tileHeight;

        Vector2 safeAnchor = resolveSafeAnchor(template);
        template.safeCenter = new Vector2(safeAnchor);
        template.safeRadius = defaultIfZero(definition.getStarterSafeRadiusTiles(), DEFAULT_SAFE_RADIUS_TILES) * template.tileWidth;
        template.safeZone = false;

        FrontierTerrainSampler terrainSampler = new FrontierTerrainSampler(worldSeed);
        FrontierBiomeCatalog biomeCatalog = new FrontierBiomeCatalog();
        FrontierLandmarkPrefabCatalog landmarkCatalog = new FrontierLandmarkPrefabCatalog();
        populateEnemySpawns(template, definition, worldSeed, terrainSampler);
        populateFrontierFeatures(template, definition, worldSeed, terrainSampler, biomeCatalog, landmarkCatalog);
        return template;
    }

    private int defaultIfZero(int value, int fallback) {
        return value > 0 ? value : fallback;
    }

    private Vector2 resolveSafeAnchor(TmxWorldLoader.LoadedZone zone) {
        if (zone.playerSpawns.containsKey("from_town")) {
            return zone.playerSpawns.get("from_town");
        }
        if (zone.playerSpawns.containsKey("town_square")) {
            return zone.playerSpawns.get("town_square");
        }
        if (!zone.playerSpawns.isEmpty()) {
            return zone.playerSpawns.values().next();
        }
        return new Vector2(zone.pixelWidth * 0.5f, zone.pixelHeight * 0.5f);
    }

    private void populateEnemySpawns(
        TmxWorldLoader.LoadedZone zone,
        ZoneDefinition definition,
        long worldSeed,
        FrontierTerrainSampler terrainSampler
    ) {
        int authoredSpawnCount = zone.enemySpawns.size;
        Random random = new Random(mixSeed(worldSeed, definition.getId().hashCode(), 11));
        int proceduralSpawnCount = Math.max(0, ENEMY_SPAWN_COUNT - authoredSpawnCount);
        for (int i = 0; i < proceduralSpawnCount; i++) {
            Vector2 spawn = findPlacementPoint(zone, random, terrainSampler, PlacementKind.ENEMY, null, 0f, 0f);
            zone.enemySpawns.add(spawn);
        }
    }

    private void populateFrontierFeatures(
        TmxWorldLoader.LoadedZone zone,
        ZoneDefinition definition,
        long worldSeed,
        FrontierTerrainSampler terrainSampler,
        FrontierBiomeCatalog biomeCatalog,
        FrontierLandmarkPrefabCatalog landmarkCatalog
    ) {
        Random landmarkRandom = new Random(mixSeed(worldSeed, definition.getId().hashCode(), 17));
        Random resourceRandom = new Random(mixSeed(worldSeed, definition.getId().hashCode(), 23));
        Random siteRandom = new Random(mixSeed(worldSeed, definition.getId().hashCode(), 37));

        addLandmarkPrefabs(zone, landmarkRandom, terrainSampler, biomeCatalog, landmarkCatalog);
        addResourceNodes(zone, resourceRandom, terrainSampler, biomeCatalog, "scrap_alloy", 2, "Salvage Wreck", RESOURCE_CLUSTER_COUNT / 2);
        addResourceNodes(zone, resourceRandom, terrainSampler, biomeCatalog, "slime_resin", 2, "Resin Patch", RESOURCE_CLUSTER_COUNT / 3);
        addResourceNodes(zone, resourceRandom, terrainSampler, biomeCatalog, "bone_fiber", 1, "Fiber Grove", RESOURCE_CLUSTER_COUNT / 6);
        addBaseSites(zone, siteRandom, terrainSampler, biomeCatalog);
    }

    private void addLandmarkPrefabs(
        TmxWorldLoader.LoadedZone zone,
        Random random,
        FrontierTerrainSampler terrainSampler,
        FrontierBiomeCatalog biomeCatalog,
        FrontierLandmarkPrefabCatalog landmarkCatalog
    ) {
        if (landmarkCatalog == null) {
            return;
        }
        int placed = 0;
        for (FrontierLandmarkPrefabDefinition prefab : landmarkCatalog.getAll()) {
            if (placed >= LANDMARK_PREFAB_COUNT) {
                break;
            }
            Rectangle bounds = randomFrontierRect(
                zone,
                random,
                terrainSampler,
                PlacementKind.LANDMARK_PREFAB,
                prefab.getId(),
                prefab.getWidthTiles() * zone.tileWidth,
                prefab.getHeightTiles() * zone.tileHeight
            );
            Vector2 center = new Vector2(bounds.x + bounds.width * 0.5f, bounds.y + bounds.height * 0.5f);
            FrontierTerrainSampler.TerrainType terrainType = terrainSampler.sampleWorld(
                center.x,
                center.y,
                zone.tileWidth,
                zone.tileHeight
            ).type;
            if (!prefab.allows(terrainType)) {
                continue;
            }
            FrontierBiomeDefinition biomeDefinition = biomeCatalog.resolve(terrainType);
            TmxWorldLoader.Feature feature = new TmxWorldLoader.Feature();
            feature.id = "landmark_" + prefab.getId();
            feature.kind = "landmark_prefab";
            feature.label = prefab.getLabel();
            feature.bounds = bounds;
            feature.interactionType = "inspect_landmark";
            feature.interactionMessage = prefab.getInteractionMessage();
            feature.blockedMessage = prefab.getInteractionMessage();
            feature.blocksMovement = false;
            feature.persistentStateId = feature.id;
            feature.terrainType = terrainType.name();
            feature.biomeId = biomeDefinition.getId();
            feature.biomeGroundAssetFolder = biomeDefinition.getGroundAssetFolder();
            feature.biomeObjectAssetFolder = prefab.getObjectAssetFolder();
            feature.prefabId = prefab.getId();
            feature.prefabCategory = prefab.getCategory();
            zone.features.add(feature);
            placed++;
        }
    }

    private void addResourceNodes(
        TmxWorldLoader.LoadedZone zone,
        Random random,
        FrontierTerrainSampler terrainSampler,
        FrontierBiomeCatalog biomeCatalog,
        String resourceId,
        int amount,
        String label,
        int count
    ) {
        for (int i = 0; i < count; i++) {
            Rectangle bounds = randomFrontierRect(zone, random, terrainSampler, PlacementKind.RESOURCE, resourceId, 56f, 56f);
            FrontierTerrainSampler.TerrainType terrainType = terrainSampler.sampleWorld(
                bounds.x + bounds.width * 0.5f,
                bounds.y + bounds.height * 0.5f,
                zone.tileWidth,
                zone.tileHeight
            ).type;
            FrontierBiomeDefinition biomeDefinition = biomeCatalog.resolve(terrainType);
            TmxWorldLoader.Feature feature = new TmxWorldLoader.Feature();
            feature.id = resourceId + "_node_" + i;
            feature.kind = "resource_node";
            feature.label = label;
            feature.bounds = bounds;
            feature.interactionType = "harvest_resource";
            feature.interactionMessage = "Your crew salvages " + amount + " " + resourceId.replace('_', ' ') + ".";
            feature.blockedMessage = "There is nothing left to harvest here.";
            feature.blocksMovement = true;
            feature.resourceId = resourceId;
            feature.resourceAmount = amount;
            feature.persistentStateId = feature.id;
            feature.terrainType = terrainType.name();
            feature.biomeId = biomeDefinition.getId();
            feature.biomeGroundAssetFolder = biomeDefinition.getGroundAssetFolder();
            feature.biomeObjectAssetFolder = biomeDefinition.getObjectAssetFolder();
            zone.features.add(feature);
        }
    }

    private void addBaseSites(
        TmxWorldLoader.LoadedZone zone,
        Random random,
        FrontierTerrainSampler terrainSampler,
        FrontierBiomeCatalog biomeCatalog
    ) {
        for (int i = 0; i < BASE_SITE_COUNT; i++) {
            Rectangle bounds = randomFrontierRect(zone, random, terrainSampler, PlacementKind.BASE_SITE, null, 120f, 120f);
            FrontierTerrainSampler.TerrainType terrainType = terrainSampler.sampleWorld(
                bounds.x + bounds.width * 0.5f,
                bounds.y + bounds.height * 0.5f,
                zone.tileWidth,
                zone.tileHeight
            ).type;
            FrontierBiomeDefinition biomeDefinition = biomeCatalog.resolve(terrainType);
            TmxWorldLoader.Feature feature = new TmxWorldLoader.Feature();
            feature.id = "frontier_base_site_" + i;
            feature.kind = "base_site";
            feature.label = "Open Ground";
            feature.bounds = bounds;
            feature.interactionType = "claim_outpost_site";
            feature.interactionMessage = "This clearing is large enough for a future outpost, defense line, or reserve bot post.";
            feature.blockedMessage = "This site has already been logged for expansion.";
            feature.blocksMovement = false;
            feature.persistentStateId = feature.id;
            feature.terrainType = terrainType.name();
            feature.biomeId = biomeDefinition.getId();
            feature.biomeGroundAssetFolder = biomeDefinition.getGroundAssetFolder();
            feature.biomeObjectAssetFolder = biomeDefinition.getObjectAssetFolder();
            zone.features.add(feature);
        }
    }

    private Rectangle randomFrontierRect(
        TmxWorldLoader.LoadedZone zone,
        Random random,
        FrontierTerrainSampler terrainSampler,
        PlacementKind kind,
        String resourceId,
        float width,
        float height
    ) {
        Rectangle bestRect = null;
        float bestScore = Float.NEGATIVE_INFINITY;
        int attempts = 0;
        while (attempts++ < 120) {
            float x = zone.tileWidth * 6f + random.nextFloat() * Math.max(zone.tileWidth, zone.pixelWidth - width - zone.tileWidth * 12f);
            float y = zone.tileHeight * 6f + random.nextFloat() * Math.max(zone.tileHeight, zone.pixelHeight - height - zone.tileHeight * 12f);
            Rectangle rect = new Rectangle(x, y, width, height);
            Vector2 center = new Vector2(rect.x + rect.width * 0.5f, rect.y + rect.height * 0.5f);
            if (zone.isSafeAt(center)) {
                continue;
            }
            if (overlapsExistingCollision(zone, rect)) {
                continue;
            }
            if (kind != PlacementKind.ENEMY && intersectsStarterCorridor(zone, rect)) {
                continue;
            }
            float score = placementScore(zone, terrainSampler, center, kind, resourceId);
            if (score > bestScore) {
                bestScore = score;
                bestRect = rect;
            }
            if (score >= 1.25f) {
                return rect;
            }
        }
        if (bestRect != null) {
            return bestRect;
        }
        return new Rectangle(zone.safeCenter.x + zone.safeRadius + 120f, zone.safeCenter.y + zone.safeRadius + 120f, width, height);
    }

    private Vector2 findPlacementPoint(
        TmxWorldLoader.LoadedZone zone,
        Random random,
        FrontierTerrainSampler terrainSampler,
        PlacementKind kind,
        String resourceId,
        float width,
        float height
    ) {
        Rectangle rect = randomFrontierRect(
            zone,
            random,
            terrainSampler,
            kind,
            resourceId,
            Math.max(width, zone.tileWidth),
            Math.max(height, zone.tileHeight)
        );
        return new Vector2(rect.x + rect.width * 0.5f, rect.y + rect.height * 0.5f);
    }

    private float placementScore(
        TmxWorldLoader.LoadedZone zone,
        FrontierTerrainSampler terrainSampler,
        Vector2 center,
        PlacementKind kind,
        String resourceId
    ) {
        FrontierTerrainSampler.TerrainSample sample = terrainSampler.sampleWorld(center.x, center.y, zone.tileWidth, zone.tileHeight);
        float distanceFromSafeEdge = center.dst(zone.safeCenter) - zone.safeRadius;
        float distanceScore = Math.min(1.4f, Math.max(0f, distanceFromSafeEdge / (zone.tileWidth * 48f)));
        switch (kind) {
            case ENEMY:
                return (terrainSampler.isGoodEnemyTerrain(sample.type) ? 1.2f : 0.25f)
                    + distanceScore
                    + sample.ruggedness * 0.35f;
            case RESOURCE:
                return (terrainSampler.isPreferredResourceTerrain(resourceId, sample.type) ? 1.5f : 0.15f)
                    + distanceScore * 0.55f;
            case BASE_SITE:
                return (terrainSampler.isBuildFriendly(sample.type) ? 1.35f : -0.4f)
                    + distanceScore * 0.45f
                    - Math.max(0f, sample.moisture) * 0.2f;
            case LANDMARK_PREFAB:
                return landmarkScore(sample, resourceId) + distanceScore * 0.35f;
            default:
                return 0f;
        }
    }

    private float landmarkScore(FrontierTerrainSampler.TerrainSample sample, String prefabId) {
        if (prefabId == null || prefabId.isEmpty()) {
            return 0f;
        }
        switch (prefabId) {
            case "village_small_a":
                return sample.type == FrontierTerrainSampler.TerrainType.MEADOW
                    || sample.type == FrontierTerrainSampler.TerrainType.GROVE ? 1.4f : -0.6f;
            case "ruined_village_a":
                return sample.type == FrontierTerrainSampler.TerrainType.RUIN_FIELD
                    || sample.type == FrontierTerrainSampler.TerrainType.STONE_FLATS ? 1.45f : -0.7f;
            case "trader_camp_a":
                return sample.type == FrontierTerrainSampler.TerrainType.MEADOW
                    || sample.type == FrontierTerrainSampler.TerrainType.SCRUB
                    || sample.type == FrontierTerrainSampler.TerrainType.STONE_FLATS ? 1.25f : -0.45f;
            case "watchpost_a":
                return sample.type == FrontierTerrainSampler.TerrainType.STONE_FLATS
                    || sample.type == FrontierTerrainSampler.TerrainType.SCRUB ? 1.3f : -0.5f;
            case "shrine_a":
                return sample.type == FrontierTerrainSampler.TerrainType.GROVE
                    || sample.type == FrontierTerrainSampler.TerrainType.MARSH
                    || sample.type == FrontierTerrainSampler.TerrainType.MEADOW ? 1.2f : -0.35f;
            default:
                return 0.2f;
        }
    }

    private boolean overlapsExistingCollision(TmxWorldLoader.LoadedZone zone, Rectangle rect) {
        for (Rectangle collision : zone.collisions) {
            if (rect.overlaps(collision)) {
                return true;
            }
        }
        for (TmxWorldLoader.Feature feature : zone.features) {
            if (feature.bounds != null && rect.overlaps(feature.bounds)) {
                return true;
            }
        }
        return false;
    }

    private boolean intersectsStarterCorridor(TmxWorldLoader.LoadedZone zone, Rectangle rect) {
        Rectangle corridor = starterCorridor(zone);
        return corridor != null && rect.overlaps(corridor);
    }

    private Rectangle starterCorridor(TmxWorldLoader.LoadedZone zone) {
        if (zone == null || zone.safeCenter == null || zone.tileWidth <= 0 || zone.tileHeight <= 0) {
            return null;
        }
        float corridorStartX = zone.safeCenter.x + zone.safeRadius - zone.tileWidth;
        float corridorWidth = zone.tileWidth * STARTER_CORRIDOR_LENGTH_TILES;
        float corridorY = zone.safeCenter.y - zone.tileHeight * STARTER_CORRIDOR_HALF_HEIGHT_TILES;
        float corridorHeight = zone.tileHeight * STARTER_CORRIDOR_HALF_HEIGHT_TILES * 2f;
        return new Rectangle(corridorStartX, corridorY, corridorWidth, corridorHeight);
    }

    private long mixSeed(long worldSeed, int zoneHash, int salt) {
        long mixed = worldSeed ^ (((long) zoneHash) << 32);
        mixed ^= salt * 0x9E3779B97F4A7C15L;
        mixed ^= (mixed >>> 30);
        mixed *= 0xBF58476D1CE4E5B9L;
        mixed ^= (mixed >>> 27);
        mixed *= 0x94D049BB133111EBL;
        mixed ^= (mixed >>> 31);
        return mixed;
    }

    private enum PlacementKind {
        ENEMY,
        RESOURCE,
        BASE_SITE,
        LANDMARK_PREFAB
    }
}
