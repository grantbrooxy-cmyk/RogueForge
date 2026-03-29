package com.rogueforge.game.engine.base;

import com.badlogic.gdx.math.Rectangle;

/**
 * Persistent structure instance placed by the player in the frontier.
 */
public class PlacedStructure {
    private final String instanceId;
    private final String structureDefinitionId;
    private final String zoneId;
    private final String claimedSiteId;
    private final Rectangle bounds;
    private int currentHitPoints;
    private boolean active = true;

    public PlacedStructure(
        String instanceId,
        String structureDefinitionId,
        String zoneId,
        String claimedSiteId,
        Rectangle bounds,
        int currentHitPoints
    ) {
        this.instanceId = instanceId;
        this.structureDefinitionId = structureDefinitionId;
        this.zoneId = zoneId;
        this.claimedSiteId = claimedSiteId;
        this.bounds = bounds != null ? new Rectangle(bounds) : new Rectangle();
        this.currentHitPoints = Math.max(0, currentHitPoints);
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getStructureDefinitionId() {
        return structureDefinitionId;
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getClaimedSiteId() {
        return claimedSiteId;
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    public int getCurrentHitPoints() {
        return currentHitPoints;
    }

    public void setCurrentHitPoints(int currentHitPoints) {
        this.currentHitPoints = Math.max(0, currentHitPoints);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
