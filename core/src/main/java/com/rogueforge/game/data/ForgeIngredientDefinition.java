package com.rogueforge.game.data;

/**
 * Single forge recipe ingredient requirement.
 */
public class ForgeIngredientDefinition {
    private String componentId;
    private int quantity;

    public ForgeIngredientDefinition() {
    }

    public String getComponentId() {
        return componentId;
    }

    public int getQuantity() {
        return Math.max(0, quantity);
    }
}
