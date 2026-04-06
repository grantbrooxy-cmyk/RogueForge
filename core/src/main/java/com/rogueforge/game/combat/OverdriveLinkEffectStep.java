package com.rogueforge.game.combat;

/**
 * Applies the OVERDRIVE_LINK unique boost to ability damage.
 */
public class OverdriveLinkEffectStep implements AbilityEffectStep {
    @Override
    public void apply(AbilityEffectContext context) {
        BattleCombatant caster = context.getCaster();
        if (caster != null
            && caster.hasUniqueBoost("OVERDRIVE_LINK")
            && caster.getHealth() >= (caster.getMaxHealth() * 0.7f)) {
            context.setBaseDamage(context.getBaseDamage() * 1.15f);
        }
    }
}
