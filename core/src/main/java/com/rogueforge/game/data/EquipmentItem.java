package com.rogueforge.game.data;

/**
 * Data POJO for equipment items loaded from JSON.
 * Represents a piece of equipment with slot type and stat bonuses.
 */
public class EquipmentItem {

    private String id;
    private String name;
    private String slotType;
    private int hpBonus;
    private int attackBonus;
    private int defenseBonus;
    private int speedBonus;
    private int intelligenceBonus;
    private long cost;
    private int tier;
    private String gradeRequirement;
    private String uniqueBoost;

    /**
     * No-arg constructor required for JSON deserialization.
     */
    public EquipmentItem() {
    }

    public EquipmentItem(String id, String name, String slotType, int hpBonus,
                        int attackBonus, int defenseBonus, int speedBonus, int intelligenceBonus,
                        long cost, int tier, String gradeRequirement, String uniqueBoost) {
        this.id = id;
        this.name = name;
        this.slotType = slotType;
        this.hpBonus = hpBonus;
        this.attackBonus = attackBonus;
        this.defenseBonus = defenseBonus;
        this.speedBonus = speedBonus;
        this.intelligenceBonus = intelligenceBonus;
        this.cost = cost;
        this.tier = tier;
        this.gradeRequirement = gradeRequirement;
        this.uniqueBoost = uniqueBoost;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlotType() {
        return slotType;
    }

    public int getHpBonus() {
        return hpBonus;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public int getDefenseBonus() {
        return defenseBonus;
    }

    public int getSpeedBonus() {
        return speedBonus;
    }

    public int getIntelligenceBonus() {
        return intelligenceBonus;
    }

    public long getCost() {
        return cost;
    }

    public int getTier() {
        return tier;
    }

    public String getGradeRequirement() {
        return gradeRequirement != null ? gradeRequirement : "G";
    }

    public String getUniqueBoost() {
        return uniqueBoost != null ? uniqueBoost : "";
    }
}
