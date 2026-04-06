package com.rogueforge.game.engine.meta;

/**
 * Aggregated permanent endgame bonuses unlocked from Forge Legacy.
 */
public class ForgeLegacyBonuses {
    private int startingGoldBonus;
    private int startingPotionBonus;
    private float maxHealthMultiplier = 1f;
    private float attackMultiplier = 1f;
    private float robotSpeedBonus;

    public int getStartingGoldBonus() {
        return Math.max(0, startingGoldBonus);
    }

    public void addStartingGoldBonus(int amount) {
        this.startingGoldBonus += Math.max(0, amount);
    }

    public int getStartingPotionBonus() {
        return Math.max(0, startingPotionBonus);
    }

    public void addStartingPotionBonus(int amount) {
        this.startingPotionBonus += Math.max(0, amount);
    }

    public float getMaxHealthMultiplier() {
        return Math.max(1f, maxHealthMultiplier);
    }

    public void multiplyMaxHealth(float multiplier) {
        if (multiplier > 0f) {
            this.maxHealthMultiplier *= multiplier;
        }
    }

    public float getAttackMultiplier() {
        return Math.max(1f, attackMultiplier);
    }

    public void multiplyAttack(float multiplier) {
        if (multiplier > 0f) {
            this.attackMultiplier *= multiplier;
        }
    }

    public float getRobotSpeedBonus() {
        return robotSpeedBonus;
    }

    public void addRobotSpeedBonus(float amount) {
        this.robotSpeedBonus += amount;
    }
}
