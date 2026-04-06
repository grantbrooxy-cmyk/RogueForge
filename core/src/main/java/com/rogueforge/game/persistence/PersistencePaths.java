package com.rogueforge.game.persistence;

/**
 * Canonical local-storage paths for mutable player and settings data.
 */
public final class PersistencePaths {
    public static final String SAVE_DIR = "saves";
    public static final String META_PROGRESSION = SAVE_DIR + "/meta_progression.json";
    public static final String SETTINGS = "settings.json";

    private PersistencePaths() {
    }
}
