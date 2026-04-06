package com.rogueforge.game.combat;

/**
 * Applies the target's ability damage taken multiplier.
 */
public class AbilityDamageTakenEffectStep implements AbilityEffectStep {
    @Override
    public void apply(AbilityEffectContext context) {
        BattleCombatant target = context.getTarget();
        if (target == null) {
            return;
        }
        context.setMultiplier(context.getMultiplier()
            * target.getStatusEffectManager().getAbilityDamageTakenMultiplier());
    }
}
