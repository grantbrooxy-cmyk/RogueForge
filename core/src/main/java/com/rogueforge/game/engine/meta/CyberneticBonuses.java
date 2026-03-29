package com.rogueforge.game.engine.meta;

import java.util.HashMap;
import java.util.Map;

/**
 * Aggregated passive bonuses from owned cybernetic enhancements.
 */
public class CyberneticBonuses {
    private float hpBonus;
    private float agilityBonus;
    private float strengthBonus;
    private float intelligenceBonus;
    private float staminaBonus;
    private int startingHealingPotionsBonus;
    private long startingGoldBonus;
    private int harvestYieldBonus;
    private float experienceMultiplier = 1f;
    private final Map<String, Integer> startingForgeComponents = new HashMap<>();

    public float getHpBonus() { return hpBonus; }
    public void addHpBonus(float value) { hpBonus += value; }
    public float getAgilityBonus() { return agilityBonus; }
    public void addAgilityBonus(float value) { agilityBonus += value; }
    public float getStrengthBonus() { return strengthBonus; }
    public void addStrengthBonus(float value) { strengthBonus += value; }
    public float getIntelligenceBonus() { return intelligenceBonus; }
    public void addIntelligenceBonus(float value) { intelligenceBonus += value; }
    public float getStaminaBonus() { return staminaBonus; }
    public void addStaminaBonus(float value) { staminaBonus += value; }
    public int getStartingHealingPotionsBonus() { return startingHealingPotionsBonus; }
    public void addStartingHealingPotionsBonus(int value) { startingHealingPotionsBonus += value; }
    public long getStartingGoldBonus() { return startingGoldBonus; }
    public void addStartingGoldBonus(long value) { startingGoldBonus += value; }
    public int getHarvestYieldBonus() { return harvestYieldBonus; }
    public void addHarvestYieldBonus(int value) { harvestYieldBonus += value; }
    public float getExperienceMultiplier() { return experienceMultiplier; }
    public void multiplyExperienceMultiplier(float multiplier) { experienceMultiplier *= Math.max(0.1f, multiplier); }
    public Map<String, Integer> getStartingForgeComponents() { return new HashMap<>(startingForgeComponents); }

    public void addStartingForgeComponent(String componentId, int amount) {
        if (componentId == null || componentId.isEmpty() || amount <= 0) {
            return;
        }
        startingForgeComponents.merge(componentId, amount, Integer::sum);
    }
}
