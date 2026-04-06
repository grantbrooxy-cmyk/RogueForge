package com.rogueforge.game.world.actor;

import com.badlogic.gdx.math.Vector2;

/**
 * Overworld enemy actor composed from lightweight grouped state.
 */
public class EnemyActor extends OverworldActor {
    public float hp;
    public float maxHp;
    public float speed;
    public float defense;
    public float agility;
    public float strength;
    public float intelligence;
    public float stamina;
    public int rewardGold;
    public int rewardExperience;
    public String name;
    public String monsterId;
    public boolean alive;
    public int spriteIndex;
    public float attackCooldown;
    public float attackTimer;
    public Vector2 patrolTarget = new Vector2();
    public int dungeonFloor;
    public boolean raidSpawned;

    private final VitalsComponent vitals = new VitalsComponent();
    private final CombatStatsComponent combatStats = new CombatStatsComponent();
    private final MotionComponent motion = new MotionComponent();

    public VitalsComponent vitals() {
        vitals.health = hp;
        vitals.maxHealth = maxHp;
        vitals.alive = alive;
        return vitals;
    }

    public CombatStatsComponent combatStats() {
        combatStats.defense = defense;
        combatStats.agility = agility;
        combatStats.strength = strength;
        combatStats.intelligence = intelligence;
        combatStats.stamina = stamina;
        return combatStats;
    }

    public MotionComponent motion() {
        motion.speed = speed;
        motion.attackCooldown = attackCooldown;
        motion.attackTimer = attackTimer;
        motion.patrolTarget = patrolTarget;
        return motion;
    }
}
