package com.rogueforge.game.engine.base;

/**
 * Normalized reason for a base-placement failure.
 */
public enum PlacementFailureReason {
    INVALID_DEFINITION,
    INVALID_ZONE,
    MISSING_CLAIM,
    UNKNOWN_CLAIM_SITE,
    OUT_OF_BOUNDS,
    BLOCKED_BY_WORLD,
    OVERLAPS_STRUCTURE,
    INVALID_TERRAIN,
    TOO_CLOSE_TO_STARTER_SAFE_ZONE
}
