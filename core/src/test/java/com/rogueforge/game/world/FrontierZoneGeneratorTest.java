package com.rogueforge.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.data.ZoneDefinition;
import com.rogueforge.game.engine.world.FrontierTerrainSampler;
import com.rogueforge.game.engine.world.FrontierBiomeCatalog;
import com.rogueforge.game.engine.world.FrontierZoneGenerator;
import com.rogueforge.game.engine.world.TmxWorldLoader;
import com.rogueforge.game.support.GdxTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontierZoneGeneratorTest {

    @BeforeAll
    static void bootGdx() {
        GdxTestSupport.init();
    }

    @Test
    void verdantFieldsExpandsIntoLargeFrontierWithStarterSafeRadiusAndResources() {
        ZoneDefinition[] definitions = new Json().fromJson(
            ZoneDefinition[].class,
            Gdx.files.internal("data/zones.json").readString()
        );
        assertNotNull(definitions);

        ZoneDefinition verdantFields = null;
        for (ZoneDefinition definition : definitions) {
            if ("verdant_fields".equals(definition.getId())) {
                verdantFields = definition;
                break;
            }
        }
        assertNotNull(verdantFields);
        assertTrue(verdantFields.isExpansiveFrontier());

        TmxWorldLoader loader = new TmxWorldLoader();
        TmxWorldLoader.LoadedZone template = loader.load(verdantFields);
        int authoredEnemySpawns = template.enemySpawns.size;
        FrontierZoneGenerator generator = new FrontierZoneGenerator();
        TmxWorldLoader.LoadedZone generated = generator.generate(verdantFields, template, 424242L);
        FrontierTerrainSampler terrainSampler = new FrontierTerrainSampler(424242L);
        FrontierBiomeCatalog biomeCatalog = new FrontierBiomeCatalog();

        assertTrue(generated.mapWidthTiles >= 512);
        assertTrue(generated.mapHeightTiles >= 512);
        assertNotNull(generated.safeCenter);
        assertTrue(generated.safeRadius > 0f);
        assertTrue(generated.enemySpawns.size >= 40);
        assertTrue(generated.enemySpawns.size > authoredEnemySpawns, "Expansive frontier should keep authored encounters and add procedural ones.");

        boolean foundResourceNode = false;
        boolean foundBaseSite = false;
        com.badlogic.gdx.math.Rectangle starterCorridor = new com.badlogic.gdx.math.Rectangle(
            generated.safeCenter.x + generated.safeRadius - generated.tileWidth,
            generated.safeCenter.y - generated.tileHeight * 3.5f,
            generated.tileWidth * 18f,
            generated.tileHeight * 7f
        );
        for (TmxWorldLoader.Feature feature : generated.features) {
            if ("harvest_resource".equals(feature.interactionType)) {
                foundResourceNode = true;
                assertNotNull(feature.resourceId);
                assertNotNull(feature.terrainType);
                assertNotNull(feature.biomeId);
                assertNotNull(feature.biomeGroundAssetFolder);
                assertNotNull(feature.biomeObjectAssetFolder);
                Vector2 center = new Vector2(feature.bounds.x + feature.bounds.width * 0.5f, feature.bounds.y + feature.bounds.height * 0.5f);
                assertFalse(generated.isSafeAt(center), "Harvest nodes should stay outside the starter safe zone.");
                assertFalse(feature.bounds.overlaps(starterCorridor), "Starter east corridor should stay clear of procedural blockers.");
                FrontierTerrainSampler.TerrainType terrainType = terrainSampler.sampleWorld(center.x, center.y, generated.tileWidth, generated.tileHeight).type;
                assertEquals(terrainType.name(), feature.terrainType);
                assertEquals(biomeCatalog.resolve(terrainType).getId(), feature.biomeId);
                assertTrue(
                    terrainSampler.isPreferredResourceTerrain(feature.resourceId, terrainType),
                    "Resource nodes should land in terrain that matches their seeded biome affinity."
                );
            }
            if ("claim_outpost_site".equals(feature.interactionType)) {
                foundBaseSite = true;
                assertNotNull(feature.terrainType);
                assertNotNull(feature.biomeId);
                assertNotNull(feature.biomeGroundAssetFolder);
                assertNotNull(feature.biomeObjectAssetFolder);
                Vector2 center = new Vector2(feature.bounds.x + feature.bounds.width * 0.5f, feature.bounds.y + feature.bounds.height * 0.5f);
                assertFalse(generated.isSafeAt(center), "Base claim sites should be outside the starter safe zone.");
                assertFalse(feature.bounds.overlaps(starterCorridor), "Starter east corridor should stay clear of procedural build-site markers.");
                FrontierTerrainSampler.TerrainType terrainType = terrainSampler.sampleWorld(center.x, center.y, generated.tileWidth, generated.tileHeight).type;
                assertEquals(terrainType.name(), feature.terrainType);
                assertEquals(biomeCatalog.resolve(terrainType).getId(), feature.biomeId);
                assertTrue(
                    terrainSampler.isBuildFriendly(terrainType),
                    "Base claim sites should avoid hostile or waterlogged terrain."
                );
            }
        }

        assertTrue(foundResourceNode, "Expected procedural resource nodes in the frontier.");
        assertTrue(foundBaseSite, "Expected future base claim sites in the frontier.");
    }

    @Test
    void frontierGenerationUsesSeedForDeterministicLayout() {
        ZoneDefinition[] definitions = new Json().fromJson(
            ZoneDefinition[].class,
            Gdx.files.internal("data/zones.json").readString()
        );
        assertNotNull(definitions);

        ZoneDefinition verdantFields = null;
        for (ZoneDefinition definition : definitions) {
            if ("verdant_fields".equals(definition.getId())) {
                verdantFields = definition;
                break;
            }
        }
        assertNotNull(verdantFields);

        TmxWorldLoader loader = new TmxWorldLoader();
        FrontierZoneGenerator generator = new FrontierZoneGenerator();

        TmxWorldLoader.LoadedZone seedA1 = generator.generate(verdantFields, loader.load(verdantFields), 111L);
        TmxWorldLoader.LoadedZone seedA2 = generator.generate(verdantFields, loader.load(verdantFields), 111L);
        TmxWorldLoader.LoadedZone seedB = generator.generate(verdantFields, loader.load(verdantFields), 222L);

        assertEquals(seedA1.enemySpawns.first().x, seedA2.enemySpawns.first().x, 0.001f);
        assertEquals(seedA1.enemySpawns.first().y, seedA2.enemySpawns.first().y, 0.001f);
        assertEquals(seedA1.features.first().bounds.x, seedA2.features.first().bounds.x, 0.001f);
        assertEquals(seedA1.features.first().bounds.y, seedA2.features.first().bounds.y, 0.001f);
        assertEquals(seedA1.features.first().terrainType, seedA2.features.first().terrainType);
        Vector2 proceduralSpawnA = seedA1.enemySpawns.peek();
        Vector2 proceduralSpawnB = seedB.enemySpawns.peek();
        assertFalse(
            Math.abs(proceduralSpawnA.x - proceduralSpawnB.x) < 0.001f
                && Math.abs(proceduralSpawnA.y - proceduralSpawnB.y) < 0.001f,
            "Different world seeds should produce different procedural frontier layouts."
        );
        assertNotEquals("", seedA1.features.first().terrainType);
    }
}
