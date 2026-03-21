package com.rogueforge.game.progression;

import com.rogueforge.game.combat.AbilityDefinition;
import com.rogueforge.game.combat.AbilityRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles mastery upgrades and unique-skill unlocks for robot abilities.
 */
public final class AbilityEvolutionManager {
    private AbilityEvolutionManager() {
    }

    public static List<String> applyMasteryUnlocks(RobotProgressionState robotState,
                                                   Map<String, RobotProgressionState> partyStates) {
        List<String> messages = new ArrayList<>();
        if (robotState == null || robotState.getKnownAbilityIds() == null) {
            return messages;
        }

        List<String> known = robotState.getKnownAbilityIds();
        for (int i = 0; i < known.size(); i++) {
            String abilityId = known.get(i);
            AbilityProgressionState progression = robotState.getAbilityProgression().get(abilityId);
            AbilityDefinition definition = AbilityRegistry.get(abilityId);
            if (progression == null || definition == null || progression.getProficiencyLevel() < 10) {
                continue;
            }

            String masteryUpgradeId = definition.getMasteryUpgradeId();
            if (!masteryUpgradeId.isEmpty() && !known.contains(masteryUpgradeId)) {
                AbilityDefinition upgraded = AbilityRegistry.get(masteryUpgradeId);
                known.set(i, masteryUpgradeId);
                robotState.getAbilityProgression().put(masteryUpgradeId, progression);
                robotState.getAbilityProgression().remove(abilityId);
                if (upgraded != null) {
                    messages.add(robotState.getDisplayName() + " mastered " + definition.getName()
                        + " and unlocked " + upgraded.getName() + ".");
                }
                abilityId = masteryUpgradeId;
                definition = upgraded != null ? upgraded : definition;
            }

            String uniqueSkillId = definition != null ? definition.getUniqueSkillId() : "";
            if (!uniqueSkillId.isEmpty() && !known.contains(uniqueSkillId) && !isUniqueTaken(uniqueSkillId, partyStates)) {
                AbilityDefinition unique = AbilityRegistry.get(uniqueSkillId);
                if (unique != null) {
                    known.add(uniqueSkillId);
                    robotState.getOrCreateAbilityProgression(uniqueSkillId);
                    messages.add(robotState.getDisplayName() + " awakened unique skill " + unique.getName() + ".");
                }
            }
        }
        return messages;
    }

    private static boolean isUniqueTaken(String uniqueSkillId, Map<String, RobotProgressionState> partyStates) {
        if (partyStates == null) {
            return false;
        }
        for (RobotProgressionState state : partyStates.values()) {
            if (state != null && state.getKnownAbilityIds() != null && state.getKnownAbilityIds().contains(uniqueSkillId)) {
                return true;
            }
        }
        return false;
    }
}
