package com.rogueforge.game.event;

import com.rogueforge.game.data.ZoneDefinition;

/**
 * Event fired when the current zone changes.
 */
public class ZoneChangedEvent {
    private String previousZoneId;
    private String newZoneId;

    public ZoneChangedEvent(ZoneDefinition newZone) {
        this(null, newZone != null ? newZone.getId() : null);
    }

    public ZoneChangedEvent(String previousZoneId, String newZoneId) {
        this.previousZoneId = previousZoneId;
        this.newZoneId = newZoneId;
    }

    public String getPreviousZoneId() {
        return previousZoneId;
    }

    public String getNewZoneId() {
        return newZoneId;
    }

    @Override
    public String toString() {
        return "ZoneChangedEvent{" +
                "previousZoneId='" + previousZoneId + '\'' +
                ", newZoneId='" + newZoneId + '\'' +
                '}';
    }
}
