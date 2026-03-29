package com.rogueforge.game.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent roguelite progression shared across runs.
 */
public class MetaProgressionState {
    private int deathCount;
    private List<String> ownedEnhancementIds;
    private List<String> activeCurseIds;
    private int collapseStreak;

    public MetaProgressionState() {
        this.ownedEnhancementIds = new ArrayList<>();
        this.activeCurseIds = new ArrayList<>();
    }

    public int getDeathCount() {
        return Math.max(0, deathCount);
    }

    public void setDeathCount(int deathCount) {
        this.deathCount = Math.max(0, deathCount);
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

    public int getCollapseStreak() {
        return Math.max(0, collapseStreak);
    }

    public void setCollapseStreak(int collapseStreak) {
        this.collapseStreak = Math.max(0, collapseStreak);
    }
}
