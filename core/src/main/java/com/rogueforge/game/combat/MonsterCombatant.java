package com.rogueforge.game.combat;

import java.util.List;

/**
 * Runtime battle combatant for enemies.
 */
public class MonsterCombatant extends AbstractBattleCombatant {
    public MonsterCombatant(String id, String name, int partyIndex, String rank, String aiProfile,
                            String combatClass, float health, float maxHealth, float agility, float strength,
                            float intelligence, float stamina, List<AbilityInstance> abilities,
                            List<Element> weaknesses, List<Element> resistances, List<Element> absorbs,
                            int rewardGold, int rewardExperience, Object sourceReference, List<String> uniqueBoosts) {
        super(id, name, false, partyIndex, rank, aiProfile, combatClass, health, maxHealth, agility, strength,
            intelligence, stamina, abilities, weaknesses, resistances, absorbs, rewardGold, rewardExperience,
            sourceReference, uniqueBoosts);
    }
}
