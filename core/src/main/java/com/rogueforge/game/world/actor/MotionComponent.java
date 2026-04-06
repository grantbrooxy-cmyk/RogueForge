package com.rogueforge.game.world.actor;

import com.badlogic.gdx.math.Vector2;

/**
 * Movement and cooldown state for overworld actors.
 */
public class MotionComponent {
    public float speed;
    public float attackCooldown;
    public float attackTimer;
    public float angleDeg;
    public Vector2 patrolTarget = new Vector2();
}
