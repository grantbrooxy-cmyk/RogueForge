package com.rogueforge.game.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

public class CameraController {
    private OrthographicCamera camera;
    private Vector2 targetPosition;
    private Vector2 currentPosition;
    private float mapWidth;
    private float mapHeight;
    private static final float LERP_SPEED = 5f;

    // Screen shake
    private float shakeIntensity;
    private float shakeDuration;
    private float shakeTimer;

    public CameraController(OrthographicCamera camera) {
        this.camera = camera;
        this.currentPosition = new Vector2(camera.position.x, camera.position.y);
        this.targetPosition = new Vector2(currentPosition);
        this.mapWidth = 100f;
        this.mapHeight = 100f;
        this.shakeIntensity = 0f;
        this.shakeDuration = 0f;
        this.shakeTimer = 0f;
    }

    public void setTarget(Vector2 target) {
        targetPosition.set(target);
    }

    public void setMapBounds(float width, float height) {
        this.mapWidth = width;
        this.mapHeight = height;
    }

    public void shake(float intensity, float duration) {
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
        this.shakeTimer = duration;
    }

    public void update(float delta) {
        // Lerp toward target
        float lerpFactor = LERP_SPEED * delta;
        currentPosition.lerp(targetPosition, lerpFactor);

        // Clamp to map bounds
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        currentPosition.x = Math.max(halfWidth, Math.min(mapWidth - halfWidth, currentPosition.x));
        currentPosition.y = Math.max(halfHeight, Math.min(mapHeight - halfHeight, currentPosition.y));

        // Apply screen shake
        float shakeX = 0f;
        float shakeY = 0f;

        if (shakeTimer > 0) {
            shakeTimer -= delta;
            float progress = 1f - (shakeTimer / shakeDuration);
            float currentIntensity = shakeIntensity * (1f - progress); // Fade out shake

            shakeX = (float)(Math.random() - 0.5f) * currentIntensity * 2f;
            shakeY = (float)(Math.random() - 0.5f) * currentIntensity * 2f;
        }

        camera.position.set(currentPosition.x + shakeX, currentPosition.y + shakeY, 0);
        camera.update();
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    public Vector2 getCurrentPosition() {
        return currentPosition;
    }

    public Vector2 getTargetPosition() {
        return targetPosition;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public boolean isShaking() {
        return shakeTimer > 0;
    }
}
