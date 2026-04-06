package com.rogueforge.game.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent roguelite progression shared across runs.
 */
public class MetaProgressionState {
    private int deathCount;
    private int forgeShards;
    private int worldEventCompletions;
    private List<String> ownedEnhancementIds;
    private List<String> activeCurseIds;
    private List<String> purchasedLegacyNodeIds;
    private List<String> unlockedLegendaryRobotIds;
    private int collapseStreak;

    public MetaProgressionState() {
        this.ownedEnhancementIds = new ArrayList<>();
        this.activeCurseIds = new ArrayList<>();
        this.purchasedLegacyNodeIds = new ArrayList<>();
        this.unlockedLegendaryRobotIds = new ArrayList<>();
    }

    public int getDeathCount() {
        return Math.max(0, deathCount);
    }

    public void setDeathCount(int deathCount) {
        this.deathCount = Math.max(0, deathCount);
    }

    public int getForgeShards() {
        return Math.max(0, forgeShards);
    }

    public void setForgeShards(int forgeShards) {
        this.forgeShards = Math.max(0, forgeShards);
    }

    public int getWorldEventCompletions() {
        return Math.max(0, worldEventCompletions);
    }

    public void setWorldEventCompletions(int worldEventCompletions) {
        this.worldEventCompletions = Math.max(0, worldEventCompletions);
    }

    public List<String> getOwnedEnhancementIds() {
        if (ownedEnhancementIds == null) {
            ownedEnhancementIds = new ArrayList<>();
        }
        return ownedEnhancementIds;
    }

    public void setOwnedEnhancementIds(List<String> ownedEnhancementIds) {
        this.ownedEnhancementIds = ownedEnhancementIds != null ? new ArrayList<>(ownedEnhancementIds) : new ArrayList<>();
    }

    public List<String> getActiveCurseIds() {
        if (activeCurseIds == null) {
            activeCurseIds = new ArrayList<>();
        }
        return activeCurseIds;
    }

    public void setActiveCurseIds(List<String> activeCurseIds) {
        this.activeCurseIds = activeCurseIds != null ? new ArrayList<>(activeCurseIds) : new ArrayList<>();
    }

    public List<String> getPurchasedLegacyNodeIds() {
        if (purchasedLegacyNodeIds == null) {
            purchasedLegacyNodeIds = new ArrayList<>();
        }
        return purchasedLegacyNodeIds;
    }

    public void setPurchasedLegacyNodeIds(List<String> purchasedLegacyNodeIds) {
        this.purchasedLegacyNodeIds = purchasedLegacyNodeIds != null ? new ArrayList<>(purchasedLegacyNodeIds) : new ArrayList<>();
    }

    public List<String> getUnlockedLegendaryRobotIds() {
        if (unlockedLegendaryRobotIds == null) {
            unlockedLegendaryRobotIds = new ArrayList<>();
        }
        return unlockedLegendaryRobotIds;
    }

    public void setUnlockedLegendaryRobotIds(List<String> unlockedLegendaryRobotIds) {
        this.unlockedLegendaryRobotIds = unlockedLegendaryRobotIds != null ? new ArrayList<>(unlockedLegendaryRobotIds) : new ArrayList<>();
    }

    public int getCollapseStreak() {
        return Math.max(0, collapseStreak);
    }

    public void setCollapseStreak(int collapseStreak) {
        this.collapseStreak = Math.max(0, collapseStreak);
    }
}
