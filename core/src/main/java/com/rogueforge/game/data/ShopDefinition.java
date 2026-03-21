package com.rogueforge.game.data;

/**
 * Data definition for a shop inventory loaded from JSON.
 */
public class ShopDefinition {
    private String id;
    private ShopEntryDefinition[] entries;

    public String getId() {
        return id;
    }

    public ShopEntryDefinition[] getEntries() {
        return entries;
    }
}
