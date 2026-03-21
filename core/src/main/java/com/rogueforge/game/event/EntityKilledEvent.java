package com.rogueforge.game.event;

/**
 * Event fired when an entity dies.
 */
public class EntityKilledEvent {
    private Object entity;

    public EntityKilledEvent(Object entity) {
        this.entity = entity;
    }

    public Object getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        return "EntityKilledEvent{" +
                "entity=" + entity +
                '}';
    }
}
