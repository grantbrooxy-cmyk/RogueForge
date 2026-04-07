package com.rogueforge.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.data.ZoneDefinition;
import com.rogueforge.game.engine.world.TmxWorldLoader;
import com.rogueforge.game.engine.world.WorldGenerator;
import com.rogueforge.game.support.GdxTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldGeneratorTest {

    @BeforeAll
    static void bootGdx() {
        GdxTestSupport.init();
    }

    @Test
    void expansiveFrontierGetsProceduralOverlayFeatures() {
        ZoneDefinition[] definitions = new Json().fromJson(
            ZoneDefinition[].class,
            Gdx.files.internal("data/zones.json").readString()
        );
        ZoneDefinition verdantFields = null;
        for (ZoneDefinition definition : definitions) {
            if ("verdant_fields".equals(definition.getId())) {
                verdantFields = definition;
                break;
            }
        }
        assertNotNull(verdantFields);

        TmxWorldLoader loader = new TmxWorldLoader();
        TmxWorldLoader.LoadedZone zone = loader.load(verdantFields);
        zone.safeCenter = zone.playerSpawns.get("from_town");
        zone.safeRadius = zone.tileWidth * 24f;

        WorldGenerator generator = new WorldGenerator();
        generator.decorate(verdantFields, zone, 424242L);

        boolean foundTreeCluster = false;
        boolean foundOreVein = false;
        boolean foundHiddenCave = false;
        for (TmxWorldLoader.Feature feature : zone.features) {
            if ("tree_cluster".equals(feature.kind)) {
                foundTreeCluster = true;
            }
            if ("ore_vein".equals(feature.kind)) {
                foundOreVein = true;
            }
            if ("hidden_cave".equals(feature.kind)) {
                foundHiddenCave = true;
            }
        }
        assertTrue(foundTreeCluster);
        assertTrue(foundOreVein);
        assertTrue(foundHiddenCave);
    }
}
