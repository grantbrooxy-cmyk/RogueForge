package com.rogueforge.game.screen;

import com.rogueforge.game.combat.AbilityDefinition;
import com.rogueforge.game.combat.Element;
import com.rogueforge.game.combat.StatusEffectType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BattleScreenBossPhaseTest {

    @Test
    void phasedBossThresholdsAdvanceAtExpectedHealthBands() {
        assertEquals(1, BattleScreen.determineBossPhase(BattleScreen.ORIGIN_CORE_ID, 1, 80f, 100f));
        assertEquals(2, BattleScreen.determineBossPhase(BattleScreen.ORIGIN_CORE_ID, 1, 66f, 100f));
        assertEquals(3, BattleScreen.determineBossPhase(BattleScreen.ORIGIN_CORE_ID, 2, 33f, 100f));

        assertEquals(1, BattleScreen.determineBossPhase(BattleScreen.VOLT_SPECTER_ID, 1, 51f, 100f));
        assertEquals(2, BattleScreen.determineBossPhase(BattleScreen.VOLT_SPECTER_ID, 1, 50f, 100f));

        assertEquals(1, BattleScreen.determineBossPhase(BattleScreen.NULL_WARDEN_ID, 1, 51f, 100f));
        assertEquals(2, BattleScreen.determineBossPhase(BattleScreen.NULL_WARDEN_ID, 1, 50f, 100f));

        assertEquals(1, BattleScreen.determineBossPhase(BattleScreen.THE_UNMAKER_ID, 1, 46f, 100f));
        assertEquals(2, BattleScreen.determineBossPhase(BattleScreen.THE_UNMAKER_ID, 1, 45f, 100f));
    }

    @Test
    void phasedBossesSwapToDistinctEnemyAbilities() {
        AbilityDefinition voltAbility = BattleScreen.createEnemyAbilityFor(BattleScreen.VOLT_SPECTER_ID, 2);
        assertEquals("Thunder Refrain", voltAbility.getName());
        assertEquals(Element.LIGHTNING, voltAbility.getElement());
        assertEquals(StatusEffectType.PARALYZE, voltAbility.getAppliedStatus());

        AbilityDefinition nullAbility = BattleScreen.createEnemyAbilityFor(BattleScreen.NULL_WARDEN_ID, 2);
        assertEquals("Null Lock", nullAbility.getName());
        assertEquals(Element.WATER, nullAbility.getElement());
        assertEquals(StatusEffectType.SILENCE, nullAbility.getAppliedStatus());

        AbilityDefinition unmakerAbility = BattleScreen.createEnemyAbilityFor(BattleScreen.THE_UNMAKER_ID, 2);
        assertEquals("Ruin Wake", unmakerAbility.getName());
        assertEquals(Element.FIRE, unmakerAbility.getElement());
        assertEquals(StatusEffectType.WEAKEN, unmakerAbility.getAppliedStatus());

        assertTrue(BattleScreen.isPhasedBoss(BattleScreen.VOLT_SPECTER_ID));
        assertTrue(BattleScreen.isPhasedBoss(BattleScreen.NULL_WARDEN_ID));
        assertTrue(BattleScreen.isPhasedBoss(BattleScreen.THE_UNMAKER_ID));
    }
}
