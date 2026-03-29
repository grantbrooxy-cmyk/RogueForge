package com.rogueforge.game.engine.meta;

public class CurseCardDefinition {
    private final String id;
    private final String name;
    private final String description;
    private final float hpBonus;
    private final float agilityBonus;
    private final float strengthBonus;
    private final float intelligenceBonus;
    private final float staminaBonus;
    private final int startingHealingPotionsBonus;
    private final int harvestYieldBonus;
    private final float experienceMultiplier;

    public CurseCardDefinition(
        String id,
        String name,
        String description,
        float hpBonus,
        float agilityBonus,
        float strengthBonus,
        float intelligenceBonus,
        float staminaBonus,
        int startingHealingPotionsBonus,
        int harvestYieldBonus,
        float experienceMultiplier
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.hpBonus = hpBonus;
        this.agilityBonus = agilityBonus;
        this.strengthBonus = strengthBonus;
        this.intelligenceBonus = intelligenceBonus;
        this.staminaBonus = staminaBonus;
        this.startingHealingPotionsBonus = startingHealingPotionsBonus;
        this.harvestYieldBonus = harvestYieldBonus;
        this.experienceMultiplier = experienceMultiplier;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getHpBonus() { return hpBonus; }
    public float getAgilityBonus() { return agilityBonus; }
    public float getStrengthBonus() { return strengthBonus; }
    public float getIntelligenceBonus() { return intelligenceBonus; }
    public float getStaminaBonus() { return staminaBonus; }
    public int getStartingHealingPotionsBonus() { return startingHealingPotionsBonus; }
    public int getHarvestYieldBonus() { return harvestYieldBonus; }
    public float getExperienceMultiplier() { return experienceMultiplier; }
}
