package com.rogueforge.game.world;

import java.util.Map;

/**
 * Small pure helpers for Act 2 support systems so progression rules can be tested
 * without needing to boot the full screen stack.
 */
public final class ActTwoSupportDirector {

    private ActTwoSupportDirector() {
    }

    public static int calculateTrainingGroundDrillXp(
        long bankedGold,
        Map<String, Integer> bankedComponents,
        Map<String, Integer> bankedShards,
        Map<String, Integer> bankedBlueprints
    ) {
        int score = 0;
        score += (int) Math.min(6L, Math.max(0L, bankedGold / 80L));
        score += sumInventoryAmounts(bankedComponents);
        score += sumInventoryAmounts(bankedShards) * 2;
        score += sumInventoryAmounts(bankedBlueprints) * 3;
        if (score <= 0) {
            return 0;
        }
        return Math.min(24, Math.max(6, score));
    }

    public static int sumInventoryAmounts(Map<String, Integer> inventory) {
        if (inventory == null || inventory.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Integer value : inventory.values()) {
            if (value != null && value > 0) {
                total += value;
            }
        }
        return total;
    }
}
