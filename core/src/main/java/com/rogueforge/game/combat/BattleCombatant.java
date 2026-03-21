package com.rogueforge.game.combat;

import java.util.ArrayList;
import java.util.List;

/**
 * Runtime combatant used by the turn-based battle system.
 */
public class BattleCombatant {
    private final String id;
    private final String name;
    private final boolean ally;
    private final int partyIndex;
    private final String rank;
    private final String aiProfile;
    private final List<AbilityInstance> abilities;
    private final List<Element> weaknesses;
    private final List<Element> resistances;
    private final List<Element> absorbs;
    private final StatusEffectManager statusEffectManager = new StatusEffectManager();
    private final int rewardGold;
    private final int rewardExperience;
    private final Object sourceReference;

    private float health;
    private final float maxHealth;
    private final float agility;
    private final float strength;
    private final float intelligence;
    private final float stamina;

    public BattleCombatant(String id, String name, boolean ally, int partyIndex, String rank, String aiProfile,
                           float health, float maxHealth, float agility, float strength,
                           float intelligence, float stamina, List<AbilityInstance> abilities,
                           List<Element> weaknesses, List<Element> resistances, List<Element> absorbs,
                           int rewardGold, int rewardExperience, Object sourceReference) {
        this.id = id;
        this.name = name;
        this.ally = ally;
        this.partyIndex = partyIndex;
        this.rank = rank;
        this.aiProfile = aiProfile;
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
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAlly() {
        return ally;
    }

    public int getPartyIndex() {
        return partyIndex;
    }

    public String getAiProfile() {
        return aiProfile;
    }

    public String getRank() {
        return rank;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getAgility() {
        return agility;
    }

    public float getStrength() {
        return strength;
    }

    public float getIntelligence() {
        return intelligence;
    }

    public float getStamina() {
        return stamina;
    }

    public List<AbilityInstance> getAbilities() {
        return abilities;
    }

    public List<Element> getWeaknesses() {
        return weaknesses;
    }

    public List<Element> getResistances() {
        return resistances;
    }

    public List<Element> getAbsorbs() {
        return absorbs;
    }

    public StatusEffectManager getStatusEffectManager() {
        return statusEffectManager;
    }

    public int getRewardGold() {
        return rewardGold;
    }

    public int getRewardExperience() {
        return rewardExperience;
    }

    public Object getSourceReference() {
        return sourceReference;
    }

    public boolean isAlive() {
        return health > 0f;
    }

    public void applyDirectDamage(float amount) {
        health = Math.max(0f, health - Math.max(1f, amount));
    }

    public void heal(float amount) {
        health = Math.min(maxHealth, health + Math.max(0f, amount));
    }

    public float getEffectiveSpeed() {
        return Math.max(1f, agility * statusEffectManager.getSpeedMultiplier());
    }

    public float getEffectiveStamina() {
        float modified = stamina;
        if (statusEffectManager.has(StatusEffectType.BURN)) {
            modified *= 0.9f;
        }
        return modified;
    }
}
