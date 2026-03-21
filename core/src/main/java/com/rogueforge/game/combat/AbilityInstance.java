package com.rogueforge.game.combat;

import com.rogueforge.game.progression.AbilityProgressionState;
import com.rogueforge.game.progression.ProficiencyTracker;

/**
 * Runtime instance of an ability tracking its cooldown state.
 * Each entity has instances of their abilities that track cooldown independently.
 */
public class AbilityInstance {

    private AbilityDefinition definition;
    private AbilityProgressionState progressionState;
    private float currentCooldown;  // Counts down to 0

    /**
     * Creates an ability instance from its definition
     *
     * @param definition The ability definition
     */
    public AbilityInstance(AbilityDefinition definition) {
        this(definition, null);
    }

    public AbilityInstance(AbilityDefinition definition, AbilityProgressionState progressionState) {
        this.definition = definition;
        this.progressionState = progressionState;
        this.currentCooldown = 0f;
    }

    /**
     * Checks if the ability is ready to use (cooldown expired)
     *
     * @return true if currentCooldown <= 0
     */
    public boolean isReady() {
        return currentCooldown <= 0f;
    }

    /**
     * Marks the ability as used, starting its cooldown
     */
    public void use() {
        this.currentCooldown = getEffectiveCooldown();
    }

    /**
     * Updates the cooldown timer, reducing it by delta
     *
     * @param delta Time elapsed in seconds
     */
    public void update(float delta) {
        if (currentCooldown > 0f) {
            currentCooldown -= delta;
            if (currentCooldown < 0f) {
                currentCooldown = 0f;
            }
        }
    }

    /**
     * Gets the cooldown as a percentage of total cooldown
     *
     * @return Value between 0.0 and 1.0 representing cooldown progress
     */
    public float getCooldownPercent() {
        if (definition.getCooldown() <= 0f) {
            return 0f;
        }
        return currentCooldown / definition.getCooldown();
    }

    /**
     * Gets the current cooldown remaining in seconds
     *
     * @return Current cooldown value
     */
    public float getCurrentCooldown() {
        return currentCooldown;
    }

    public void setCurrentCooldown(float currentCooldown) {
        this.currentCooldown = Math.max(0f, currentCooldown);
    }

    public float getEffectiveCooldown() {
        return definition.getCooldown() * ProficiencyTracker.cooldownMultiplier(getProficiencyLevel());
    }

    /**
     * Gets the ability definition
     *
     * @return The definition
     */
    public AbilityDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(AbilityDefinition definition) {
        if (definition != null) {
            this.definition = definition;
        }
    }

    public AbilityProgressionState getProgressionState() {
        return progressionState;
    }

    public void setProgressionState(AbilityProgressionState progressionState) {
        this.progressionState = progressionState;
    }

    public int getProficiencyLevel() {
        return progressionState != null ? progressionState.getProficiencyLevel() : 1;
    }

    public int getProficiencyXp() {
        return progressionState != null ? progressionState.getProficiencyXp() : 0;
    }

    public int addProficiencyXp(int amount) {
        return progressionState != null ? progressionState.addXp(amount) : 0;
    }

    public float getPowerMultiplier() {
        return ProficiencyTracker.abilityPowerMultiplier(getProficiencyLevel());
    }

    @Override
    public String toString() {
        return "AbilityInstance{" +
                "definition=" + definition.getName() +
                ", currentCooldown=" + currentCooldown +
                ", isReady=" + isReady() +
                '}';
    }
}
