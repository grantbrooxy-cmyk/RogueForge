package com.rogueforge.game.data;

/**
 * Data POJO for zone definitions loaded from JSON.
 * Represents a game zone/level with difficulty range and monsters.
 */
public class ZoneDefinition {

    private String id;
    private String name;
    private String tilemapPath;
    private String rankFloor;
    private String rankCeiling;
    private String bossId;
    private String[] monsterIds;

    /**
     * No-arg constructor required for JSON deserialization.
     */
    public ZoneDefinition() {
    }

    public ZoneDefinition(String id, String name, String tilemapPath, String rankFloor,
                         String rankCeiling, String bossId, String[] monsterIds) {
        this.id = id;
        this.name = name;
        this.tilemapPath = tilemapPath;
        this.rankFloor = rankFloor;
        this.rankCeiling = rankCeiling;
        this.bossId = bossId;
        this.monsterIds = monsterIds;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTilemapPath() {
        return tilemapPath;
    }

    public String getRankFloor() {
        return rankFloor;
    }

    public String getRankCeiling() {
        return rankCeiling;
    }

    public String getBossId() {
        return bossId;
    }

    public String[] getMonsterIds() {
        return monsterIds;
    }
}
