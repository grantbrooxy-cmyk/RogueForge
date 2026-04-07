package com.rogueforge.game.world.actor;

import com.badlogic.gdx.math.Vector2;

/**
 * Movement and cooldown state for overworld actors.
 */
public class MotionComponent implements com.rogueforge.game.entity.component.Component {
    public float speed;
    public float attackCooldown;
    public float attackTimer;
    public float angleDeg;
    public Vector2 patrolTarget = new Vector2();
}
