package com.rogueforge.game.entity.component;

import com.badlogic.gdx.math.Vector2;

/**
 * Shared spatial state for gameplay entities.
 */
public class PositionComponent {
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    public Vector2 facing = new Vector2(0f, -1f);
    public float angleDeg;
    public float size;
}
