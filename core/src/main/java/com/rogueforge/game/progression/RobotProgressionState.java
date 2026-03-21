package com.rogueforge.game.progression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistent progression state for a robot companion.
 */
public class RobotProgressionState {
    private String robotId;
    private String displayName;
    private int level = 1;
    private int experience = 0;
    private int evolutionTier = 1;
    private List<String> knownAbilityIds = new ArrayList<>();
    private Map<String, AbilityProgressionState> abilityProgression = new HashMap<>();
    private Map<String, WeaponProficiencyState> weaponProficiencies = new HashMap<>();

    public RobotProgressionState() {
    }

    public RobotProgressionState(String robotId, String displayName) {
        this.robotId = robotId;
        this.displayName = displayName;
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = Math.max(0, experience);
    }

    public int getEvolutionTier() {
        return evolutionTier;
    }

    public void setEvolutionTier(int evolutionTier) {
        this.evolutionTier = Math.max(1, evolutionTier);
    }

    public List<String> getKnownAbilityIds() {
        return knownAbilityIds;
    }

    public void setKnownAbilityIds(List<String> knownAbilityIds) {
        this.knownAbilityIds = knownAbilityIds != null ? knownAbilityIds : new ArrayList<>();
    }

    public Map<String, AbilityProgressionState> getAbilityProgression() {
        return abilityProgression;
    }

    public void setAbilityProgression(Map<String, AbilityProgressionState> abilityProgression) {
        this.abilityProgression = abilityProgression != null ? abilityProgression : new HashMap<>();
    }

    public Map<String, WeaponProficiencyState> getWeaponProficiencies() {
        return weaponProficiencies;
    }

    public void setWeaponProficiencies(Map<String, WeaponProficiencyState> weaponProficiencies) {
        this.weaponProficiencies = weaponProficiencies != null ? weaponProficiencies : new HashMap<>();
    }

    public AbilityProgressionState getOrCreateAbilityProgression(String abilityId) {
        AbilityProgressionState state = abilityProgression.get(abilityId);
        if (state == null) {
            state = new AbilityProgressionState(abilityId);
            abilityProgression.put(abilityId, state);
        }
        return state;
    }
}
