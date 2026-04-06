package com.rogueforge.game.combat;

/**
 * Tracks elemental streaks on the target and flags break triggers.
 */
public class ElementalBreakEffectStep implements AbilityEffectStep {
    @Override
    public void apply(AbilityEffectContext context) {
        Element element = context.getAbility() != null ? context.getAbility().getElement() : null;
        BattleCombatant target = context.getTarget();
        if (target == null || element == null || element == Element.NONE) {
            return;
        }
        int streak = target.registerElementalHit(element);
        context.setElementalBreak(streak == 3);
    }
}
