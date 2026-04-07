package com.rogueforge.game.engine;

/**
 * Optional lifecycle hooks for shared engine services.
 */
public interface ServiceLifecycle {
    default void initialize() {
    }

    default void dispose() {
    }
}
