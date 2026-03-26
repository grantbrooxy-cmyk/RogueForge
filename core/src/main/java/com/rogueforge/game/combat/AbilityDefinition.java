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
    private Element element = Element.NONE;
    private int speedCost = 80;
    private StatusEffectType appliedStatus;
    private int statusTurns;
    private WeaponType weaponType = WeaponType.NONE;
    private String masteryUpgradeId;
    private String uniqueSkillId;

    /**
     * Some unique skills (Omniscience Eye, Gravity Well, Genesis Field) apply
     * two statuses at once. secondaryStatus is applied alongside appliedStatus.
     * secondaryStatusTurns defaults to statusTurns when not set.
     * secondaryTargetType overrides the ability's own targetType for the
     * secondary effect only (e.g. Gravity Well slows ALL_ENEMIES but applies
     * Protect to SELF).
     */
    private StatusEffectType secondaryStatus;
    private int secondaryStatusTurns;
    private TargetType secondaryTargetType;

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

    public Element getElement() {
        return element != null ? element : Element.NONE;
    }

    public int getSpeedCost() {
        return speedCost > 0 ? speedCost : 80;
    }

    public StatusEffectType getAppliedStatus() {
        return appliedStatus;
    }

    public int getStatusTurns() {
        return statusTurns;
    }

    public WeaponType getWeaponType() {
        return weaponType != null ? weaponType : WeaponType.NONE;
    }

    public String getMasteryUpgradeId() {
        return masteryUpgradeId != null ? masteryUpgradeId : "";
    }

    public String getUniqueSkillId() {
        return uniqueSkillId != null ? uniqueSkillId : "";
    }

    public StatusEffectType getSecondaryStatus() {
        return secondaryStatus;
    }

    /** Returns the secondary status duration, falling back to statusTurns if not explicitly set. */
    public int getSecondaryStatusTurns() {
        return secondaryStatusTurns > 0 ? secondaryStatusTurns : statusTurns;
    }

    /**
     * Returns the target type to use when applying the secondary status.
     * Null means "use the same target(s) as the primary effect".
     */
    public TargetType getSecondaryTargetType() {
        return secondaryTargetType;
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

    public void setElement(Element element) {
        this.element = element;
    }

    public void setSpeedCost(int speedCost) {
        this.speedCost = speedCost;
    }

    public void setAppliedStatus(StatusEffectType appliedStatus) {
        this.appliedStatus = appliedStatus;
    }

    public void setStatusTurns(int statusTurns) {
        this.statusTurns = statusTurns;
    }

    public void setWeaponType(WeaponType weaponType) {
        this.weaponType = weaponType;
    }

    public void setMasteryUpgradeId(String masteryUpgradeId) {
        this.masteryUpgradeId = masteryUpgradeId;
    }

    public void setUniqueSkillId(String uniqueSkillId) {
        this.uniqueSkillId = uniqueSkillId;
    }

    public void setSecondaryStatus(StatusEffectType secondaryStatus) {
        this.secondaryStatus = secondaryStatus;
    }

    public void setSecondaryStatusTurns(int secondaryStatusTurns) {
        this.secondaryStatusTurns = secondaryStatusTurns;
    }

    public void setSecondaryTargetType(TargetType secondaryTargetType) {
        this.secondaryTargetType = secondaryTargetType;
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
                ", element=" + element +
                ", speedCost=" + speedCost +
                ", weaponType=" + weaponType +
                ", description='" + description + '\'' +
                '}';
    }
}
