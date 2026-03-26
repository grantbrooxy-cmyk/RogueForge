package com.rogueforge.game.combat;

import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.event.DamageDealtEvent;
import com.rogueforge.game.event.EntityKilledEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatSystemsTest {

    @Test
    void combatStatsClampDamageHealingAndAliveState() {
        CombatStats stats = new CombatStats(10f, 5f, 100f);

        stats.takeDamage(30f);
        stats.heal(10f);
        stats.setMaxHp(50f);

        assertEquals(50f, stats.getHp());
        stats.setHp(-5f);
        assertEquals(0f, stats.getHp());
        assertFalse(stats.isAlive());
    }

    @Test
    void bestiaryManagerCapsAndCopiesScanData() {
        BestiaryManager manager = new BestiaryManager();

        assertEquals(1, manager.recordScan("slime", 0));
        assertEquals(3, manager.recordScan("slime", 5));

        assertEquals(3, manager.getScanLevel("slime"));
        assertEquals(1, manager.exportData().size());
    }

    @Test
    void battleCombatantTracksClassBoostsBreaksAndHealing() {
        BattleCombatant combatant = combatant("ally", "Vanguard/Scout", 100f, 30f, 20f, 12f, 14f);

        assertTrue(combatant.isCombatClass("Vanguard"));
        assertTrue(combatant.isCombatClass("Scout"));
        assertFalse(combatant.isCombatClass("Support"));

        assertEquals(1, combatant.registerElementalHit(Element.FIRE));
        assertEquals(2, combatant.registerElementalHit(Element.FIRE));
        assertEquals(3, combatant.registerElementalHit(Element.FIRE));
        assertTrue(combatant.hasElementalBreak(Element.FIRE));

        combatant.applyDirectDamage(20f);
        combatant.heal(5f);

        assertEquals(85f, combatant.getHealth());
    }

    @Test
    void elementalSystemRecognizesWeaknessResistanceAbsorbAndBreak() {
        BattleCombatant target = new BattleCombatant(
            "enemy",
            "Drake",
            false,
            0,
            "B",
            "AGGRO",
            "Enemy",
            100f,
            100f,
            10f,
            14f,
            12f,
            10f,
            new ArrayList<>(),
            List.of(Element.FIRE),
            List.of(Element.ICE),
            List.of(Element.WATER),
            0,
            0,
            null,
            List.of()
        );
        target.registerElementalHit(Element.FIRE);
        target.registerElementalHit(Element.FIRE);
        target.registerElementalHit(Element.FIRE);

        assertEquals(1.5f, ElementalSystem.getMultiplier(Element.FIRE, target));
        assertEquals(0.5f, ElementalSystem.getMultiplier(Element.ICE, target));
        assertEquals(-1f, ElementalSystem.getMultiplier(Element.WATER, target));
        assertEquals("absorbed", ElementalSystem.describeHit(-1f));
    }

    @Test
    void statusEffectManagerAppliesDotsBuffsRestrictionsAndExpiry() {
        BattleCombatant target = combatant("target", "Support", 100f, 30f, 20f, 12f, 20f);
        StatusEffectManager manager = target.getStatusEffectManager();
        manager.apply(StatusEffectType.BURN, 1);
        manager.apply(StatusEffectType.POISON, 1);
        manager.apply(StatusEffectType.REGEN, 1);
        manager.apply(StatusEffectType.BERSERK, 1);
        manager.apply(StatusEffectType.DEFENDING, 1);
        manager.apply(StatusEffectType.SHELL, 1);
        manager.apply(StatusEffectType.PROTECT, 1);
        manager.apply(StatusEffectType.WEAKEN, 1);
        manager.apply(StatusEffectType.SLOW, 1);
        manager.apply(StatusEffectType.HASTE, 1);
        manager.apply(StatusEffectType.SILENCE, 1);
        manager.apply(StatusEffectType.BLEED, 1);

        List<String> beginLog = manager.beginTurn(target);
        List<String> actionLog = manager.onActionTaken(target);
        assertTrue(beginLog.stream().anyMatch(line -> line.contains("scorched")));
        assertTrue(beginLog.stream().anyMatch(line -> line.contains("poison")));
        assertTrue(beginLog.stream().anyMatch(line -> line.contains("regenerates")));
        assertTrue(beginLog.stream().anyMatch(line -> line.contains("berserk")));
        assertTrue(actionLog.get(0).contains("bleeds"));
        assertEquals(1.125f, manager.getPhysicalDamageDealtMultiplier(), 0.001f);
        assertEquals(0.525f, manager.getAbilityDamageTakenMultiplier(), 0.001f);
        assertEquals(0.35f, manager.getPhysicalDamageTakenMultiplier(), 0.001f);
        assertEquals(0.75f, manager.getSpeedMultiplier(), 0.001f);
        assertFalse(manager.canUseAbilities());
        assertTrue(manager.canAttackOnly());

        List<String> endLog = manager.endTurn(target);
        assertTrue(endLog.stream().allMatch(line -> line.contains("no longer affected")));
    }

    @Test
    void turnTimelineOrdersByTickSpeedAndRemovals() {
        BattleCombatant fast = combatant("fast", "Scout", 50f, 40f, 10f, 10f, 10f);
        BattleCombatant slow = combatant("slow", "Vanguard", 50f, 10f, 10f, 10f, 10f);
        TurnTimeline timeline = new TurnTimeline();
        List<BattleCombatant> combatants = List.of(fast, slow);

        timeline.initialize(combatants);

        assertEquals("fast", timeline.getCurrentActor(combatants).getId());
        timeline.consumeTurn(fast, 80);
        assertEquals("slow", timeline.getCurrentActor(combatants).getId());
        assertEquals(4, timeline.getProjectedTurns(combatants, 4).size());

        timeline.remove("slow");
        assertEquals("fast", timeline.getCurrentActor(combatants).getId());
    }

    @Test
    void combatResolverHandlesDeterministicCasesEventsAndBreakEncoding() {
        RecordingSubscriber subscriber = new RecordingSubscriber();
        EventBus eventBus = new EventBus();
        eventBus.subscribe(subscriber);
        CombatResolver resolver = new CombatResolver(eventBus);

        CombatStats attackerStats = new CombatStats(12f, 1f, 20f);
        CombatStats defenderStats = new CombatStats(5f, 40f, 10f);
        assertEquals(1f, resolver.resolveHit(attackerStats, defenderStats));

        Object source = "source";
        Object target = "target";
        float applied = resolver.applyDamage(attackerStats, defenderStats, source, target);
        assertEquals(applied, subscriber.damageEvent.getDamage());
        assertEquals(source, subscriber.damageEvent.getSource());
        assertEquals(target, subscriber.damageEvent.getTarget());

        CombatStats doomed = new CombatStats(2f, 0f, 1f);
        resolver.applyRawDamage(10f, doomed, "hazard");
        assertEquals("hazard", subscriber.rawDamageEvent.getSource());
        assertEquals(null, subscriber.rawDamageEvent.getTarget());
        assertEquals(null, subscriber.killedEvent.getEntity());

        BattleCombatant caster = combatant("caster", "Support", 100f, 20f, 16f, 30f, 12f);
        BattleCombatant absorbed = new BattleCombatant(
            "absorbed",
            "Absorber",
            false,
            0,
            "C",
            "AI",
            "Enemy",
            20f,
            100f,
            10f,
            10f,
            10f,
            10f,
            new ArrayList<>(),
            List.of(),
            List.of(),
            List.of(Element.WATER),
            0,
            0,
            null,
            List.of()
        );
        AbilityDefinition healPulse = new AbilityDefinition(
            "heal_pulse",
            "Heal Pulse",
            AbilityDefinition.AbilityType.HEAL,
            AbilityDefinition.TargetType.ALL_ALLIES,
            10f,
            30f,
            0f,
            "heal"
        );
        healPulse.setElement(Element.WATER);

        int absorbedResult = resolver.resolveAbilityDamage(caster, absorbed, healPulse);
        assertTrue(absorbedResult < 0);
        assertTrue(absorbed.getHealth() > 20f);
        assertTrue(resolver.resolveHealing(caster, healPulse) > 0);

        BattleCombatant weakTarget = new BattleCombatant(
            "weak",
            "Weak Target",
            false,
            0,
            "C",
            "AI",
            "Enemy",
            100f,
            100f,
            10f,
            10f,
            10f,
            10f,
            new ArrayList<>(),
            List.of(Element.WATER),
            List.of(),
            List.of(),
            0,
            0,
            null,
            List.of()
        );

        resolver.resolveAbilityDamage(caster, weakTarget, healPulse);
        resolver.resolveAbilityDamage(caster, weakTarget, healPulse);
        int breakResult = resolver.resolveAbilityDamage(caster, weakTarget, healPulse);

        assertTrue(CombatResolver.wasElementalBreak(breakResult));
        assertTrue(CombatResolver.extractBreakDamage(breakResult) > 0);
    }

    private static BattleCombatant combatant(String id, String combatClass, float hp, float agility,
                                             float strength, float intelligence, float stamina) {
        return new BattleCombatant(
            id,
            id,
            true,
            0,
            "C",
            "ALLY",
            combatClass,
            hp,
            hp,
            agility,
            strength,
            intelligence,
            stamina,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            10,
            20,
            null,
            List.of("AUTO_REPAIR")
        );
    }

    private static class RecordingSubscriber {
        private DamageDealtEvent damageEvent;
        private DamageDealtEvent rawDamageEvent;
        private EntityKilledEvent killedEvent;

        void onDamageDealtEvent(DamageDealtEvent event) {
            if (event.getTarget() == null) {
                rawDamageEvent = event;
            } else {
                damageEvent = event;
            }
        }

        void onEntityKilledEvent(EntityKilledEvent event) {
            killedEvent = event;
        }
    }
}
