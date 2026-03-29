package com.rogueforge.game.engine.base;

/**
 * Persistent raid-pressure state for one frontier base zone.
 */
public class BaseRaidState {
    private boolean active;
    private float threatLevel;
    private float cooldownSeconds;
    private int waveIndex;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public float getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(float threatLevel) {
        this.threatLevel = Math.max(0f, threatLevel);
    }

    public float getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(float cooldownSeconds) {
        this.cooldownSeconds = Math.max(0f, cooldownSeconds);
    }

    public int getWaveIndex() {
        return waveIndex;
    }

    public void setWaveIndex(int waveIndex) {
        this.waveIndex = Math.max(0, waveIndex);
    }
}
