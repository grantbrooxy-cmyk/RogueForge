package com.rogueforge.game.combat;

import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.event.AbilityUsedEvent;

/**
 * Executes abilities and resolves their effects on targets.
 * Handles cooldown checking, damage calculations, and event firing.
 */
public class AbilityExecutor {

    private static final float MIN_DAMAGE = 1.0f;
    private static final float DEFENSE_REDUCTION_FACTOR = 0.5f;

    private EventBus eventBus;

    /**
     * Constructor
     *
     * @param eventBus Event bus for firing ability events
     */
    public AbilityExecutor(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Executes an ability if it's ready, applying its effects to the target.
     * Returns null if the ability is not ready (still on cooldown).
     *
     * @param ability The ability instance to execute
     * @param caster The entity using the ability
     * @param target The target entity (may be null for SELF targeting)
     * @return AbilityResult if successful, null if not ready
     */
    public AbilityResult execute(AbilityInstance ability, CombatStats caster, CombatStats target) {
        // Check if ability is ready
        if (!ability.isReady()) {
            return null;
        }

        AbilityDefinition definition = ability.getDefinition();
        AbilityResult result = null;

        // Start cooldown
        ability.use();

        // Execute based on ability type
        AbilityDefinition.AbilityType type = definition.getType();
        switch (type) {
            case DAMAGE:
                result = executeDamage(definition, caster, target);
                break;
            case HEAL:
                result = executeHeal(definition, target);
                break;
            case BUFF:
                result = executeBuff(definition);
                break;
            case DEBUFF:
                result = executeDebuff(definition);
                break;
            case UTILITY:
                result = executeUtility(definition);
                break;
            default:
                result = new AbilityResult(definition.getName(), type, 0f, false);
        }

        // Fire event
        if (eventBus != null && result != null) {
            AbilityUsedEvent event = new AbilityUsedEvent(definition.getId(), caster.toString(), result);
            eventBus.fire(event);
        }

        return result;
    }

    /**
     * Executes a damage ability.
     * Uses damage formula: power - (target defense * 0.5), minimum 1
     */
    private AbilityResult executeDamage(AbilityDefinition definition, CombatStats caster, CombatStats target) {
        if (target == null) {
            return new AbilityResult(definition.getName(), definition.getType(), 0f, false);
        }

        // Calculate damage: power - (defense * 0.5), min 1
        float baseDamage = definition.getPower() - (target.getDefense() * DEFENSE_REDUCTION_FACTOR);
        float finalDamage = Math.max(baseDamage, MIN_DAMAGE);

        // Apply damage to target
        target.takeDamage(finalDamage);

        return new AbilityResult(definition.getName(), definition.getType(), finalDamage, true);
    }

    /**
     * Executes a healing ability.
     */
    private AbilityResult executeHeal(AbilityDefinition definition, CombatStats target) {
        if (target == null) {
            return new AbilityResult(definition.getName(), definition.getType(), 0f, false);
        }

        // Heal target by power amount
        float healAmount = definition.getPower();
        target.heal(healAmount);

        return new AbilityResult(definition.getName(), definition.getType(), healAmount, true);
    }

    /**
     * Executes a buff ability.
     * Returns a result indicating the buff was applied.
     */
    private AbilityResult executeBuff(AbilityDefinition definition) {
        float power = definition.getPower();
        float duration = definition.getDuration();

        return new AbilityResult(
            definition.getName(),
            definition.getType(),
            power,
            true
        );
    }

    /**
     * Executes a debuff ability.
     * Returns a result indicating the debuff was applied.
     */
    private AbilityResult executeDebuff(AbilityDefinition definition) {
        float power = definition.getPower();
        float duration = definition.getDuration();

        return new AbilityResult(
            definition.getName(),
            definition.getType(),
            power,
            true
        );
    }

    /**
     * Executes a utility ability.
     * Returns a result indicating the utility effect was applied.
     */
    private AbilityResult executeUtility(AbilityDefinition definition) {
        float power = definition.getPower();
        float duration = definition.getDuration();

        return new AbilityResult(
            definition.getName(),
            definition.getType(),
            power,
            true
        );
    }
}
