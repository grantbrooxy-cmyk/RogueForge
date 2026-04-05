package com.rogueforge.game.progression;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RobotEvolutionManagerTest {

    @Test
    void addExperienceLevelsRobotUsingProgressionCurve() {
        RobotProgressionState state = new RobotProgressionState("rust_mk1", "Rust");

        int gained = RobotEvolutionManager.addExperience(state, 53);

        assertEquals(1, gained);
        assertEquals(2, state.getLevel());
        assertEquals(0, state.getExperience());
    }

    @Test
    void applyEvolutionHonorsForgeCoreAndGradeGates() {
        RobotProgressionState state = new RobotProgressionState("rust_mk1", "Rust");
        state.setLevel(5);

        assertFalse(RobotEvolutionManager.applyEvolution(state, "F", 2));
        assertEquals(1, state.getEvolutionTier());

        assertTrue(RobotEvolutionManager.applyEvolution(state, "E", 2));
        assertEquals(2, state.getEvolutionTier());

        state.setLevel(10);
        assertTrue(RobotEvolutionManager.applyEvolution(state, "C", 3));
        assertEquals(3, state.getEvolutionTier());
    }

    @Test
    void materialCostsAndRobotIdsMatchTier() {
        assertEquals(Map.of("bone_fiber", 3), RobotEvolutionManager.evolutionMaterialCost(2));
        assertEquals(Map.of("drake_heart", 2), RobotEvolutionManager.evolutionMaterialCost(3));
        assertEquals(Map.of(), RobotEvolutionManager.evolutionMaterialCost(1));
        assertEquals("bot_chassis_schema", RobotEvolutionManager.evolutionBlueprintFragmentId(2));
        assertEquals(2, RobotEvolutionManager.evolutionBlueprintFragmentCost(2));
        assertEquals("", RobotEvolutionManager.evolutionBlueprintFragmentId(3));
        assertEquals(0, RobotEvolutionManager.evolutionBlueprintFragmentCost(3));
        assertEquals("rust_mk3", RobotEvolutionManager.getEvolvedRobotId("rust_mk1", 3));
        assertEquals("rust_mk2", RobotEvolutionManager.getEvolvedRobotId("rust", 2));
    }
}
