package com.rogueforge.game.combat;

/**
 * Result of executing an ability.
 * Contains information about what happened when the ability was used.
 */
public class AbilityResult {

    private String abilityName;
    private AbilityDefinition.AbilityType type;
    private float value;            // damage dealt, healing done, buff power, etc.
    private boolean success;

    /**
     * Constructor for ability result
     *
     * @param abilityName Name of the ability that was used
     * @param type Type of ability effect
     * @param value The numeric result value (damage, healing, etc.)
     * @param success Whether the ability successfully executed
     */
    public AbilityResult(String abilityName, AbilityDefinition.AbilityType type, float value, boolean success) {
        this.abilityName = abilityName;
        this.type = type;
        this.value = value;
        this.success = success;
    }

    public String getAbilityName() {
        return abilityName;
    }

    public AbilityDefinition.AbilityType getType() {
        return type;
    }

    public float getValue() {
        return value;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "AbilityResult{" +
                "abilityName='" + abilityName + '\'' +
                ", type=" + type +
                ", value=" + value +
                ", success=" + success +
                '}';
    }
}
