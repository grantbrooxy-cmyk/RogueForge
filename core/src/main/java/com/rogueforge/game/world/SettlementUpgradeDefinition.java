package com.rogueforge.game.world;

/**
 * Describes one settlement upgrade and its reward payload.
 */
public class SettlementUpgradeDefinition {
    private String id;
    private String name;
    private String rewardEquipmentId;
    private int rewardPotions;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRewardEquipmentId() { return rewardEquipmentId; }
    public int getRewardPotions() { return rewardPotions; }
}
