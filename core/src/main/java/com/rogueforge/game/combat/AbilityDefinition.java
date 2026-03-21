package com.rogueforge.game.combat;

/**
 * Represents the definition of an ability - its properties, cooldown, effects, and targets.
 * This is the static definition; AbilityInstance tracks runtime state (cooldown).
 */
public class AbilityDefinition {

    /**
     * Type of ability effect
     */
    public enum AbilityType {
        DAMAGE,
        HEAL,
        BUFF,
        DEBUFF,
        UTILITY
    }

    /**
     * Who the ability targets
     */
    public enum TargetType {
        SELF,
        SINGLE_ENEMY,
        ALL_ENEMIES,
        SINGLE_ALLY,
        ALL_ALLIES
    }

    private String id;
    private String name;
    private AbilityType type;
    private TargetType targetType;
    private float cooldown;           // in seconds
    private float power;              // strength of effect (damage, healing, buff amount)
    private float duration;           // for buffs/debuffs, 0 for instant
    private String description;

    /**
     * No-arg constructor for JSON serialization
     */
    public AbilityDefinition() {
    }

    /**
     * Full constructor
     */
    public AbilityDefinition(String id, String name, AbilityType type, TargetType targetType,
                           float cooldown, float power, float duration, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.targetType = targetType;
        this.cooldown = cooldown;
        this.power = power;
        this.duration = duration;
        this.description = description;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AbilityType getType() {
        return type;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public float getCooldown() {
        return cooldown;
    }

    public float getPower() {
        return power;
    }

    public float getDuration() {
        return duration;
    }

    public String getDescription() {
        return description;
    }

    // Setters (for JSON deserialization)
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(AbilityType type) {
        this.type = type;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public void setCooldown(float cooldown) {
        this.cooldown = cooldown;
    }

    public void setPower(float power) {
        this.power = power;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "AbilityDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", targetType=" + targetType +
                ", cooldown=" + cooldown +
                ", power=" + power +
                ", duration=" + duration +
                ", description='" + description + '\'' +
                '}';
    }
}
