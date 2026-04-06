package com.rogueforge.game.world.actor;

/**
 * Overworld robot actor composed from lightweight grouped state.
 */
public class RobotActor extends OverworldActor {
    public String grade;
    public float health;
    public float maxHealth;
    public float attackTimer;
    public float angleDeg;
    public float agility;
    public float strength;
    public float intelligence;
    public float stamina;

    private final VitalsComponent vitals = new VitalsComponent();
    private final CombatStatsComponent combatStats = new CombatStatsComponent();
    private final MotionComponent motion = new MotionComponent();

    public RobotActor() {
        registerComponent(VitalsComponent.class, vitals);
        registerComponent(CombatStatsComponent.class, combatStats);
        registerComponent(MotionComponent.class, motion);
    }

    public VitalsComponent vitals() {
        vitals.health = health;
        vitals.maxHealth = maxHealth;
        vitals.alive = health > 0f;
        return vitals;
    }

    public CombatStatsComponent combatStats() {
        combatStats.agility = agility;
        combatStats.strength = strength;
        combatStats.intelligence = intelligence;
        combatStats.stamina = stamina;
        return combatStats;
    }

    public MotionComponent motion() {
        position();
        motion.attackTimer = attackTimer;
        motion.angleDeg = angleDeg;
        return motion;
    }
}
