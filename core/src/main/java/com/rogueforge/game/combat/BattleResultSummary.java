package com.rogueforge.game.combat;

/**
 * Summary data displayed on the battle results screen.
 */
public class BattleResultSummary {
    private final int goldEarned;
    private final int experienceEarned;
    private final int levelUps;
    private final int bestiaryUpdates;
    private final String[] drops;
    private final String[] abilityProgress;
    private final String[] weaponProgress;
    private final String[] masteryUnlocks;
    private final String[] robotProgress;

    public BattleResultSummary(int goldEarned, int experienceEarned, int levelUps, int bestiaryUpdates,
                               String[] drops, String[] abilityProgress, String[] weaponProgress,
                               String[] masteryUnlocks, String[] robotProgress) {
        this.goldEarned = goldEarned;
        this.experienceEarned = experienceEarned;
        this.levelUps = levelUps;
        this.bestiaryUpdates = bestiaryUpdates;
        this.drops = drops != null ? drops : new String[0];
        this.abilityProgress = abilityProgress != null ? abilityProgress : new String[0];
        this.weaponProgress = weaponProgress != null ? weaponProgress : new String[0];
        this.masteryUnlocks = masteryUnlocks != null ? masteryUnlocks : new String[0];
        this.robotProgress = robotProgress != null ? robotProgress : new String[0];
    }

    public int getGoldEarned() {
        return goldEarned;
    }

    public int getExperienceEarned() {
        return experienceEarned;
    }

    public int getLevelUps() {
        return levelUps;
    }

    public int getBestiaryUpdates() {
        return bestiaryUpdates;
    }

    public String[] getDrops() {
        return drops;
    }

    public String[] getAbilityProgress() {
        return abilityProgress;
    }

    public String[] getWeaponProgress() {
        return weaponProgress;
    }

    public String[] getMasteryUnlocks() {
        return masteryUnlocks;
    }

    public String[] getRobotProgress() {
        return robotProgress;
    }
}
