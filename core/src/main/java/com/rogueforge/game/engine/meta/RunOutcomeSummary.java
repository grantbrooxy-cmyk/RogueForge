package com.rogueforge.game.engine.meta;

public class RunOutcomeSummary {
    private final String zoneRank;
    private final int enemiesKilled;
    private final float survivalTimeSeconds;
    private final int playerLevel;
    private final int bossesDefeated;
    private final int structuresBuilt;
    private final int claimedSites;

    public RunOutcomeSummary(
        String zoneRank,
        int enemiesKilled,
        float survivalTimeSeconds,
        int playerLevel,
        int bossesDefeated,
        int structuresBuilt,
        int claimedSites
    ) {
        this.zoneRank = zoneRank;
        this.enemiesKilled = Math.max(0, enemiesKilled);
        this.survivalTimeSeconds = Math.max(0f, survivalTimeSeconds);
        this.playerLevel = Math.max(1, playerLevel);
        this.bossesDefeated = Math.max(0, bossesDefeated);
        this.structuresBuilt = Math.max(0, structuresBuilt);
        this.claimedSites = Math.max(0, claimedSites);
    }

    public String getZoneRank() {
        return zoneRank;
    }

    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    public float getSurvivalTimeSeconds() {
        return survivalTimeSeconds;
    }

    public int getPlayerLevel() {
        return playerLevel;
    }

    public int getBossesDefeated() {
        return bossesDefeated;
    }

    public int getStructuresBuilt() {
        return structuresBuilt;
    }

    public int getClaimedSites() {
        return claimedSites;
    }
}
