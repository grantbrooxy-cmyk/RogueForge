package com.rogueforge.game.combat;

import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.event.DamageDealtEvent;
import com.rogueforge.game.event.EntityKilledEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CombatResolverRegressionTest {

    @Test
    void lethalDamageQueuesDamageBeforeKillOnce() {
        EventBus bus = new EventBus();
        List<String> eventOrder = new ArrayList<>();
        bus.subscribe(DamageDealtEvent.class, event -> eventOrder.add("damage"));
        bus.subscribe(EntityKilledEvent.class, event -> eventOrder.add("kill"));
        CombatResolver resolver = new CombatResolver(bus);

        BattleCombatant attacker = new PlayerCombatant(
            "attacker", "Attacker", 0, "ALLY", "ALLY", "Striker",
            100f, 100f, 20f, 25f, 10f, 10f,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
            0, 0, "attacker-ref", List.of()
        );
        BattleCombatant target = new MonsterCombatant(
            "target", "Target", 0, "C", "AI", "Enemy",
            5f, 5f, 10f, 10f, 10f, 5f,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
            0, 0, "target-ref", List.of()
        );

        int applied = resolver.applyResolvedDamage(attacker, target, 7);
        bus.processQueuedEvents();

        assertEquals(7, applied);
        assertEquals(List.of("damage", "kill"), eventOrder);
        assertEquals(0f, target.getHealth());
    }

    @Test
    void abilityDamageResultCanBeAppliedWithoutDroppingEventDispatch() {
        EventBus bus = new EventBus();
        RecordingSubscriber subscriber = new RecordingSubscriber();
        bus.subscribe(DamageDealtEvent.class, event -> subscriber.damageEvent = event);
        CombatResolver resolver = new CombatResolver(bus);

        BattleCombatant caster = new PlayerCombatant(
            "caster", "Caster", 0, "ALLY", "ALLY", "Support",
            100f, 100f, 20f, 18f, 30f, 12f,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
            0, 0, "caster-ref", List.of()
        );
        BattleCombatant target = new MonsterCombatant(
            "enemy", "Enemy", 0, "C", "AI", "Enemy",
            50f, 50f, 10f, 10f, 10f, 10f,
            new ArrayList<>(), List.of(Element.FIRE), new ArrayList<>(), new ArrayList<>(),
            0, 0, "enemy-ref", List.of()
        );
        AbilityDefinition ability = new AbilityDefinition(
            "flame", "Flame", AbilityDefinition.AbilityType.DAMAGE,
            AbilityDefinition.TargetType.SINGLE_ENEMY, 8f, 24f, 0f, "burn"
        );
        ability.setElement(Element.FIRE);

        DamageResult result = resolver.resolveAndApplyAbilityDamage(caster, target, ability, 1f);
        bus.processQueuedEvents();

        assertNotNull(subscriber.damageEvent);
        assertEquals(result.damage(), Math.round(subscriber.damageEvent.getDamage()));
    }

    private static class RecordingSubscriber {
        private DamageDealtEvent damageEvent;
    }
}
