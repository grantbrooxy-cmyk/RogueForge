package com.rogueforge.game.combat;

import java.util.List;
import java.util.Set;

/**
 * Runtime combatant contract used by the turn-based battle system.
 */
public interface BattleCombatant {
    String getId();
    String getName();
    boolean isAlly();
    int getPartyIndex();
    String getAiProfile();
    String getRank();
    String getCombatClass();
    boolean isCombatClass(String value);
    float getHealth();
    float getMaxHealth();
    float getAgility();
    float getStrength();
    float getIntelligence();
    float getStamina();
    List<AbilityInstance> getAbilities();
    List<Element> getWeaknesses();
    List<Element> getResistances();
    List<Element> getAbsorbs();
    StatusEffectManager getStatusEffectManager();
    int getRewardGold();
    int getRewardExperience();
    Object getSourceReference();
    boolean hasUniqueBoost(String uniqueBoost);
    Set<String> getUniqueBoosts();
    boolean hasElementalBreak(Element element);
    int registerElementalHit(Element element);
    boolean isAlive();
    void applyDirectDamage(float amount);
    void heal(float amount);
    float getEffectiveSpeed();
    float getEffectiveStamina();
}
