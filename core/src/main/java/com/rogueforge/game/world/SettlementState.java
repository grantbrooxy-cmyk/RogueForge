package com.rogueforge.game.world;

/**
 * Persistent settlement upgrade state.
 */
public class SettlementState {
    private String upgradeId;
    private int level;

    public SettlementState() {
    }

    public SettlementState(String upgradeId, int level) {
        this.upgradeId = upgradeId;
        this.level = level;
    }

    public String getUpgradeId() { return upgradeId; }
    public void setUpgradeId(String upgradeId) { this.upgradeId = upgradeId; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(0, level); }
}
