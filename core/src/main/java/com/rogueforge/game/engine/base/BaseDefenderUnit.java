package com.rogueforge.game.engine.base;

import com.badlogic.gdx.math.Vector2;

/**
 * Runtime state for one live reserve bot defending a player base in the overworld.
 */
public class BaseDefenderUnit {
    private final String assignmentKey;
    private final String robotId;
    private final String displayName;
    private final String structureInstanceId;
    private final DefenderRole role;
    private final Vector2 position;
    private final Vector2 guardPosition;
    private final Vector2 patrolPosition;
    private float currentHealth;
    private float maxHealth;
    private float attackPower;
    private float defense;
    private float moveSpeed;
    private float attackRange;
    private float detectionRange;
    private float attackCooldown;
    private float attackTimer;
    private float animationTime;
    private boolean active;

    public BaseDefenderUnit(
        String assignmentKey,
        String robotId,
        String displayName,
        String structureInstanceId,
        DefenderRole role,
        Vector2 position,
        Vector2 guardPosition,
        Vector2 patrolPosition,
        float maxHealth,
        float currentHealth,
        float attackPower,
        float defense,
        float moveSpeed,
        float attackRange,
        float detectionRange,
        float attackCooldown,
        float attackTimer,
        float animationTime,
        boolean active
    ) {
        this.assignmentKey = assignmentKey;
        this.robotId = robotId;
        this.displayName = displayName;
        this.structureInstanceId = structureInstanceId;
        this.role = role != null ? role : DefenderRole.GUARD;
        this.position = position != null ? new Vector2(position) : new Vector2();
        this.guardPosition = guardPosition != null ? new Vector2(guardPosition) : new Vector2();
        this.patrolPosition = patrolPosition != null ? new Vector2(patrolPosition) : new Vector2(this.guardPosition);
        this.maxHealth = Math.max(1f, maxHealth);
        this.currentHealth = Math.max(0f, Math.min(this.maxHealth, currentHealth));
        this.attackPower = Math.max(1f, attackPower);
        this.defense = Math.max(0f, defense);
        this.moveSpeed = Math.max(40f, moveSpeed);
        this.attackRange = Math.max(32f, attackRange);
        this.detectionRange = Math.max(this.attackRange, detectionRange);
        this.attackCooldown = Math.max(0.15f, attackCooldown);
        this.attackTimer = Math.max(0f, attackTimer);
        this.animationTime = Math.max(0f, animationTime);
        this.active = active;
    }

    public String getAssignmentKey() {
        return assignmentKey;
    }

    public String getRobotId() {
        return robotId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStructureInstanceId() {
        return structureInstanceId;
    }

    public DefenderRole getRole() {
        return role;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getGuardPosition() {
        return guardPosition;
    }

    public Vector2 getPatrolPosition() {
        return patrolPosition;
    }

    public float getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(float currentHealth) {
        this.currentHealth = Math.max(0f, Math.min(maxHealth, currentHealth));
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

    public float getAttackTimer() {
        return attackTimer;
    }

    public void setAttackTimer(float attackTimer) {
        this.attackTimer = Math.max(0f, attackTimer);
    }

    public float getAnimationTime() {
        return animationTime;
    }

    public void setAnimationTime(float animationTime) {
        this.animationTime = Math.max(0f, animationTime);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
