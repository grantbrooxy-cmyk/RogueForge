package com.rogueforge.game.core;

/**
 * Simple priority bands for deferred event processing.
 */
public final class EventPriority {
    public static final int HIGH = 0;
    public static final int NORMAL = 100;
    public static final int LOW = 200;

    private EventPriority() {
    }
}
