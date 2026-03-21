package com.rogueforge.game.combat;

import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.event.DamageDealtEvent;
import com.rogueforge.game.event.EntityKilledEvent;

import java.util.List;

/**
 * Processes hit events, applies damage calculations, and manages combat outcomes.
 */
public class CombatResolver {
    private static final float MIN_DAMAGE = 1.0f;
    private static final float DEFENSE_REDUCTION_FACTOR = 0.5f;

    private EventBus eventBus;

    public CombatResolver(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Resolves a hit between attacker and defender, calculating damage.
     * Damage formula: baseDamage = attack - (defense * 0.5), minimum 1
     *
     * @param attackerStats The attacker's combat stats
     * @param defenderStats The defender's combat stats
     * @return The calculated damage value
     */
    public float resolveHit(CombatStats attackerStats, CombatStats defenderStats) {
        float baseDamage = attackerStats.getAttack() - (defenderStats.getDefense() * DEFENSE_REDUCTION_FACTOR);
        float finalDamage = Math.max(baseDamage, MIN_DAMAGE);

        return finalDamage;
    }

    /**
     * Processes all active status effects, advancing their timers and executing their effects.
     *
     * @param effects List of active status effects
     * @param delta Time elapsed since last frame in seconds
     */
    public void processStatusEffects(List<StatusEffect> effects, float delta) {
        if (effects == null || effects.isEmpty()) {
            return;
        }

        // Update all effects and remove expired ones
        for (int i = effects.size() - 1; i >= 0; i--) {
            StatusEffect effect = effects.get(i);
            effect.update(delta);

            if (effect.isExpired()) {
                effects.remove(i);
            }
        }
    }

    /**
     * Box2D contact listener callback for when two entities collide.
     * This is a placeholder for integration with Box2D physics.
     *
     * @param entityA First colliding entity
     * @param entityB Second colliding entity
     */
    public void onContactBegin(Object entityA, Object entityB) {
        // TODO: Implement Box2D contact listener integration
        // - Check if either entity is an attacker
        // - Calculate hit resolution
        // - Apply damage to defender
        // - Trigger damage events
    }

    /**
     * Applies damage from attacker to defender, resolving the hit and updating HP.
     * Fires DamageDealtEvent and checks for death.
     *
     * @param attacker The attacker's combat stats
     * @param defender The defender's combat stats
     * @param attackerSource The source entity of the attack
     * @param defenderSource The target entity taking damage
     * @return the actual damage dealt
     */
    public float applyDamage(CombatStats attacker, CombatStats defender, Object attackerSource, Object defenderSource) {
        float damage = resolveHit(attacker, defender);
        defender.takeDamage(damage);

        DamageDealtEvent event = new DamageDealtEvent(attackerSource, defenderSource, damage);
        eventBus.fire(event);

        checkDeath(defender, defenderSource);
        return damage;
    }

    /**
     * Checks if a combatant is dead and fires EntityKilledEvent.
     *
     * @param stats The combat stats to check
     * @param entity The entity associated with these stats
     */
    public void checkDeath(CombatStats stats, Object entity) {
        if (!stats.isAlive()) {
            EntityKilledEvent event = new EntityKilledEvent(entity);
            eventBus.fire(event);
        }
    }

    /**
     * Convenience overload for distance-based combat (no Box2D needed).
     * Takes raw attack/defense values and applies damage to a target's CombatStats.
     *
     * @param attackPower The base attack power value
     * @param defender The defender's combat stats
     * @param source The source entity of the damage
     * @return the actual damage dealt
     */
    public float applyRawDamage(float attackPower, CombatStats defender, Object source) {
        float baseDamage = attackPower - (defender.getDefense() * DEFENSE_REDUCTION_FACTOR);
        float damage = Math.max(baseDamage, MIN_DAMAGE);
        defender.takeDamage(damage);

        DamageDealtEvent event = new DamageDealtEvent(source, null, damage);
        eventBus.fire(event);

        checkDeath(defender, null);
        return damage;
    }
}
