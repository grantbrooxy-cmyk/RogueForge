package com.rogueforge.game.persistence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.data.SettingsConfig;

/**
 * Manages game settings persistence and application.
 * Loads, saves, and applies user preferences.
 */
public class SettingsManager {
    private SettingsConfig currentConfig;
    private Json json;

    public SettingsManager() {
        this.json = new Json();
        this.currentConfig = new SettingsConfig();
    }

    /**
     * Loads settings from file, or creates defaults if file doesn't exist.
     */
    public void load() {
        FileHandle settingsFile = Gdx.files.local(PersistencePaths.SETTINGS);

        if (settingsFile.exists()) {
            try {
                String jsonData = settingsFile.readString();
                currentConfig = json.fromJson(SettingsConfig.class, jsonData);
            } catch (Exception e) {
                Gdx.app.error("SettingsManager", "Failed to load settings, using defaults", e);
                currentConfig = new SettingsConfig();
            }
        } else {
            currentConfig = new SettingsConfig();
        }

        applySettings();
    }

    /**
     * Saves current settings to file.
     */
    public void save() {
        FileHandle settingsFile = Gdx.files.local(PersistencePaths.SETTINGS);
        String jsonData = json.prettyPrint(currentConfig);
        settingsFile.writeString(jsonData, false);
    }

    /**
     * Returns the current settings configuration.
     *
     * @return the SettingsConfig instance
     */
    public SettingsConfig getSettings() {
        return currentConfig;
    }

    /**
     * Applies the current settings to the game.
     * Updates graphics, audio, and UI based on current config.
     */
    public void applySettings() {
        // Apply audio settings
        applyAudioSettings();

        // Apply graphics settings
        applyGraphicsSettings();
    }

    /**
     * Applies audio volume settings.
     * Music and SFX volume should be applied through your audio system.
     */
    private void applyAudioSettings() {
        // Example: Update your AudioManager with current volumes
        // AudioManager.setMusicVolume(currentConfig.getMusicVolume());
        // AudioManager.setSfxVolume(currentConfig.getSfxVolume());
    }

    /**
     * Applies graphics settings (fullscreen, resolution).
     */
    private void applyGraphicsSettings() {
        if (currentConfig.isFullscreen()) {
            // Apply fullscreen mode if not already fullscreen
            if (!Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        } else {
            // Apply windowed mode with specified dimensions
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(currentConfig.getWindowWidth(),
                        currentConfig.getWindowHeight());
            } else {
                // Only resize if dimensions changed
                if (Gdx.graphics.getWidth() != currentConfig.getWindowWidth() ||
                    Gdx.graphics.getHeight() != currentConfig.getWindowHeight()) {
                    Gdx.graphics.setWindowedMode(currentConfig.getWindowWidth(),
                            currentConfig.getWindowHeight());
                }
            }
        }
    }

    /**
     * Resets settings to defaults without saving.
     */
    public void resetToDefaults() {
        currentConfig = new SettingsConfig();
    }

    /**
     * Resets settings to defaults and saves.
     */
    public void resetAndSave() {
        resetToDefaults();
        save();
        applySettings();
    }
}
