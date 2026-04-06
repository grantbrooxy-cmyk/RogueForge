package com.rogueforge.game.combat;

import java.util.List;

/**
 * Ordered ability-effect processing pipeline for combat damage resolution.
 */
public class AbilityEffectPipeline {
    private final List<AbilityEffectStep> steps;

    public AbilityEffectPipeline(List<AbilityEffectStep> steps) {
        this.steps = steps;
    }

    public AbilityEffectContext apply(AbilityEffectContext context) {
        if (context == null) {
            return null;
        }
        for (AbilityEffectStep step : steps) {
            step.apply(context);
        }
        return context;
    }
}
