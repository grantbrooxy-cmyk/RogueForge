package com.rogueforge.game.entity.component;

import com.badlogic.gdx.math.Vector2;

/**
 * Shared transform state with floating-origin aware helpers.
 */
public class TransformComponent implements Component {
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    public Vector2 facing = new Vector2(0f, -1f);
    public Vector2 floatingOriginOffset = new Vector2();
    public float angleDeg;
    public float size;
    public float animationTime;

    public Vector2 getWorldPosition() {
        return new Vector2(position).add(floatingOriginOffset);
    }

    public void setWorldPosition(float worldX, float worldY) {
        position.set(worldX - floatingOriginOffset.x, worldY - floatingOriginOffset.y);
    }

    public void setFloatingOriginOffset(float x, float y) {
        floatingOriginOffset.set(x, y);
    }

    public void applyFloatingOriginShift(float shiftX, float shiftY) {
        floatingOriginOffset.add(shiftX, shiftY);
        position.sub(shiftX, shiftY);
    }
}
