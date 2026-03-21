package com.rogueforge.game.data;

/**
 * Data definition for a single shop entry.
 */
public class ShopEntryDefinition {
    private String type;
    private String itemId;
    private String label;
    private int quantity;
    private long cost;

    public String getType() {
        return type;
    }

    public String getItemId() {
        return itemId;
    }

    public String getLabel() {
        return label;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getCost() {
        return cost;
    }
}
