package com.rogueforge.game.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RobotProgressionStateTest {

    @Test
    void usageProfileDerivesPersonalityAndEvolutionPath() {
        RobotProgressionState state = new RobotProgressionState("ivy", "Ivy");

        state.recordUsage("support", 4);
        state.recordUsage("control", 1);

        assertEquals("Caretaker", state.getPersonalityArchetype());
        assertEquals("harmonic", state.getEvolutionPath());

        state.recordUsage("assault", 8);

        assertEquals("Berserker", state.getPersonalityArchetype());
        assertEquals("feral", state.getEvolutionPath());
    }
}
