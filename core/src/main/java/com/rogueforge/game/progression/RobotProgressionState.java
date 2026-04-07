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
    private float currentHealth = -1f;
    private List<String> knownAbilityIds = new ArrayList<>();
    private Map<String, AbilityProgressionState> abilityProgression = new HashMap<>();
    private Map<String, WeaponProficiencyState> weaponProficiencies = new HashMap<>();
    private Map<String, Integer> usageProfile = new HashMap<>();
    private String personalityArchetype = "Balanced";
    private String evolutionPath = "standard";

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

    public float getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(float currentHealth) {
        this.currentHealth = currentHealth < 0f ? -1f : currentHealth;
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

    public Map<String, Integer> getUsageProfile() {
        return usageProfile;
    }

    public void setUsageProfile(Map<String, Integer> usageProfile) {
        this.usageProfile = usageProfile != null ? usageProfile : new HashMap<>();
        refreshPersonalityArchetype();
    }

    public String getPersonalityArchetype() {
        return personalityArchetype != null && !personalityArchetype.isEmpty() ? personalityArchetype : "Balanced";
    }

    public void setPersonalityArchetype(String personalityArchetype) {
        this.personalityArchetype = personalityArchetype != null && !personalityArchetype.isEmpty()
            ? personalityArchetype
            : "Balanced";
    }

    public String getEvolutionPath() {
        return evolutionPath != null && !evolutionPath.isEmpty() ? evolutionPath : "standard";
    }

    public void setEvolutionPath(String evolutionPath) {
        this.evolutionPath = evolutionPath != null && !evolutionPath.isEmpty() ? evolutionPath : "standard";
    }

    public void recordUsage(String usageKey, int amount) {
        if (usageKey == null || usageKey.isEmpty() || amount <= 0) {
            return;
        }
        usageProfile.put(usageKey, usageProfile.getOrDefault(usageKey, 0) + amount);
        refreshPersonalityArchetype();
    }

    public void refreshPersonalityArchetype() {
        int support = usageProfile.getOrDefault("support", 0);
        int assault = usageProfile.getOrDefault("assault", 0);
        int control = usageProfile.getOrDefault("control", 0);
        int defense = usageProfile.getOrDefault("defense", 0);
        if (support > assault && support >= control && support >= defense) {
            personalityArchetype = "Caretaker";
            evolutionPath = "harmonic";
        } else if (control > assault && control >= defense) {
            personalityArchetype = "Tactician";
            evolutionPath = "oracle";
        } else if (defense > assault) {
            personalityArchetype = "Bulwark";
            evolutionPath = "aegis";
        } else if (assault > 0) {
            personalityArchetype = "Berserker";
            evolutionPath = "feral";
        } else {
            personalityArchetype = "Balanced";
            evolutionPath = "standard";
        }
    }
}
