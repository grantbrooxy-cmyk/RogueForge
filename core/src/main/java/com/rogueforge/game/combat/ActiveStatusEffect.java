package com.rogueforge.game.combat;

/**
 * Runtime status effect entry.
 */
public class ActiveStatusEffect {
    private final StatusEffectType type;
    private int remainingTurns;
    private final float magnitude;

    public ActiveStatusEffect(StatusEffectType type, int remainingTurns, float magnitude) {
        this.type = type;
        this.remainingTurns = Math.max(1, remainingTurns);
        this.magnitude = magnitude;
    }

    public StatusEffectType getType() {
        return type;
    }

    public int getRemainingTurns() {
        return remainingTurns;
    }

    public float getMagnitude() {
        return magnitude;
    }

    public void refresh(int turns) {
        remainingTurns = Math.max(remainingTurns, turns);
    }

    public void setRemainingTurns(int remainingTurns) {
        this.remainingTurns = Math.max(0, remainingTurns);
    }

    public boolean tickDown() {
        remainingTurns--;
        return remainingTurns <= 0;
    }
}
