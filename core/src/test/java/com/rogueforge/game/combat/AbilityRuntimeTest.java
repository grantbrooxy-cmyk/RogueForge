package com.rogueforge.game.combat;

import com.rogueforge.game.progression.AbilityProgressionState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityRuntimeTest {

    @Test
    void abilityDefinitionDefaultsAndSecondaryStatusFallbacksWork() {
        AbilityDefinition definition = new AbilityDefinition();
        definition.setElement(null);
        definition.setSpeedCost(0);
        definition.setWeaponType(null);
        definition.setMasteryUpgradeId(null);
        definition.setUniqueSkillId(null);
        definition.setStatusTurns(3);

        assertEquals(Element.NONE, definition.getElement());
        assertEquals(80, definition.getSpeedCost());
        assertEquals(WeaponType.NONE, definition.getWeaponType());
        assertEquals("", definition.getMasteryUpgradeId());
        assertEquals("", definition.getUniqueSkillId());
        assertEquals(3, definition.getSecondaryStatusTurns());
    }

    @Test
    void abilityInstanceTracksCooldownAndProgressionBonuses() {
        AbilityDefinition definition = new AbilityDefinition(
            "heal_pulse",
            "Heal Pulse",
            AbilityDefinition.AbilityType.HEAL,
            AbilityDefinition.TargetType.ALL_ALLIES,
            10f,
            30f,
            0f,
            "Heals allies"
        );
        AbilityProgressionState progression = new AbilityProgressionState("heal_pulse");
        progression.setProficiencyLevel(4);

        AbilityInstance instance = new AbilityInstance(definition, progression);
        instance.use();

        assertFalse(instance.isReady());
        assertEquals(9.1f, instance.getEffectiveCooldown(), 0.001f);
        assertEquals(0.91f, instance.getCooldownPercent(), 0.001f);
        assertEquals(1.12f, instance.getPowerMultiplier(), 0.001f);

        instance.update(20f);

        assertTrue(instance.isReady());
        assertEquals(0f, instance.getCurrentCooldown(), 0.001f);
    }
}
