package com.rogueforge.game.engine.meta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * One persistent cybernetic enhancement unlocked between runs.
 */
public class CyberneticEnhancementDefinition {
    private final String id;
    private final String name;
    private final String description;
    private final int rewardTier;
    private final float hpBonus;
    private final float agilityBonus;
    private final float strengthBonus;
    private final float intelligenceBonus;
    private final float staminaBonus;
    private final int startingHealingPotionsBonus;
    private final long startingGoldBonus;
    private final int harvestYieldBonus;
    private final float experienceMultiplier;
    private final Map<String, Integer> startingForgeComponents;

    public CyberneticEnhancementDefinition(
        String id,
        String name,
        String description,
        int rewardTier,
        float hpBonus,
        float agilityBonus,
        float strengthBonus,
        float intelligenceBonus,
        float staminaBonus,
        int startingHealingPotionsBonus,
        long startingGoldBonus,
        int harvestYieldBonus,
        float experienceMultiplier,
        Map<String, Integer> startingForgeComponents
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rewardTier = Math.max(1, Math.min(4, rewardTier));
        this.hpBonus = hpBonus;
        this.agilityBonus = agilityBonus;
        this.strengthBonus = strengthBonus;
        this.intelligenceBonus = intelligenceBonus;
        this.staminaBonus = staminaBonus;
        this.startingHealingPotionsBonus = Math.max(0, startingHealingPotionsBonus);
        this.startingGoldBonus = Math.max(0L, startingGoldBonus);
        this.harvestYieldBonus = Math.max(0, harvestYieldBonus);
        this.experienceMultiplier = Math.max(1f, experienceMultiplier);
        this.startingForgeComponents = startingForgeComponents != null
            ? Collections.unmodifiableMap(new HashMap<>(startingForgeComponents))
            : Collections.emptyMap();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getRewardTier() { return rewardTier; }
    public float getHpBonus() { return hpBonus; }
    public float getAgilityBonus() { return agilityBonus; }
    public float getStrengthBonus() { return strengthBonus; }
    public float getIntelligenceBonus() { return intelligenceBonus; }
    public float getStaminaBonus() { return staminaBonus; }
    public int getStartingHealingPotionsBonus() { return startingHealingPotionsBonus; }
    public long getStartingGoldBonus() { return startingGoldBonus; }
    public int getHarvestYieldBonus() { return harvestYieldBonus; }
    public float getExperienceMultiplier() { return experienceMultiplier; }
    public Map<String, Integer> getStartingForgeComponents() { return startingForgeComponents; }
}
