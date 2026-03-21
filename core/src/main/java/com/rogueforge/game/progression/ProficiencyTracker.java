package com.rogueforge.game.progression;

/**
 * Shared formulas for ability proficiency growth and combat scaling.
 */
public final class ProficiencyTracker {
    private ProficiencyTracker() {
    }

    public static int xpForAbilityUse() {
        return 12;
    }

    public static float abilityPowerMultiplier(int level) {
        int clamped = Math.max(1, Math.min(10, level));
        return 1f + ((clamped - 1) * 0.04f);
    }

    public static float cooldownMultiplier(int level) {
        int clamped = Math.max(1, Math.min(10, level));
        return Math.max(0.72f, 1f - ((clamped - 1) * 0.03f));
    }
}
