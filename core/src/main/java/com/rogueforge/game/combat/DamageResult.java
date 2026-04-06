package com.rogueforge.game.combat;

/**
 * Result of resolving a combat hit.
 */
public final class DamageResult {
    private final int damage;
    private final boolean elementalBreak;

    public DamageResult(int damage, boolean elementalBreak) {
        this.damage = damage;
        this.elementalBreak = elementalBreak;
    }

    public int damage() {
        return damage;
    }

    public boolean elementalBreak() {
        return elementalBreak;
    }
}
