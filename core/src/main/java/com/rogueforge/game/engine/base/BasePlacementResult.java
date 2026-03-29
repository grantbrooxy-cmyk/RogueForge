package com.rogueforge.game.engine.base;

import com.badlogic.gdx.math.Rectangle;

/**
 * Result object for validating or placing a structure.
 */
public class BasePlacementResult {
    private final boolean allowed;
    private final PlacementFailureReason failureReason;
    private final String message;
    private final Rectangle bounds;

    private BasePlacementResult(boolean allowed, PlacementFailureReason failureReason, String message, Rectangle bounds) {
        this.allowed = allowed;
        this.failureReason = failureReason;
        this.message = message;
        this.bounds = bounds != null ? new Rectangle(bounds) : null;
    }

    public static BasePlacementResult allowed(Rectangle bounds) {
        return new BasePlacementResult(true, null, null, bounds);
    }

    public static BasePlacementResult denied(PlacementFailureReason failureReason, String message, Rectangle bounds) {
        return new BasePlacementResult(false, failureReason, message, bounds);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public PlacementFailureReason getFailureReason() {
        return failureReason;
    }

    public String getMessage() {
        return message;
    }

    public Rectangle getBounds() {
        return bounds != null ? new Rectangle(bounds) : null;
    }
}
