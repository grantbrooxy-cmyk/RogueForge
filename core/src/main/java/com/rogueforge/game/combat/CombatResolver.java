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

    public int resolvePhysicalDamage(BattleCombatant attacker, BattleCombatant defender, float actionMultiplier) {
        return resolvePhysicalDamage(attacker, defender, actionMultiplier, 1f);
    }

    public int resolvePhysicalDamage(BattleCombatant attacker, BattleCombatant defender, float actionMultiplier, float weaponMultiplier) {
        if (attacker == null || defender == null) {
            return 0;
        }
        float baseDamage = (attacker.getStrength() * actionMultiplier)
            * Math.max(1f, weaponMultiplier)
            * attacker.getStatusEffectManager().getPhysicalDamageDealtMultiplier()
            - (defender.getEffectiveStamina() * DEFENSE_REDUCTION_FACTOR);
        float finalDamage = Math.max(MIN_DAMAGE, applyVariance(baseDamage));
        if (Math.random() < getCritChance(attacker)) {
            finalDamage *= 1.5f;
        }
        finalDamage *= defender.getStatusEffectManager().getPhysicalDamageTakenMultiplier();
        return Math.max(1, Math.round(finalDamage));
    }

    public int resolveAbilityDamage(BattleCombatant caster, BattleCombatant target, AbilityDefinition ability) {
        return resolveAbilityDamage(caster, target, ability, 1f);
    }

    public int resolveAbilityDamage(BattleCombatant caster, BattleCombatant target, AbilityDefinition ability, float proficiencyMultiplier) {
        if (caster == null || target == null || ability == null) {
            return 0;
        }
        float offense = (caster.getStrength() * 0.6f) + (caster.getIntelligence() * 0.4f);
        float baseDamage = ability.getPower()
            * Math.max(1f, proficiencyMultiplier)
            * Math.max(1f, offense)
            / Math.max(1f, target.getEffectiveStamina());
        float multiplier = ElementalSystem.getMultiplier(ability.getElement(), target);
        if (multiplier < 0f) {
            int healing = Math.max(1, Math.round(Math.abs(baseDamage)));
            target.heal(healing);
            return -healing;
        }
        float finalDamage = Math.max(MIN_DAMAGE, applyVariance(baseDamage * multiplier));
        finalDamage *= target.getStatusEffectManager().getAbilityDamageTakenMultiplier();
        return Math.max(1, Math.round(finalDamage));
    }

    public int resolveHealing(BattleCombatant caster, AbilityDefinition ability) {
        return resolveHealing(caster, ability, 1f);
    }

    public int resolveHealing(BattleCombatant caster, AbilityDefinition ability, float proficiencyMultiplier) {
        if (caster == null || ability == null) {
            return 0;
        }
        float healAmount = ability.getPower()
            * ((caster.getIntelligence() * 0.8f) + (caster.getStrength() * 0.2f))
            * 0.08f
            * Math.max(1f, proficiencyMultiplier);
        return Math.max(1, Math.round(healAmount));
    }

    public void applyDamage(BattleCombatant defender, int damage) {
        if (defender == null || damage == 0) {
            return;
        }
        if (damage < 0) {
            defender.heal(Math.abs(damage));
            return;
        }
        defender.applyDirectDamage(damage);
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

    private float applyVariance(float value) {
        float variance = 0.95f + ((float) Math.random() * 0.1f);
        return value * variance;
    }

    private float getCritChance(BattleCombatant attacker) {
        return Math.min(0.3f, 0.05f + (attacker.getEffectiveSpeed() / 1000f));
    }
}
