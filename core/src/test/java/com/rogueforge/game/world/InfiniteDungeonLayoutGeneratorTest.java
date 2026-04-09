package com.rogueforge.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.engine.world.InfiniteDungeonLayoutGenerator;
import com.rogueforge.game.engine.world.TmxWorldLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfiniteDungeonLayoutGeneratorTest {

    @Test
    void generatorProducesDeterministicButFloorVariantLayouts() {
        InfiniteDungeonLayoutGenerator generator = new InfiniteDungeonLayoutGenerator();
        TmxWorldLoader.LoadedZone template = templateZone();

        TmxWorldLoader.LoadedZone floorThreeA = generator.generate(template, 3, false);
        TmxWorldLoader.LoadedZone floorThreeB = generator.generate(template, 3, false);
        TmxWorldLoader.LoadedZone floorFour = generator.generate(template, 4, false);
        TmxWorldLoader.LoadedZone shardRunFloor = generator.generate(template, 1, true);

        assertNotEquals(0, shardRunFloor.pixelWidth);

        assertEquals(floorThreeA.features.size, floorThreeB.features.size);
        assertEquals(floorThreeA.chests.get(0).id, floorThreeB.chests.get(0).id);
        assertEquals(floorThreeA.enemySpawns.get(0), floorThreeB.enemySpawns.get(0));

        assertNotEquals(floorThreeA.chests.get(0).id, floorFour.chests.get(0).id);
        assertNotEquals(floorThreeA.enemySpawns.get(0), floorFour.enemySpawns.get(0));
        assertTrue(floorThreeA.playerSpawns.containsKey("from_hub"));
        assertTrue(floorThreeA.playerSpawns.containsKey("from_void"));
        assertTrue(floorThreeA.playerSpawns.containsKey("from_boss_gate"));
        assertEquals(3, floorThreeA.doors.size);
        assertFalse(floorThreeA.collisions.isEmpty());
        assertFalse(floorThreeA.npcs.isEmpty());
    }

    private TmxWorldLoader.LoadedZone templateZone() {
        TmxWorldLoader.LoadedZone zone = new TmxWorldLoader.LoadedZone();
        zone.id = "infinite_dungeon";
        zone.displayName = "Infinite Dungeon";
        zone.tileMapPath = "maps/infinite_dungeon.tmx";
        zone.groundStyle = "cave";
        zone.safeZone = false;
        zone.mapWidthTiles = 40;
        zone.mapHeightTiles = 28;
        zone.tileWidth = 48;
        zone.tileHeight = 48;
        zone.pixelWidth = 1920;
        zone.pixelHeight = 1344;
        zone.playerSpawns.put("from_hub", new Vector2(420f, 280f));
        zone.enemySpawns.add(new Vector2(520f, 864f));
        zone.collisions.add(new Rectangle(0f, 0f, 60f, 1344f));
        return zone;
    }
}
