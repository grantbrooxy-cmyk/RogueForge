package com.rogueforge.game.robot;

import com.badlogic.gdx.math.Vector2;

/**
 * Computes world positions for up to 8 robot slots in various formations relative to player position.
 */
public class FormationController {
    /**
     * Formation type enum defining spatial arrangement patterns.
     */
    public enum FormationType {
        CIRCLE,
        V_SHAPE,
        LINE,
        CLUSTER
    }

    private FormationType currentFormation;
    private static final float CIRCLE_RADIUS = 80f;
    private static final float V_SHAPE_DISTANCE = 100f;
    private static final float LINE_SPACING = 60f;
    private static final float CLUSTER_OFFSET = 30f;

    /**
     * Constructor with default formation (CIRCLE).
     */
    public FormationController() {
        this.currentFormation = FormationType.CIRCLE;
    }

    /**
     * Sets the current formation type.
     */
    public void setFormation(FormationType type) {
        if (type != null) {
            this.currentFormation = type;
        }
    }

    /**
     * Returns current formation type.
     */
    public FormationType getCurrentFormation() {
        return currentFormation;
    }

    /**
     * Computes slot positions based on current formation type.
     * Returns an array of Vector2 positions for up to robotCount robots.
     *
     * @param playerPos Center reference position
     * @param robotCount Number of robots to position (clamped to 0-8)
     * @return Array of slot positions
     */
    public Vector2[] computeSlots(Vector2 playerPos, int robotCount) {
        robotCount = Math.max(0, Math.min(robotCount, 8));
        if (robotCount == 0) {
            return new Vector2[0];
        }

        switch (currentFormation) {
            case CIRCLE:  return computeCircleFormation(playerPos, robotCount);
            case V_SHAPE: return computeVShapeFormation(playerPos, robotCount);
            case LINE:    return computeLineFormation(playerPos, robotCount);
            case CLUSTER: return computeClusterFormation(playerPos, robotCount);
            default:      return computeCircleFormation(playerPos, robotCount);
        }
    }

    /**
     * CIRCLE: Evenly spaced robots on a circle of radius 80f around player.
     */
    private Vector2[] computeCircleFormation(Vector2 playerPos, int robotCount) {
        Vector2[] slots = new Vector2[robotCount];
        float angleStep = 360f / robotCount;

        for (int i = 0; i < robotCount; i++) {
            float angle = i * angleStep;
            float radians = (float) Math.toRadians(angle);
            float x = playerPos.x + CIRCLE_RADIUS * (float) Math.cos(radians);
            float y = playerPos.y + CIRCLE_RADIUS * (float) Math.sin(radians);
            slots[i] = new Vector2(x, y);
        }

        return slots;
    }

    /**
     * V_SHAPE: Robots in V formation behind player.
     * Two columns, staggered back from player position.
     */
    private Vector2[] computeVShapeFormation(Vector2 playerPos, int robotCount) {
        Vector2[] slots = new Vector2[robotCount];
        int leftCount = (robotCount + 1) / 2;
        int rightCount = robotCount / 2;

        // Left side of V
        for (int i = 0; i < leftCount; i++) {
            float x = playerPos.x - (i + 1) * 40f;
            float y = playerPos.y - (i + 1) * 50f;
            slots[i] = new Vector2(x, y);
        }

        // Right side of V
        for (int i = 0; i < rightCount; i++) {
            float x = playerPos.x + (i + 1) * 40f;
            float y = playerPos.y - (i + 1) * 50f;
            slots[leftCount + i] = new Vector2(x, y);
        }

        return slots;
    }

    /**
     * LINE: Horizontal line of robots behind player.
     * Centered on player x-axis, positioned back on y-axis.
     */
    private Vector2[] computeLineFormation(Vector2 playerPos, int robotCount) {
        Vector2[] slots = new Vector2[robotCount];
        float totalWidth = (robotCount - 1) * LINE_SPACING;
        float startX = playerPos.x - totalWidth / 2f;
        float lineY = playerPos.y - 80f;

        for (int i = 0; i < robotCount; i++) {
            float x = startX + i * LINE_SPACING;
            slots[i] = new Vector2(x, lineY);
        }

        return slots;
    }

    /**
     * CLUSTER: Tight group of robots behind player.
     * Pseudo-random but deterministic offset from center point.
     */
    private Vector2[] computeClusterFormation(Vector2 playerPos, int robotCount) {
        Vector2[] slots = new Vector2[robotCount];
        float centerX = playerPos.x;
        float centerY = playerPos.y - 60f;

        for (int i = 0; i < robotCount; i++) {
            // Pseudo-random but deterministic positioning
            float offsetX = (i % 3 - 1) * CLUSTER_OFFSET;
            float offsetY = (i / 3) * CLUSTER_OFFSET;
            float x = centerX + offsetX;
            float y = centerY - offsetY;
            slots[i] = new Vector2(x, y);
        }

        return slots;
    }

    /**
     * Utility method to get the default offset distance for a formation type.
     */
    public static float getFormationDistance(FormationType type) {
        switch (type) {
            case CIRCLE:  return CIRCLE_RADIUS;
            case V_SHAPE: return V_SHAPE_DISTANCE;
            case LINE:    return 80f;
            case CLUSTER: return CLUSTER_OFFSET * 2;
            default:      return 80f;
        }
    }

    @Override
    public String toString() {
        return String.format("FormationController{currentFormation=%s}", currentFormation);
    }
}
