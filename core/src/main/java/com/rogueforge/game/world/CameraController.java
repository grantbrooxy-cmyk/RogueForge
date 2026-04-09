package com.rogueforge.game.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class CameraController {
    private final OrthographicCamera camera;
    private final Vector2 targetPosition;
    private final Vector2 currentPosition;
    private final Rectangle mapBounds = new Rectangle();
    private final Vector2 floatingOrigin = new Vector2();
    private final Vector2 pendingWorldShift = new Vector2();
    private static final float LERP_SPEED = 5f;
    private boolean floatingOriginEnabled;
    private float floatingOriginThreshold = 4096f;
    private float floatingOriginSnap = 2048f;

    // Screen shake
    private float shakeIntensity;
    private float shakeDuration;
    private float shakeTimer;

    public CameraController(OrthographicCamera camera) {
        this.camera = camera;
        this.currentPosition = new Vector2(camera.position.x, camera.position.y);
        this.targetPosition = new Vector2(currentPosition);
        // Initialize to a large enough area to prevent immediate clamping before first load
        this.mapBounds.set(-100000f, -100000f, 200000f, 200000f);
        this.shakeIntensity = 0f;
        this.shakeDuration = 0f;
        this.shakeTimer = 0f;
    }

    public void setTarget(Vector2 target) {
        targetPosition.set(target);
    }

    public void snapToTarget() {
        currentPosition.set(targetPosition);
        camera.position.set(currentPosition.x, currentPosition.y, 0f);
        camera.update();
    }

    public void setMapBounds(float width, float height) {
        this.mapBounds.set(0f, 0f, width, height);
    }

    public void setMapBounds(float x, float y, float width, float height) {
        this.mapBounds.set(x, y, width, height);
    }

    public void enableFloatingOrigin(boolean enabled) {
        this.floatingOriginEnabled = enabled;
        if (!enabled) {
            resetFloatingOrigin();
        }
    }

    public void configureFloatingOrigin(float threshold, float snap) {
        this.floatingOriginThreshold = Math.max(512f, threshold);
        this.floatingOriginSnap = Math.max(256f, Math.min(this.floatingOriginThreshold, snap));
    }

    public void shake(float intensity, float duration) {
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
        this.shakeTimer = duration;
    }

    public Vector2 update(float delta) {
        pendingWorldShift.setZero();
        maybeShiftFloatingOrigin();

        // Lerp toward target
        float lerpFactor = LERP_SPEED * delta;
        currentPosition.lerp(targetPosition, lerpFactor);

        // Clamp to map bounds
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        if (mapBounds.width > 0f) {
            currentPosition.x = Math.max(mapBounds.x + halfWidth, Math.min(mapBounds.x + mapBounds.width - halfWidth, currentPosition.x));
        }
        if (mapBounds.height > 0f) {
            currentPosition.y = Math.max(mapBounds.y + halfHeight, Math.min(mapBounds.y + mapBounds.height - halfHeight, currentPosition.y));
        }

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
        return new Vector2(pendingWorldShift);
    }

    private void maybeShiftFloatingOrigin() {
        if (!floatingOriginEnabled) {
            return;
        }
        float shiftX = computeAxisShift(targetPosition.x);
        float shiftY = computeAxisShift(targetPosition.y);
        if (shiftX == 0f && shiftY == 0f) {
            return;
        }
        pendingWorldShift.set(shiftX, shiftY);
        floatingOrigin.add(shiftX, shiftY);
        targetPosition.sub(shiftX, shiftY);
        currentPosition.sub(shiftX, shiftY);
        mapBounds.x -= shiftX;
        mapBounds.y -= shiftY;
    }

    private float computeAxisShift(float coordinate) {
        if (Math.abs(coordinate) <= floatingOriginThreshold) {
            return 0f;
        }
        float direction = Math.signum(coordinate);
        float magnitude = Math.max(floatingOriginSnap,
            (float) Math.floor(Math.abs(coordinate) / floatingOriginSnap) * floatingOriginSnap);
        return direction * magnitude;
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

    public Vector2 getFloatingOrigin() {
        return new Vector2(floatingOrigin);
    }

    public void resetFloatingOrigin() {
        floatingOrigin.setZero();
        pendingWorldShift.setZero();
    }

    public void syncFloatingOrigin(Vector2 originOffset) {
        floatingOrigin.set(originOffset != null ? originOffset : Vector2.Zero);
        pendingWorldShift.setZero();
    }

    public boolean isShaking() {
        return shakeTimer > 0;
    }
}
