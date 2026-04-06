package com.rogueforge.game.combat;

/**
 * Mutable context passed through the ability effect pipeline.
 */
public class AbilityEffectContext {
    private final BattleCombatant caster;
    private final BattleCombatant target;
    private final AbilityDefinition ability;
    private final float proficiencyMultiplier;
    private float baseDamage;
    private float multiplier = 1f;
    private boolean absorbed;
    private boolean elementalBreak;

    public AbilityEffectContext(BattleCombatant caster, BattleCombatant target, AbilityDefinition ability,
                                float proficiencyMultiplier, float baseDamage) {
        this.caster = caster;
        this.target = target;
        this.ability = ability;
        this.proficiencyMultiplier = proficiencyMultiplier;
        this.baseDamage = baseDamage;
    }

    public BattleCombatant getCaster() {
        return caster;
    }

    public BattleCombatant getTarget() {
        return target;
    }

    public AbilityDefinition getAbility() {
        return ability;
    }

    public float getProficiencyMultiplier() {
        return proficiencyMultiplier;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(float baseDamage) {
        this.baseDamage = baseDamage;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }

    public boolean isAbsorbed() {
        return absorbed;
    }

    public void setAbsorbed(boolean absorbed) {
        this.absorbed = absorbed;
    }

    public boolean isElementalBreak() {
        return elementalBreak;
    }

    public void setElementalBreak(boolean elementalBreak) {
        this.elementalBreak = elementalBreak;
    }
}
