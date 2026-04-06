package com.rogueforge.game.combat;

/**
 * Resolves elemental affinity and absorb behavior for an ability hit.
 */
public class ElementalMultiplierEffectStep implements AbilityEffectStep {
    @Override
    public void apply(AbilityEffectContext context) {
        Element element = context.getAbility() != null ? context.getAbility().getElement() : null;
        BattleCombatant target = context.getTarget();
        if (target == null) {
            return;
        }
        float multiplier = ElementalSystem.getMultiplier(element, target);
        if (multiplier < 0f) {
            context.setAbsorbed(true);
            context.setMultiplier(1f);
            return;
        }
        context.setMultiplier(multiplier);
    }
}
