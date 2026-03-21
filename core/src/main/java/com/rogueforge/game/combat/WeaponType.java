package com.rogueforge.game.combat;

/**
 * Weapon families used for proficiency progression.
 */
public enum WeaponType {
    NONE,
    SWORD,
    AXE,
    LANCE,
    STAFF,
    BOW,
    FIST,
    GUN,
    DUAL_BLADE;

    public static WeaponType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return NONE;
        }
        try {
            return WeaponType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
