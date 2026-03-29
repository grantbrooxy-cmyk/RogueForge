package com.rogueforge.game.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.rogueforge.game.data.ZoneDefinition;

/**
 * Loads simplified world metadata from TMX object layers.
 * This keeps zone layout authored in Tiled while the game continues to use
 * its own rendering and interaction systems.
 */
public class TmxWorldLoader {

    public LoadedZone load(ZoneDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Zone definition is required");
        }

        XmlReader reader = new XmlReader();
        XmlReader.Element root;
        root = reader.parse(Gdx.files.internal(definition.getTilemapPath()));

        LoadedZone zone = new LoadedZone();
        zone.id = definition.getId();
        zone.displayName = definition.getName();
        zone.tileMapPath = definition.getTilemapPath();
        zone.mapWidthTiles = root.getIntAttribute("width", 0);
        zone.mapHeightTiles = root.getIntAttribute("height", 0);
        zone.tileWidth = root.getIntAttribute("tilewidth", 48);
        zone.tileHeight = root.getIntAttribute("tileheight", 48);
        zone.pixelWidth = zone.mapWidthTiles * zone.tileWidth;
        zone.pixelHeight = zone.mapHeightTiles * zone.tileHeight;
        zone.groundStyle = readMapProperty(root, "groundStyle", zone.id);
        zone.safeZone = Boolean.parseBoolean(readMapProperty(root, "safeZone", "false"));

        Array<XmlReader.Element> groups = root.getChildrenByName("objectgroup");
        for (XmlReader.Element group : groups) {
            String groupName = group.getAttribute("name", "");
            Array<XmlReader.Element> objects = group.getChildrenByName("object");
            for (XmlReader.Element object : objects) {
                parseObject(zone, groupName, object);
            }
        }

        return zone;
    }

    private void parseObject(LoadedZone zone, String groupName, XmlReader.Element object) {
        switch (groupName) {
            case "collisions":
                zone.collisions.add(readRectangle(zone, object));
                break;
            case "doors":
                zone.doors.add(readDoor(zone, object));
                break;
            case "spawn_points":
                readSpawn(zone, object);
                break;
            case "npcs":
                zone.npcs.add(readNpc(zone, object));
                break;
            case "features":
                zone.features.add(readFeature(zone, object));
                break;
            case "chests":
                zone.chests.add(readChest(zone, object));
                break;
            default:
                break;
        }
    }

    private Door readDoor(LoadedZone zone, XmlReader.Element object) {
        Door door = new Door();
        door.id = object.getAttribute("name", "");
        door.bounds = readRectangle(zone, object);
        door.label = readProperty(object, "label", door.id);
        door.targetZoneId = readProperty(object, "targetZone", null);
        door.targetSpawnId = readProperty(object, "targetSpawn", null);
        door.lockedByKeyItem = readProperty(object, "keyItem", null);
        door.lockedByWorldFlag = readProperty(object, "lockedByWorldFlag", null);
        door.requiredWorldFlag = readProperty(object, "requiredWorldFlag", null);
        door.lockMessage = readProperty(object, "lockMessage", "It won't open yet.");
        String houseIdText = readProperty(object, "houseId", null);
        door.houseId = houseIdText != null && !houseIdText.isEmpty() ? Integer.parseInt(houseIdText) : -1;
        return door;
    }

    private void readSpawn(LoadedZone zone, XmlReader.Element object) {
        String spawnType = object.getAttribute("type", "");
        String spawnId = readProperty(object, "spawnId", object.getAttribute("name", ""));
        Vector2 position = readPoint(zone, object);
        if ("player".equals(spawnType)) {
            zone.playerSpawns.put(spawnId, position);
        } else if ("enemy".equals(spawnType)) {
            zone.enemySpawns.add(position);
        }
    }

    private NpcData readNpc(LoadedZone zone, XmlReader.Element object) {
        NpcData npc = new NpcData();
        npc.id = readProperty(object, "npcId", object.getAttribute("name", ""));
        npc.name = readProperty(object, "displayName", npc.id);
        npc.position = readPoint(zone, object);
        npc.requiredWorldFlag = readProperty(object, "requiredWorldFlag", null);
        npc.hiddenUntilFlag = readProperty(object, "hiddenUntilFlag", null);
        return npc;
    }

    private Feature readFeature(LoadedZone zone, XmlReader.Element object) {
        Feature feature = new Feature();
        feature.id = object.getAttribute("name", "");
        feature.kind = readProperty(object, "kind", "wall");
        feature.label = readProperty(object, "label", feature.id);
        feature.bounds = readRectangle(zone, object);
        feature.requiredWorldFlag = readProperty(object, "requiredWorldFlag", null);
        feature.hiddenUntilFlag = readProperty(object, "hiddenUntilFlag", null);
        feature.interactionType = readProperty(object, "interactionType", null);
        feature.shopId = readProperty(object, "shopId", null);
        feature.completionWorldFlag = readProperty(object, "completionWorldFlag", null);
        feature.interactionMessage = readProperty(object, "interactionMessage", null);
        feature.blockedMessage = readProperty(object, "blockedMessage", null);
        feature.blocksMovement = Boolean.parseBoolean(readProperty(object, "blocksMovement", "false"));
        feature.resourceId = readProperty(object, "resourceId", null);
        feature.resourceAmount = Integer.parseInt(readProperty(object, "resourceAmount", "0"));
        feature.persistentStateId = readProperty(object, "persistentStateId", feature.id);
        feature.biomeId = readProperty(object, "biomeId", null);
        feature.biomeGroundAssetFolder = readProperty(object, "biomeGroundAssetFolder", null);
        feature.biomeObjectAssetFolder = readProperty(object, "biomeObjectAssetFolder", null);
        String houseIdText = readProperty(object, "houseId", null);
        feature.houseId = houseIdText != null && !houseIdText.isEmpty() ? Integer.parseInt(houseIdText) : -1;
        return feature;
    }

    private ChestData readChest(LoadedZone zone, XmlReader.Element object) {
        ChestData chest = new ChestData();
        chest.id = object.getAttribute("name", "");
        chest.position = readPoint(zone, object);
        chest.goldReward = Long.parseLong(readProperty(object, "gold", "0"));
        chest.potionReward = Integer.parseInt(readProperty(object, "potions", "0"));
        chest.hidden = Boolean.parseBoolean(readProperty(object, "hidden", "false"));
        chest.keyItemReward = readProperty(object, "keyItemReward", null);
        chest.completionWorldFlag = readProperty(object, "completionWorldFlag", null);
        chest.recruitEventId = readProperty(object, "recruitEventId", null);
        chest.requiredWorldFlag = readProperty(object, "requiredWorldFlag", null);
        chest.hiddenUntilFlag = readProperty(object, "hiddenUntilFlag", null);
        chest.message = readProperty(object, "message", "You found supplies.");
        return chest;
    }

    private Rectangle readRectangle(LoadedZone zone, XmlReader.Element object) {
        float x = object.getFloatAttribute("x", 0f);
        float y = object.getFloatAttribute("y", 0f);
        float width = object.getFloatAttribute("width", 0f);
        float height = object.getFloatAttribute("height", 0f);
        return new Rectangle(x, flipY(zone, y, height), width, height);
    }

    private Vector2 readPoint(LoadedZone zone, XmlReader.Element object) {
        float x = object.getFloatAttribute("x", 0f);
        float y = object.getFloatAttribute("y", 0f);
        return new Vector2(x, flipY(zone, y, 0f));
    }

    private float flipY(LoadedZone zone, float tiledY, float objectHeight) {
        return zone.pixelHeight - tiledY - objectHeight;
    }

    private String readMapProperty(XmlReader.Element root, String propertyName, String fallback) {
        XmlReader.Element properties = root.getChildByName("properties");
        if (properties == null) {
            return fallback;
        }
        Array<XmlReader.Element> propertyList = properties.getChildrenByName("property");
        for (XmlReader.Element property : propertyList) {
            if (propertyName.equals(property.getAttribute("name", ""))) {
                return property.getAttribute("value", fallback);
            }
        }
        return fallback;
    }

    private String readProperty(XmlReader.Element object, String propertyName, String fallback) {
        XmlReader.Element properties = object.getChildByName("properties");
        if (properties == null) {
            return fallback;
        }
        Array<XmlReader.Element> propertyList = properties.getChildrenByName("property");
        for (XmlReader.Element property : propertyList) {
            if (propertyName.equals(property.getAttribute("name", ""))) {
                return property.getAttribute("value", fallback);
            }
        }
        return fallback;
    }

    public static class LoadedZone {
        public String id;
        public String displayName;
        public String tileMapPath;
        public String groundStyle;
        public boolean safeZone;
        public Vector2 safeCenter;
        public float safeRadius;
        public int mapWidthTiles;
        public int mapHeightTiles;
        public int tileWidth;
        public int tileHeight;
        public int pixelWidth;
        public int pixelHeight;
        public final Array<Rectangle> collisions = new Array<>();
        public final Array<Door> doors = new Array<>();
        public final ObjectMap<String, Vector2> playerSpawns = new ObjectMap<>();
        public final Array<Vector2> enemySpawns = new Array<>();
        public final Array<NpcData> npcs = new Array<>();
        public final Array<Feature> features = new Array<>();
        public final Array<ChestData> chests = new Array<>();

        public boolean isSafeAt(Vector2 position) {
            if (safeZone) {
                return true;
            }
            if (position == null || safeCenter == null || safeRadius <= 0f) {
                return false;
            }
            return safeCenter.dst2(position) <= safeRadius * safeRadius;
        }
    }

    public static class Door {
        public String id;
        public Rectangle bounds;
        public String label;
        public String targetZoneId;
        public String targetSpawnId;
        public String lockedByKeyItem;
        public String lockedByWorldFlag;
        public String requiredWorldFlag;
        public String lockMessage;
        public int houseId = -1;
    }

    public static class NpcData {
        public String id;
        public String name;
        public Vector2 position;
        public String requiredWorldFlag;
        public String hiddenUntilFlag;
    }

    public static class Feature {
        public String id;
        public String kind;
        public String label;
        public Rectangle bounds;
        public String requiredWorldFlag;
        public String hiddenUntilFlag;
        public String interactionType;
        public String shopId;
        public String completionWorldFlag;
        public String interactionMessage;
        public String blockedMessage;
        public boolean blocksMovement;
        public int houseId = -1;
        public String resourceId;
        public int resourceAmount;
        public String persistentStateId;
        public String terrainType;
        public String biomeId;
        public String biomeGroundAssetFolder;
        public String biomeObjectAssetFolder;
    }

    public static class ChestData {
        public String id;
        public Vector2 position;
        public long goldReward;
        public int potionReward;
        public boolean hidden;
        public String keyItemReward;
        public String completionWorldFlag;
        public String recruitEventId;
        public String requiredWorldFlag;
        public String hiddenUntilFlag;
        public String message;
    }
}
