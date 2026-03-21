package com.rogueforge.game.combat;

/**
 * Supported battle elements.
 */
public enum Element {
    NONE,
    FIRE,
    ICE,
    LIGHTNING,
    EARTH,
    WIND,
    WATER;

    public static Element fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return NONE;
        }
        try {
            return Element.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
