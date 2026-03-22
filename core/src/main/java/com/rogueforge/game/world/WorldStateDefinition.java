package com.rogueforge.game.world;

/**
 * Declares a world-state flag used by content.
 */
public class WorldStateDefinition {
    private String id;
    private boolean defaultValue;

    public String getId() {
        return id;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }
}
