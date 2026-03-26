package com.rogueforge.game.data;

/**
 * Forge recipe that turns dropped components into crafted equipment.
 */
public class ForgeRecipeDefinition {
    private String id;
    private String name;
    private String category;
    private String description;
    private String resultEquipmentId;
    private long goldCost;
    private String shardGrade;
    private int shardCost;
    private ForgeIngredientDefinition[] ingredients;

    public ForgeRecipeDefinition() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public String getCategory() {
        return category != null ? category : "General";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public String getResultEquipmentId() {
        return resultEquipmentId;
    }

    public long getGoldCost() {
        return Math.max(0L, goldCost);
    }

    public String getShardGrade() {
        return shardGrade != null ? shardGrade : "G";
    }

    public int getShardCost() {
        return Math.max(0, shardCost);
    }

    public ForgeIngredientDefinition[] getIngredients() {
        return ingredients != null ? ingredients : new ForgeIngredientDefinition[0];
    }
}
