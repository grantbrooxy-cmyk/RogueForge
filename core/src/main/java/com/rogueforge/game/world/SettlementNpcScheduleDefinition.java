package com.rogueforge.game.world;

/**
 * Data-driven NPC schedule offsets relative to the NPC's authored spawn point.
 */
public class SettlementNpcScheduleDefinition {
    private String npcId;
    private float homeOffsetX;
    private float homeOffsetY;
    private float dayOffsetX;
    private float dayOffsetY;
    private float eveningOffsetX;
    private float eveningOffsetY;
    private float nightOffsetX;
    private float nightOffsetY;
    private String morningActivity;
    private String dayActivity;
    private String eveningActivity;
    private String nightActivity;

    public SettlementNpcScheduleDefinition() {
    }

    public String getNpcId() {
        return npcId;
    }

    public float getHomeOffsetX() {
        return homeOffsetX;
    }

    public float getHomeOffsetY() {
        return homeOffsetY;
    }

    public float getDayOffsetX() {
        return dayOffsetX;
    }

    public float getDayOffsetY() {
        return dayOffsetY;
    }

    public float getEveningOffsetX() {
        return eveningOffsetX;
    }

    public float getEveningOffsetY() {
        return eveningOffsetY;
    }

    public float getNightOffsetX() {
        return nightOffsetX;
    }

    public float getNightOffsetY() {
        return nightOffsetY;
    }

    public String getMorningActivity() {
        return morningActivity;
    }

    public String getDayActivity() {
        return dayActivity;
    }

    public String getEveningActivity() {
        return eveningActivity;
    }

    public String getNightActivity() {
        return nightActivity;
    }
}
