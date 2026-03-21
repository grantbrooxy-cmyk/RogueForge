package com.rogueforge.game.progression;

/**
 * Persistent proficiency state for a single ability on a single robot.
 */
public class AbilityProgressionState {
    private String abilityId;
    private int proficiencyLevel = 1;
    private int proficiencyXp = 0;

    public AbilityProgressionState() {
    }

    public AbilityProgressionState(String abilityId) {
        this.abilityId = abilityId;
    }

    public String getAbilityId() {
        return abilityId;
    }

    public void setAbilityId(String abilityId) {
        this.abilityId = abilityId;
    }

    public int getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(int proficiencyLevel) {
        this.proficiencyLevel = Math.max(1, Math.min(10, proficiencyLevel));
    }

    public int getProficiencyXp() {
        return proficiencyXp;
    }

    public void setProficiencyXp(int proficiencyXp) {
        this.proficiencyXp = Math.max(0, proficiencyXp);
    }

    public int addXp(int amount) {
        int levelsGained = 0;
        proficiencyXp += Math.max(0, amount);
        while (proficiencyLevel < 10 && proficiencyXp >= xpForNextLevel()) {
            proficiencyXp -= xpForNextLevel();
            proficiencyLevel++;
            levelsGained++;
        }
        return levelsGained;
    }

    private int xpForNextLevel() {
        return 20 + (proficiencyLevel * 15);
    }
}
