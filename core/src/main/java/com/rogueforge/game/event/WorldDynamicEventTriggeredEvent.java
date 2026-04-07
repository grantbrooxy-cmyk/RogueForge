package com.rogueforge.game.event;

/**
 * Event fired when a boss clear or world reaction spawns a follow-up world beat.
 */
public class WorldDynamicEventTriggeredEvent {
    private final String eventType;
    private final String zoneId;
    private final String title;
    private final String description;

    public WorldDynamicEventTriggeredEvent(String eventType, String zoneId, String title, String description) {
        this.eventType = eventType;
        this.zoneId = zoneId;
        this.title = title;
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
