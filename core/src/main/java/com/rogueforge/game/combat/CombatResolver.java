package com.rogueforge.game.combat;

import com.badlogic.gdx.utils.Pools;
import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.core.EventPriority;
import com.rogueforge.game.event.DamageDealtEvent;
import com.rogueforge.game.event.EntityKilledEvent;

import java.util.List;

/**
 * Processes hit events, applies damage calculations, and manages combat outcomes.
 */
public class CombatResolver {
    private static final float MIN_DAMAGE = 1.0f;
    private static final float DEFENSE_REDUCTION_FACTOR = 0.5f;

    private final EventBus eventBus;
    private final AbilityEffectPipeline abilityEffectPipeline;

    public CombatResolver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.abilityEffectPipeline = new AbilityEffectPipeline(List.of(
            new OverdriveLinkEffectStep(),
            new ElementalBreakEffectStep(),
            new ElementalMultiplierEffectStep(),
            new AbilityDamageTakenEffectStep()
        ));
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

        // BLIND: physical attacks have a chance to miss entirely.
        float hitChance = attacker.getStatusEffectManager().getPhysicalHitChanceMultiplier();
        if (hitChance < 1f && Math.random() >= hitChance) {
            // 0 signals a miss to the caller; caller should log "X missed!"
            return 0;
        }

        float baseDamage = (attacker.getStrength() * actionMultiplier)
            * Math.max(1f, weaponMultiplier)
            * attacker.getStatusEffectManager().getPhysicalDamageDealtMultiplier()
            - (defender.getEffectiveStamina() * DEFENSE_REDUCTION_FACTOR);
        if (attacker.hasUniqueBoost("OVERDRIVE_LINK") && attacker.getHealth() >= (attacker.getMaxHealth() * 0.7f)) {
            baseDamage *= 1.15f;
        }
        float finalDamage = Math.max(MIN_DAMAGE, applyVariance(baseDamage));
        if (Math.random() < getCritChance(attacker)) {
            finalDamage *= 1.5f;
        }
        finalDamage *= defender.getStatusEffectManager().getPhysicalDamageTakenMultiplier();
        return Math.max(1, Math.round(finalDamage));
    }

    public DamageResult resolveAbilityDamage(BattleCombatant caster, BattleCombatant target, AbilityDefinition ability) {
        return resolveAbilityDamage(caster, target, ability, 1f);
    }

    public DamageResult resolveAbilityDamage(BattleCombatant caster, BattleCombatant target, AbilityDefinition ability, float proficiencyMultiplier) {
        if (caster == null || target == null || ability == null) {
            return new DamageResult(0, false);
        }
        float offense = (caster.getStrength() * 0.6f) + (caster.getIntelligence() * 0.4f);
        float baseDamage = ability.getPower()
            * Math.max(1f, proficiencyMultiplier)
            * Math.max(1f, offense)
            / Math.max(1f, target.getEffectiveStamina());

        AbilityEffectContext context = abilityEffectPipeline.apply(
            new AbilityEffectContext(caster, target, ability, proficiencyMultiplier, baseDamage)
        );
        if (context.isAbsorbed()) {
            // Absorb: heals the target instead of damaging.
            int healing = Math.max(1, Math.round(Math.abs(context.getBaseDamage())));
            target.heal(healing);
            return new DamageResult(-healing, context.isElementalBreak());
        }
        float finalDamage = Math.max(MIN_DAMAGE, applyVariance(context.getBaseDamage() * context.getMultiplier()));
        int damage = Math.max(1, Math.round(finalDamage));
        return new DamageResult(damage, context.isElementalBreak());
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
        if (caster.isCombatClass("Support")) {
            healAmount *= 1.2f;
        }
        return Math.max(1, Math.round(healAmount));
    }

    public int applyDamage(BattleCombatant defender, int damage) {
        return applyDamage(null, defender, damage);
    }

    public int applyDamage(BattleCombatant attacker, BattleCombatant defender, int damage) {
        if (defender == null || damage == 0) {
            return 0;
        }
        if (damage < 0) {
            defender.heal(Math.abs(damage));
            return damage;
        }
        return applyResolvedDamage(attacker, defender, damage);
    }

    public int applyResolvedDamage(BattleCombatant attacker, BattleCombatant defender, int damage) {
        if (defender == null || damage <= 0) {
            return 0;
        }
        boolean wasAlive = defender.isAlive();
        defender.applyDirectDamage(damage);
        eventBus.defer(() -> {
            queueDamageEvent(resolveEventReference(attacker), resolveEventReference(defender), damage);
            if (wasAlive && !defender.isAlive()) {
                queueEntityKilledEvent(resolveEventReference(defender));
            }
        });
        return damage;
    }

    public int resolveAndApplyPhysicalDamage(BattleCombatant attacker, BattleCombatant defender,
                                             float actionMultiplier, float weaponMultiplier) {
        int damage = resolvePhysicalDamage(attacker, defender, actionMultiplier, weaponMultiplier);
        return applyDamage(attacker, defender, damage);
    }

    public DamageResult resolveAndApplyAbilityDamage(BattleCombatant caster, BattleCombatant target,
                                                     AbilityDefinition ability, float proficiencyMultiplier) {
        DamageResult result = resolveAbilityDamage(caster, target, ability, proficiencyMultiplier);
        int applied = applyDamage(caster, target, result.damage());
        return new DamageResult(applied, result.elementalBreak());
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
        fireDamageEvent(attackerSource, defenderSource, damage);
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
            EntityKilledEvent event = Pools.obtain(EntityKilledEvent.class).init(entity);
            eventBus.queue(event, EventPriority.HIGH);
        }
    }

    /**
     * Convenience overload for non-battle damage sources.
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
        fireDamageEvent(source, null, damage);
        checkDeath(defender, null);
        return damage;
    }

    private void fireDamageEvent(Object source, Object target, float damage) {
        eventBus.fire(Pools.obtain(DamageDealtEvent.class).init(source, target, damage));
    }

    private void queueDamageEvent(Object source, Object target, float damage) {
        eventBus.queue(Pools.obtain(DamageDealtEvent.class).init(source, target, damage), EventPriority.HIGH);
    }

    private void queueEntityKilledEvent(Object entity) {
        eventBus.queue(Pools.obtain(EntityKilledEvent.class).init(entity), EventPriority.NORMAL);
    }

    private Object resolveEventReference(BattleCombatant combatant) {
        if (combatant == null) {
            return null;
        }
        return combatant.getSourceReference() != null ? combatant.getSourceReference() : combatant;
    }

    private float applyVariance(float value) {
        float variance = 0.95f + ((float) Math.random() * 0.1f);
        return value * variance;
    }

    private float getCritChance(BattleCombatant attacker) {
        float critChance = 0.05f + (attacker.getEffectiveSpeed() / 1000f);
        if (attacker != null && attacker.isCombatClass("Striker")) {
            critChance += 0.08f;
        }
        return Math.min(0.4f, critChance);
    }
}
