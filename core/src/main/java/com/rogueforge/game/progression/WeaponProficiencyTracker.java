package com.rogueforge.game.progression;

import com.rogueforge.game.combat.WeaponType;

/**
 * Shared formulas for weapon proficiency growth and combat scaling.
 */
public final class WeaponProficiencyTracker {
    private WeaponProficiencyTracker() {
    }

    public static WeaponProficiencyState getOrCreate(RobotProgressionState robotState, WeaponType weaponType) {
        if (robotState == null || weaponType == null || weaponType == WeaponType.NONE) {
            return null;
        }
        WeaponProficiencyState state = robotState.getWeaponProficiencies().get(weaponType.name());
        if (state == null) {
            state = new WeaponProficiencyState(weaponType.name());
            robotState.getWeaponProficiencies().put(weaponType.name(), state);
        }
        return state;
    }

    public static int xpForAttack() {
        return 10;
    }

    public static float damageMultiplier(int level) {
        int clamped = Math.max(1, Math.min(10, level));
        return 1f + ((clamped - 1) * 0.03f);
    }

    public static String unlockLabel(int level) {
        switch (level) {
            case 3:
                return "Combat Art I";
            case 6:
                return "Combat Art II";
            case 10:
                return "Weapon Mastery";
            default:
                return "";
        }
    }
}
