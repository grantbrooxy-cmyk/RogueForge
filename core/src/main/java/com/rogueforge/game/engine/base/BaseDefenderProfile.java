package com.rogueforge.game.engine.base;

/**
 * Lightweight combat/runtime profile for a reserve bot assigned to base defense.
 */
public class BaseDefenderProfile {
    private final String robotId;
    private final String displayName;
    private final float maxHealth;
    private final float attackPower;
    private final float defense;
    private final float moveSpeed;
    private final float attackRange;
    private final float detectionRange;
    private final float attackCooldown;

    public BaseDefenderProfile(
        String robotId,
        String displayName,
        float maxHealth,
        float attackPower,
        float defense,
        float moveSpeed,
        float attackRange,
        float detectionRange,
        float attackCooldown
    ) {
        this.robotId = robotId;
        this.displayName = displayName;
        this.maxHealth = Math.max(1f, maxHealth);
        this.attackPower = Math.max(1f, attackPower);
        this.defense = Math.max(0f, defense);
        this.moveSpeed = Math.max(40f, moveSpeed);
        this.attackRange = Math.max(32f, attackRange);
        this.detectionRange = Math.max(this.attackRange, detectionRange);
        this.attackCooldown = Math.max(0.15f, attackCooldown);
    }

    public String getRobotId() {
        return robotId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getAttackPower() {
        return attackPower;
    }

    public float getDefense() {
        return defense;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public float getAttackRange() {
        return attackRange;
    }

    public float getDetectionRange() {
        return detectionRange;
    }

    public float getAttackCooldown() {
        return attackCooldown;
    }
}
