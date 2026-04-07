package com.rogueforge.game.screen;

/**
 * High-level input modes used to keep exploration, settlement, and combat
 * bindings explicit instead of inferred from scattered booleans.
 */
public enum InputContext {
    EXPLORATION,
    DIALOG,
    SETTLEMENT,
    BUILD,
    COMBAT_COMMAND,
    COMBAT_TARGETING,
    COMBAT_RESULTS
}
