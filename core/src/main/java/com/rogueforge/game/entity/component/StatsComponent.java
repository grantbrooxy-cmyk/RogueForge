package com.rogueforge.game.entity.component;

/**
 * Common health and stat payload used across players, monsters, and robots.
 */
public class StatsComponent implements Component {
    public float currentHealth;
    public float maxHealth;
    public float speed;
    public float attack;
    public float defense;
    public float agility;
    public float strength;
    public float intelligence;
    public float stamina;
}
