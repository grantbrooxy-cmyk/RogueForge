package com.rogueforge.game.data;

/**
 * Data definition for a forge component dropped by enemies.
 */
public class ForgeComponentDefinition {
    private String id;
    private String name;
    private String description;
    private String rarity;
    private String[] dropTags;

    public ForgeComponentDefinition() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public String getRarity() {
        return rarity != null ? rarity : "COMMON";
    }

    public String[] getDropTags() {
        return dropTags != null ? dropTags : new String[0];
    }
}
