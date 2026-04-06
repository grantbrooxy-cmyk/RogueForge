package com.rogueforge.game.world.actor;

import com.badlogic.gdx.math.Vector2;

/**
 * Base overworld actor with grouped component views.
 *
 * <p>The public fields keep the current game code simple while the component
 * accessors provide a migration path toward a more systematic actor model.</p>
 */
public abstract class OverworldActor {
    public Vector2 pos = new Vector2();
    public Vector2 facing = new Vector2(0f, -1f);
    public float size;
    public float animationTime;

    private final TransformComponent transform = new TransformComponent();

    public TransformComponent transform() {
        transform.position = pos;
        transform.facing = facing;
        transform.size = size;
        transform.animationTime = animationTime;
        return transform;
    }
}
