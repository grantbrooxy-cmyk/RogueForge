package com.rogueforge.game.combat;

/**
 * One step in the ability effect pipeline.
 */
public interface AbilityEffectStep {
    void apply(AbilityEffectContext context);
}
