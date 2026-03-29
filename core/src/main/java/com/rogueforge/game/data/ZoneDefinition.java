package com.rogueforge.game.data;

/**
 * Data POJO for zone definitions loaded from JSON.
 * Represents a game zone/level with difficulty range and monsters.
 */
public class ZoneDefinition {
    public static final String WORLD_TYPE_STATIC = "static";
    public static final String WORLD_TYPE_EXPANSIVE_FRONTIER = "expansive_frontier";

    private String id;
    private String name;
    private String tilemapPath;
    private String rankFloor;
    private String rankCeiling;
    private String bossId;
    private String[] monsterIds;
    private String worldType;
    private int expansiveWidthTiles;
    private int expansiveHeightTiles;
    private int starterSafeRadiusTiles;

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

    public String getWorldType() {
        return worldType != null && !worldType.isEmpty() ? worldType : WORLD_TYPE_STATIC;
    }

    public boolean isExpansiveFrontier() {
        return WORLD_TYPE_EXPANSIVE_FRONTIER.equals(getWorldType());
    }

    public int getExpansiveWidthTiles() {
        return expansiveWidthTiles > 0 ? expansiveWidthTiles : 0;
    }

    public int getExpansiveHeightTiles() {
        return expansiveHeightTiles > 0 ? expansiveHeightTiles : 0;
    }

    public int getStarterSafeRadiusTiles() {
        return starterSafeRadiusTiles > 0 ? starterSafeRadiusTiles : 0;
    }
}
