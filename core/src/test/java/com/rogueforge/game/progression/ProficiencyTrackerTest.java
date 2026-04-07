package com.rogueforge.game.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProficiencyTrackerTest {

    @Test
    void abilityMultipliersClampAcrossExpectedRange() {
        assertEquals(1f, ProficiencyTracker.abilityPowerMultiplier(0), 0.0001f);
        assertEquals(1.16f, ProficiencyTracker.abilityPowerMultiplier(5), 0.0001f);
        assertEquals(1.36f, ProficiencyTracker.abilityPowerMultiplier(20), 0.0001f);
        assertEquals(1f, ProficiencyTracker.cooldownMultiplier(1), 0.0001f);
        assertEquals(0.73f, ProficiencyTracker.cooldownMultiplier(10), 0.0001f);
        assertEquals(12, ProficiencyTracker.xpForAbilityUse());
    }

    @Test
    void abilityProgressionStateLevelsAndClamps() {
        AbilityProgressionState state = new AbilityProgressionState("deep_scan");

        int levelsGained = state.addXp(200);

        assertEquals(4, state.getProficiencyLevel());
        assertEquals(50, state.getProficiencyXp());
        assertEquals(3, levelsGained);
        state.setProficiencyLevel(99);
        state.setProficiencyXp(-5);
        assertEquals(10, state.getProficiencyLevel());
        assertEquals(0, state.getProficiencyXp());
    }
}
