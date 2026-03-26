package com.rogueforge.game.combat;

/**
 * Static helpers for elemental multipliers.
 */
public final class ElementalSystem {
    private ElementalSystem() {
    }

    public static float getMultiplier(Element element, BattleCombatant target) {
        if (element == null || element == Element.NONE || target == null) {
            return 1f;
        }
        if (target.getAbsorbs().contains(element)) {
            return -1f;
        }
        if (target.hasElementalBreak(element)) {
            if (target.getWeaknesses().contains(element)) {
                return 1.5f;
            }
            if (target.getResistances().contains(element)) {
                return 1f;
            }
            return 1.5f;
        }
        if (target.getWeaknesses().contains(element)) {
            return 1.5f;
        }
        if (target.getResistances().contains(element)) {
            return 0.5f;
        }
        return 1f;
    }

    public static String describeHit(float multiplier) {
        if (multiplier < 0f) {
            return "absorbed";
        }
        if (multiplier > 1f) {
            return "weakness";
        }
        if (multiplier < 1f) {
            return "resisted";
        }
        return "neutral";
    }
}
