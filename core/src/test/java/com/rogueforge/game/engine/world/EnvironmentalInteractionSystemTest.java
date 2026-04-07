package com.rogueforge.game.engine.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentalInteractionSystemTest {

    @Test
    void proficiencyLetsCrewHackRuinsWithoutExplicitAbility() {
        EnvironmentalInteractionSystem system = new EnvironmentalInteractionSystem();
        TmxWorldLoader.Feature feature = new TmxWorldLoader.Feature();
        feature.interactionType = "hack_ruins";

        Map<String, Integer> proficiencies = new HashMap<>();
        proficiencies.put("hacking", 2);
        EnvironmentalInteractionSystem.InteractionResolution result = system.evaluate(
            feature,
            new EnvironmentalInteractionSystem.InteractionProfile(Set.of(), proficiencies)
        );

        assertTrue(result.canInteract());
        assertEquals("Hack", result.getActionLabel());
    }

    @Test
    void mineInteractionStaysBlockedWithoutAbilityOrProficiency() {
        EnvironmentalInteractionSystem system = new EnvironmentalInteractionSystem();
        TmxWorldLoader.Feature feature = new TmxWorldLoader.Feature();
        feature.interactionType = "mine_ore";

        EnvironmentalInteractionSystem.InteractionResolution result = system.evaluate(
            feature,
            new EnvironmentalInteractionSystem.InteractionProfile(Set.of(), Map.of())
        );

        assertFalse(result.canInteract());
    }
}
