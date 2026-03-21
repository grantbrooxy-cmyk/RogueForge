package com.rogueforge.game.combat;

/**
 * Runtime instance of an ability tracking its cooldown state.
 * Each entity has instances of their abilities that track cooldown independently.
 */
public class AbilityInstance {

    private AbilityDefinition definition;
    private float currentCooldown;  // Counts down to 0

    /**
     * Creates an ability instance from its definition
     *
     * @param definition The ability definition
     */
    public AbilityInstance(AbilityDefinition definition) {
        this.definition = definition;
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
        this.currentCooldown = definition.getCooldown();
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

    /**
     * Gets the ability definition
     *
     * @return The definition
     */
    public AbilityDefinition getDefinition() {
        return definition;
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
