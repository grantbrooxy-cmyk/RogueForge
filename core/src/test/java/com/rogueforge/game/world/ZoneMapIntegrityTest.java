package com.rogueforge.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.data.ZoneDefinition;
import com.rogueforge.game.support.GdxTestSupport;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoneMapIntegrityTest {

    @BeforeAll
    static void bootGdx() {
        GdxTestSupport.init();
    }

    @Test
    void everyZoneHasALoadableMapAndValidDoorTargets() {
        ZoneDefinition[] definitions = new Json().fromJson(
            ZoneDefinition[].class,
            Gdx.files.internal("data/zones.json").readString()
        );

        assertNotNull(definitions);
        assertTrue(definitions.length >= 16, "Expected the full zone list to be present.");

        TmxWorldLoader loader = new TmxWorldLoader();
        Map<String, TmxWorldLoader.LoadedZone> loadedZones = new HashMap<>();

        for (ZoneDefinition definition : definitions) {
            FileHandle mapFile = Gdx.files.internal(definition.getTilemapPath());
            assertTrue(
                mapFile.exists(),
                "Missing map file for zone " + definition.getId() + ": " + definition.getTilemapPath()
            );

            TmxWorldLoader.LoadedZone loaded = loader.load(definition);
            assertNotNull(loaded, "Failed to load TMX metadata for zone " + definition.getId());
            assertTrue(loaded.pixelWidth > 0, "Zone " + definition.getId() + " should have positive width.");
            assertTrue(loaded.pixelHeight > 0, "Zone " + definition.getId() + " should have positive height.");
            assertFalse(loaded.playerSpawns.isEmpty(), "Zone " + definition.getId() + " should define player spawns.");

            loadedZones.put(definition.getId(), loaded);
        }

        for (TmxWorldLoader.LoadedZone zone : loadedZones.values()) {
            for (TmxWorldLoader.Door door : zone.doors) {
                if (door.targetZoneId == null || door.targetZoneId.isEmpty()) {
                    continue;
                }

                TmxWorldLoader.LoadedZone targetZone = loadedZones.get(door.targetZoneId);
                assertNotNull(
                    targetZone,
                    "Door " + zone.id + ":" + door.id + " targets unknown zone " + door.targetZoneId
                );

                if (door.targetSpawnId != null && !door.targetSpawnId.isEmpty()) {
                    assertTrue(
                        targetZone.playerSpawns.containsKey(door.targetSpawnId),
                        "Door " + zone.id + ":" + door.id + " targets missing spawn "
                            + door.targetZoneId + ":" + door.targetSpawnId
                    );
                }
            }
        }
    }
}
