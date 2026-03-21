package com.rogueforge.game.progression;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent proficiency state for a single weapon family.
 */
public class WeaponProficiencyState {
    private String weaponType;
    private int level = 1;
    private int xp = 0;
    private List<String> unlockedMilestones = new ArrayList<>();

    public WeaponProficiencyState() {
    }

    public WeaponProficiencyState(String weaponType) {
        this.weaponType = weaponType;
    }

    public String getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(String weaponType) {
        this.weaponType = weaponType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
    }

    public int addXp(int amount) {
        int levelsGained = 0;
        xp += Math.max(0, amount);
        while (level < 10 && xp >= xpForNextLevel()) {
            xp -= xpForNextLevel();
            level++;
            levelsGained++;
        }
        return levelsGained;
    }

    private int xpForNextLevel() {
        return 25 + (level * 18);
    }

    public List<String> getUnlockedMilestones() {
        return unlockedMilestones;
    }

    public void setUnlockedMilestones(List<String> unlockedMilestones) {
        this.unlockedMilestones = unlockedMilestones != null ? unlockedMilestones : new ArrayList<>();
    }

    public boolean unlockMilestone(String milestone) {
        if (milestone == null || milestone.isEmpty()) {
            return false;
        }
        if (!unlockedMilestones.contains(milestone)) {
            unlockedMilestones.add(milestone);
            return true;
        }
        return false;
    }
}
