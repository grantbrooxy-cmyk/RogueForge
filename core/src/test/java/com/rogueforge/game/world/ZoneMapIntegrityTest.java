package com.rogueforge.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.data.ZoneDefinition;
import com.rogueforge.game.engine.world.TmxWorldLoader;
import com.rogueforge.game.support.GdxTestSupport;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoneMapIntegrityTest {
    private static final float PLAYER_SIZE = 56f;
    private static final float DOOR_INTERACTION_RANGE = 42f;
    private static final float SEARCH_STEP = 8f;

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
            assertFalse(
                mapFile.readString().contains("name=\"questFlag\""),
                "Map " + definition.getId() + " still uses legacy questFlag TMX properties. Use world-flag properties instead."
            );

            TmxWorldLoader.LoadedZone loaded = loader.load(definition);
            assertNotNull(loaded, "Failed to load TMX metadata for zone " + definition.getId());
            assertTrue(loaded.pixelWidth > 0, "Zone " + definition.getId() + " should have positive width.");
            assertTrue(loaded.pixelHeight > 0, "Zone " + definition.getId() + " should have positive height.");
            assertFalse(loaded.playerSpawns.isEmpty(), "Zone " + definition.getId() + " should define player spawns.");
            if ("sunken_abyss".equals(definition.getId()) || "volcanic_core".equals(definition.getId())) {
                assertTrue(
                    loaded.playerSpawns.size >= 2,
                    "Zone " + definition.getId() + " should define both entry and return fallback spawns."
                );
            }

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

                assertTrue(
                    hasAccessibleDoorApproach(zone, door),
                    "Door " + zone.id + ":" + door.id + " has no reachable standing position for the player."
                );
            }
        }
    }

    private boolean hasAccessibleDoorApproach(TmxWorldLoader.LoadedZone zone, TmxWorldLoader.Door door) {
        float minX = Math.max(0f, door.bounds.x - DOOR_INTERACTION_RANGE - PLAYER_SIZE);
        float maxX = Math.min(zone.pixelWidth, door.bounds.x + door.bounds.width + DOOR_INTERACTION_RANGE + PLAYER_SIZE);
        float minY = Math.max(0f, door.bounds.y - DOOR_INTERACTION_RANGE - PLAYER_SIZE);
        float maxY = Math.min(zone.pixelHeight, door.bounds.y + door.bounds.height + DOOR_INTERACTION_RANGE + PLAYER_SIZE);

        for (float y = minY; y <= maxY; y += SEARCH_STEP) {
            for (float x = minX; x <= maxX; x += SEARCH_STEP) {
                if (distanceToRect(x, y, door.bounds) > DOOR_INTERACTION_RANGE) {
                    continue;
                }
                if (!isBlockedAt(zone, x, y, PLAYER_SIZE)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBlockedAt(TmxWorldLoader.LoadedZone zone, float x, float y, float size) {
        float half = size / 2f;
        if (x - half < 0f || x + half > zone.pixelWidth || y - half < 0f || y + half > zone.pixelHeight) {
            return true;
        }
        Rectangle candidate = new Rectangle(x - half, y - half, size, size);
        for (Rectangle collision : zone.collisions) {
            if (candidate.overlaps(collision)) {
                return true;
            }
        }
        for (TmxWorldLoader.Feature feature : zone.features) {
            if (!feature.blocksMovement) {
                continue;
            }
            if (candidate.overlaps(feature.bounds)) {
                return true;
            }
        }
        return false;
    }

    private float distanceToRect(float x, float y, Rectangle rect) {
        return distanceToRect(new Vector2(x, y), rect);
    }

    private float distanceToRect(Vector2 point, Rectangle rect) {
        float cx = Math.max(rect.x, Math.min(point.x, rect.x + rect.width));
        float cy = Math.max(rect.y, Math.min(point.y, rect.y + rect.height));
        return point.dst(cx, cy);
    }
}
