package com.rogueforge.game.data;

/**
 * All game settings with default values.
 * Stores audio, graphics, and UI preferences.
 */
public class SettingsConfig {

    private float musicVolume = 0.7f;
    private float sfxVolume = 0.8f;
    private boolean fullscreen = false;
    private int windowWidth = 1280;
    private int windowHeight = 720;
    private boolean showMinimap = true;
    private boolean showDamageNumbers = true;
    private String difficultyMode = "NORMAL";

    /**
     * No-arg constructor with default values.
     */
    public SettingsConfig() {
    }

    // Getters
    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public boolean isShowMinimap() {
        return showMinimap;
    }

    public boolean isShowDamageNumbers() {
        return showDamageNumbers;
    }

    public String getDifficultyMode() {
        return difficultyMode;
    }

    // Setters
    public void setMusicVolume(float musicVolume) {
        this.musicVolume = Math.max(0f, Math.min(1f, musicVolume));
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, sfxVolume));
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = Math.max(800, windowWidth);
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = Math.max(600, windowHeight);
    }

    public void setShowMinimap(boolean showMinimap) {
        this.showMinimap = showMinimap;
    }

    public void setShowDamageNumbers(boolean showDamageNumbers) {
        this.showDamageNumbers = showDamageNumbers;
    }

    public void setDifficultyMode(String difficultyMode) {
        if (difficultyMode == null || difficultyMode.isEmpty()) {
            this.difficultyMode = "NORMAL";
            return;
        }
        this.difficultyMode = difficultyMode.toUpperCase();
    }
}
