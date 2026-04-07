package com.rogueforge.game.core;

/**
 * Narrow event-bus view for screens and systems that only need event access.
 */
public interface EventContext {
    EventBus getEventBus();
}
