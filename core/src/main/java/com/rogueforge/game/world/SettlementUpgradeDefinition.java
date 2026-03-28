package com.rogueforge.game.world;

/**
 * Describes one settlement upgrade and its reward payload.
 */
public class SettlementUpgradeDefinition {
    private String id;
    private String name;
    private String facility;
    private int tier;
    private String type;
    private String description;
    private String requiredUpgradeId;
    private String rewardEquipmentId;
    private int rewardPotions;
    private int rewardGold;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getFacility() { return facility != null ? facility : ""; }
    public int getTier() { return tier; }
    public String getType() { return type != null ? type : ""; }
    public String getDescription() { return description != null ? description : ""; }
    public String getRequiredUpgradeId() { return requiredUpgradeId != null ? requiredUpgradeId : ""; }
    public String getRewardEquipmentId() { return rewardEquipmentId != null ? rewardEquipmentId : ""; }
    public int getRewardPotions() { return rewardPotions; }
    public int getRewardGold() { return rewardGold; }
}
