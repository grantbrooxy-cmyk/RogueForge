package com.rogueforge.game.world.actor;

import com.badlogic.gdx.math.Vector2;

/**
 * Shared spatial state for an overworld actor.
 */
public class TransformComponent {
    public Vector2 position = new Vector2();
    public Vector2 facing = new Vector2(0f, -1f);
    public float size;
    public float animationTime;
}
