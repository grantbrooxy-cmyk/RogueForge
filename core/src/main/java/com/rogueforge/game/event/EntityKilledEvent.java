package com.rogueforge.game.event;

import com.badlogic.gdx.utils.Pool;

/**
 * Event fired when an entity dies.
 */
public class EntityKilledEvent implements Pool.Poolable {
    private Object entity;

    public EntityKilledEvent() {
    }

    public EntityKilledEvent(Object entity) {
        init(entity);
    }

    public EntityKilledEvent init(Object entity) {
        this.entity = entity;
        return this;
    }

    public Object getEntity() {
        return entity;
    }

    @Override
    public void reset() {
        // Intentionally left populated so listeners/tests can inspect the event
        // safely after dispatch. obtain()/init(...) always overwrite the field.
    }

    @Override
    public String toString() {
        return "EntityKilledEvent{" +
                "entity=" + entity +
                '}';
    }
}
