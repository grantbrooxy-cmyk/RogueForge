package com.rogueforge.game.world;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActTwoSupportDirectorTest {

    @Test
    void trainingGroundDrillXpStaysZeroWithoutAnyBankedHaul() {
        assertEquals(0, ActTwoSupportDirector.calculateTrainingGroundDrillXp(0, Map.of(), Map.of(), Map.of()));
        assertEquals(0, ActTwoSupportDirector.calculateTrainingGroundDrillXp(0, null, null, null));
    }

    @Test
    void trainingGroundDrillXpUsesMinimumRewardOnceHaulExists() {
        int xp = ActTwoSupportDirector.calculateTrainingGroundDrillXp(
            80,
            Map.of("bone_fiber", 1),
            Map.of(),
            Map.of()
        );

        assertEquals(6, xp);
    }

    @Test
    void trainingGroundDrillXpWeightsShardsAndBlueprintsMoreHeavilyAndCapsAtTwentyFour() {
        int xp = ActTwoSupportDirector.calculateTrainingGroundDrillXp(
            960,
            Map.of("bone_fiber", 4, "bot_chassis_fragment", 3),
            Map.of("B", 3, "A", 2),
            Map.of("forge_schema", 4)
        );

        assertEquals(24, xp);
    }

    @Test
    void sumInventoryAmountsIgnoresNullZeroAndNegativeEntries() {
        assertEquals(
            5,
            ActTwoSupportDirector.sumInventoryAmounts(Map.of("good", 5, "zero", 0, "bad", -3))
        );
    }
}
