package com.rogueforge.game.combat;

import com.rogueforge.game.entity.component.RobotCoreComponent;
import java.util.List;

/**
 * Runtime battle combatant for deployed robots.
 */
public class RobotCombatant extends AbstractBattleCombatant {
    private final RobotCoreComponent robotCoreComponent = new RobotCoreComponent();

    public RobotCombatant(String id, String name, int partyIndex, String rank, String aiProfile,
                          String combatClass, float health, float maxHealth, float agility, float strength,
                          float intelligence, float stamina, List<AbilityInstance> abilities,
                          List<Element> weaknesses, List<Element> resistances, List<Element> absorbs,
                          int rewardGold, int rewardExperience, Object sourceReference, List<String> uniqueBoosts) {
        super(id, name, true, partyIndex, rank, aiProfile, combatClass, health, maxHealth, agility, strength,
            intelligence, stamina, abilities, weaknesses, resistances, absorbs, rewardGold, rewardExperience,
            sourceReference, uniqueBoosts);
        addComponent(RobotCoreComponent.class, robotCoreComponent);
        robotCoreComponent.robotId = id;
        robotCoreComponent.robotName = name;
        robotCoreComponent.role = combatClass;
        robotCoreComponent.slotIndex = partyIndex;
        robotCoreComponent.deployed = true;
    }

    public RobotCoreComponent robotCore() {
        return robotCoreComponent;
    }
}
