package com.rogueforge.game.combat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared runtime implementation for battle combatants.
 */
public abstract class AbstractBattleCombatant implements BattleCombatant {
    private final String id;
    private final String name;
    private final boolean ally;
    private final int partyIndex;
    private final String rank;
    private final String aiProfile;
    private final String combatClass;
    private final List<AbilityInstance> abilities;
    private final List<Element> weaknesses;
    private final List<Element> resistances;
    private final List<Element> absorbs;
    private final StatusEffectManager statusEffectManager = new StatusEffectManager();
    private final int rewardGold;
    private final int rewardExperience;
    private final Object sourceReference;
    private final Set<Element> elementalBreaks = new HashSet<>();
    private final Set<String> uniqueBoosts;

    private float health;
    private final float maxHealth;
    private final float agility;
    private final float strength;
    private final float intelligence;
    private final float stamina;
    private Element lastElementHit = Element.NONE;
    private int consecutiveElementHits;

    protected AbstractBattleCombatant(String id, String name, boolean ally, int partyIndex, String rank, String aiProfile,
                                      String combatClass, float health, float maxHealth, float agility, float strength,
                                      float intelligence, float stamina, List<AbilityInstance> abilities,
                                      List<Element> weaknesses, List<Element> resistances, List<Element> absorbs,
                                      int rewardGold, int rewardExperience, Object sourceReference, List<String> uniqueBoosts) {
        this.id = id;
        this.name = name;
        this.ally = ally;
        this.partyIndex = partyIndex;
        this.rank = rank;
        this.aiProfile = aiProfile;
        this.combatClass = combatClass != null ? combatClass : "";
        this.health = health;
        this.maxHealth = maxHealth;
        this.agility = agility;
        this.strength = strength;
        this.intelligence = intelligence;
        this.stamina = stamina;
        this.abilities = abilities != null ? abilities : new ArrayList<>();
        this.weaknesses = weaknesses != null ? weaknesses : new ArrayList<>();
        this.resistances = resistances != null ? resistances : new ArrayList<>();
        this.absorbs = absorbs != null ? absorbs : new ArrayList<>();
        this.rewardGold = rewardGold;
        this.rewardExperience = rewardExperience;
        this.sourceReference = sourceReference;
        this.uniqueBoosts = new HashSet<>();
        if (uniqueBoosts != null) {
            this.uniqueBoosts.addAll(uniqueBoosts);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isAlly() {
        return ally;
    }

    @Override
    public int getPartyIndex() {
        return partyIndex;
    }

    @Override
    public String getAiProfile() {
        return aiProfile;
    }

    @Override
    public String getRank() {
        return rank;
    }

    @Override
    public String getCombatClass() {
        return combatClass;
    }

    @Override
    public boolean isCombatClass(String value) {
        if (value == null || combatClass.isEmpty()) {
            return false;
        }
        String[] parts = combatClass.split("/");
        for (String part : parts) {
            if (value.equalsIgnoreCase(part.trim())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float getHealth() {
        return health;
    }

    @Override
    public float getMaxHealth() {
        return maxHealth;
    }

    @Override
    public float getAgility() {
        return agility;
    }

    @Override
    public float getStrength() {
        return strength;
    }

    @Override
    public float getIntelligence() {
        return intelligence;
    }

    @Override
    public float getStamina() {
        return stamina;
    }

    @Override
    public List<AbilityInstance> getAbilities() {
        return abilities;
    }

    @Override
    public List<Element> getWeaknesses() {
        return weaknesses;
    }

    @Override
    public List<Element> getResistances() {
        return resistances;
    }

    @Override
    public List<Element> getAbsorbs() {
        return absorbs;
    }

    @Override
    public StatusEffectManager getStatusEffectManager() {
        return statusEffectManager;
    }

    @Override
    public int getRewardGold() {
        return rewardGold;
    }

    @Override
    public int getRewardExperience() {
        return rewardExperience;
    }

    @Override
    public Object getSourceReference() {
        return sourceReference;
    }

    @Override
    public boolean hasUniqueBoost(String uniqueBoost) {
        return uniqueBoost != null && uniqueBoosts.contains(uniqueBoost);
    }

    @Override
    public Set<String> getUniqueBoosts() {
        return new HashSet<>(uniqueBoosts);
    }

    @Override
    public boolean hasElementalBreak(Element element) {
        return element != null && elementalBreaks.contains(element);
    }

    @Override
    public int registerElementalHit(Element element) {
        if (element == null || element == Element.NONE) {
            lastElementHit = Element.NONE;
            consecutiveElementHits = 0;
            return 0;
        }
        if (element == lastElementHit) {
            consecutiveElementHits++;
        } else {
            lastElementHit = element;
            consecutiveElementHits = 1;
        }
        if (consecutiveElementHits >= 3) {
            elementalBreaks.add(element);
        }
        return consecutiveElementHits;
    }

    @Override
    public boolean isAlive() {
        return health > 0f;
    }

    @Override
    public void applyDirectDamage(float amount) {
        health = Math.max(0f, health - Math.max(1f, amount));
    }

    @Override
    public void heal(float amount) {
        health = Math.min(maxHealth, health + Math.max(0f, amount));
    }

    @Override
    public float getEffectiveSpeed() {
        return Math.max(1f, agility * statusEffectManager.getSpeedMultiplier());
    }

    @Override
    public float getEffectiveStamina() {
        float modified = stamina;
        if (statusEffectManager.has(StatusEffectType.BURN)) {
            modified *= 0.9f;
        }
        return modified;
    }
}
