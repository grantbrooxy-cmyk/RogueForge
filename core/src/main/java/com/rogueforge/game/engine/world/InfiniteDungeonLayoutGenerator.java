package com.rogueforge.game.engine.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import java.util.Random;

/**
 * Generates deterministic procedural layouts for Infinite Dungeon floors while
 * preserving the simplified object-layer world model used by GameScreen.
 */
public class InfiniteDungeonLayoutGenerator {
    private static final float PERIMETER = 60f;
    private static final float START_ROOM_WIDTH = 300f;
    private static final float EXIT_ROOM_WIDTH = 280f;
    private static final float ROOM_HEIGHT = 260f;
    private static final float DOOR_SIZE = 72f;

    public TmxWorldLoader.LoadedZone generate(TmxWorldLoader.LoadedZone template, int floor, boolean shardRunMode) {
        if (template == null) {
            throw new IllegalArgumentException("Template zone is required");
        }
        int effectiveFloor = Math.max(1, floor);
        Random random = new Random(0x5EEDC0DEL + (effectiveFloor * 7919L));

        TmxWorldLoader.LoadedZone zone = copySkeleton(template);
        float width = zone.pixelWidth;
        float height = zone.pixelHeight;
        float corridorY = 260f + random.nextFloat() * Math.max(120f, height - 520f);

        zone.playerSpawns.put("from_hub", new Vector2(200f, corridorY));
        zone.playerSpawns.put("from_void", new Vector2(200f, corridorY - 68f));
        zone.playerSpawns.put("from_boss_gate", new Vector2(260f, corridorY + 68f));

        addPerimeter(zone, width, height);
        addDoors(zone, width, corridorY);
        addNpc(zone, corridorY);
        addAnchorFeatures(zone, width, corridorY, effectiveFloor);
        addProceduralObstacles(zone, width, height, corridorY, effectiveFloor, random);
        addEnemySpawns(zone, width, height, corridorY, effectiveFloor, random);
        addChests(zone, width, height, corridorY, effectiveFloor, random);
        return zone;
    }

    private TmxWorldLoader.LoadedZone copySkeleton(TmxWorldLoader.LoadedZone template) {
        TmxWorldLoader.LoadedZone zone = new TmxWorldLoader.LoadedZone();
        zone.id = template.id;
        zone.displayName = template.displayName;
        zone.tileMapPath = template.tileMapPath;
        zone.groundStyle = template.groundStyle;
        zone.safeZone = false;
        zone.mapWidthTiles = template.mapWidthTiles;
        zone.mapHeightTiles = template.mapHeightTiles;
        zone.tileWidth = template.tileWidth;
        zone.tileHeight = template.tileHeight;
        zone.pixelWidth = template.pixelWidth;
        zone.pixelHeight = template.pixelHeight;
        return zone;
    }

    private void addPerimeter(TmxWorldLoader.LoadedZone zone, float width, float height) {
        zone.collisions.add(new Rectangle(0f, 0f, width, PERIMETER));
        zone.collisions.add(new Rectangle(0f, height - PERIMETER, width, PERIMETER));
        zone.collisions.add(new Rectangle(0f, 0f, PERIMETER, height));
        zone.collisions.add(new Rectangle(width - PERIMETER, 0f, PERIMETER, height));
    }

    private void addDoors(TmxWorldLoader.LoadedZone zone, float width, float corridorY) {
        zone.doors.add(createDoor(
            "the_void_exit",
            110f,
            corridorY - 110f,
            "The Void",
            "the_void",
            "from_dungeon"
        ));
        zone.doors.add(createDoor(
            "verdant_fields_gate",
            110f,
            corridorY + 52f,
            "Ironhaven Route",
            "town",
            "from_dungeon"
        ));
        zone.doors.add(createDoor(
            "boss_gate_door",
            width - 150f,
            corridorY - 36f,
            "Deep Trial Gate",
            "infinite_dungeon",
            "from_boss_gate"
        ));
    }

    private void addNpc(TmxWorldLoader.LoadedZone zone, float corridorY) {
        TmxWorldLoader.NpcData npc = new TmxWorldLoader.NpcData();
        npc.id = "bolt_simulation";
        npc.name = "Bolt Simulation";
        npc.position = new Vector2(260f, corridorY + 120f);
        zone.npcs.add(npc);
    }

    private void addAnchorFeatures(TmxWorldLoader.LoadedZone zone, float width, float corridorY, int floor) {
        zone.features.add(createFeature(
            "trial_pillar_floor_" + floor,
            "ruin",
            "Trial Pillar",
            width * 0.48f,
            corridorY - 70f,
            120f,
            140f,
            false
        ));
        zone.features.add(createFeature(
            "boss_gate_floor_" + floor,
            "gate",
            floor % 10 == 0 ? "Boss Trial Gate" : "Deep Trial Gate",
            width - 220f,
            corridorY - 110f,
            130f,
            220f,
            false
        ));
    }

    private void addProceduralObstacles(TmxWorldLoader.LoadedZone zone, float width, float height, float corridorY,
                                        int floor, Random random) {
        Rectangle safeStart = new Rectangle(120f, corridorY - ROOM_HEIGHT * 0.5f, START_ROOM_WIDTH, ROOM_HEIGHT);
        Rectangle safeExit = new Rectangle(width - EXIT_ROOM_WIDTH - 120f, corridorY - ROOM_HEIGHT * 0.5f, EXIT_ROOM_WIDTH, ROOM_HEIGHT);
        Rectangle safeCorridor = new Rectangle(140f, corridorY - 74f, width - 280f, 148f);

        int blockCount = 4 + (floor % 3);
        Array<Rectangle> placed = new Array<>();
        for (int i = 0; i < blockCount; i++) {
            Rectangle block = null;
            for (int attempt = 0; attempt < 20 && block == null; attempt++) {
                float blockWidth = 120f + (random.nextInt(4) * 24f);
                float blockHeight = 96f + (random.nextInt(5) * 24f);
                float x = 420f + random.nextFloat() * Math.max(40f, width - 900f);
                float y = 120f + random.nextFloat() * Math.max(40f, height - 360f);
                Rectangle candidate = new Rectangle(x, y, blockWidth, blockHeight);
                if (candidate.overlaps(safeStart) || candidate.overlaps(safeExit) || candidate.overlaps(safeCorridor)) {
                    continue;
                }
                boolean overlapsExisting = false;
                for (Rectangle existing : placed) {
                    if (expanded(existing, 40f).overlaps(candidate)) {
                        overlapsExisting = true;
                        break;
                    }
                }
                if (!overlapsExisting) {
                    block = candidate;
                }
            }
            if (block == null) {
                continue;
            }
            placed.add(block);
            zone.collisions.add(block);
            zone.features.add(createFeature(
                "obstacle_" + floor + "_" + i,
                i % 2 == 0 ? "ruin" : "cliff",
                i % 2 == 0 ? "Shifted Wall" : "Collapsed Span",
                block.x,
                block.y,
                block.width,
                block.height,
                false
            ));
        }
    }

    private Rectangle expanded(Rectangle rect, float amount) {
        return new Rectangle(rect.x - amount, rect.y - amount, rect.width + amount * 2f, rect.height + amount * 2f);
    }

    private void addEnemySpawns(TmxWorldLoader.LoadedZone zone, float width, float height, float corridorY,
                                int floor, Random random) {
        int count = 4 + Math.min(2, floor / 5);
        for (int i = 0; i < count; i++) {
            float laneOffset = (i % 2 == 0 ? -1f : 1f) * (150f + random.nextFloat() * 220f);
            float x = 500f + random.nextFloat() * Math.max(40f, width - 900f);
            float y = clamp(corridorY + laneOffset, 120f, height - 120f);
            zone.enemySpawns.add(new Vector2(x, y));
        }
    }

    private void addChests(TmxWorldLoader.LoadedZone zone, float width, float height, float corridorY,
                           int floor, Random random) {
        TmxWorldLoader.ChestData mainChest = new TmxWorldLoader.ChestData();
        mainChest.id = "floor_cache_" + floor;
        mainChest.position = new Vector2(
            width * 0.56f + random.nextFloat() * 160f,
            clamp(corridorY + (random.nextBoolean() ? 220f : -220f), 120f, height - 120f)
        );
        mainChest.goldReward = 40L + (floor * 18L);
        mainChest.potionReward = floor % 5 == 0 ? 2 : 1;
        mainChest.message = "The floor cache yields upgraded salvage tuned to the dungeon's latest trial.";
        zone.chests.add(mainChest);

        if (floor % 3 == 0) {
            TmxWorldLoader.ChestData sideChest = new TmxWorldLoader.ChestData();
            sideChest.id = "bonus_cache_" + floor;
            sideChest.position = new Vector2(
                width * 0.74f,
                clamp(corridorY + (random.nextBoolean() ? 280f : -280f), 120f, height - 120f)
            );
            sideChest.goldReward = 25L + (floor * 12L);
            sideChest.potionReward = 1;
            sideChest.message = "A side chamber cache waits behind the shifting walls.";
            zone.chests.add(sideChest);
        }
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private TmxWorldLoader.Door createDoor(String id, float x, float y, String label, String targetZone, String targetSpawn) {
        TmxWorldLoader.Door door = new TmxWorldLoader.Door();
        door.id = id;
        door.bounds = new Rectangle(x, y, DOOR_SIZE, DOOR_SIZE);
        door.label = label;
        door.targetZoneId = targetZone;
        door.targetSpawnId = targetSpawn;
        door.lockMessage = "The trial gate responds once the dungeon's current challenge is cleared.";
        return door;
    }

    private TmxWorldLoader.Feature createFeature(String id, String kind, String label,
                                                 float x, float y, float width, float height,
                                                 boolean blocksMovement) {
        TmxWorldLoader.Feature feature = new TmxWorldLoader.Feature();
        feature.id = id;
        feature.kind = kind;
        feature.label = label;
        feature.bounds = new Rectangle(x, y, width, height);
        feature.blocksMovement = blocksMovement;
        return feature;
    }
}
