package com.rogueforge.game.combat;

import java.util.ArrayList;
import java.util.List;

/**
 * Clone-based combat sandbox for previews, debugging, and balancing tools.
 */
public class CombatSimulation {
    private final CombatResolver combatResolver;

    public CombatSimulation(CombatResolver combatResolver) {
        this.combatResolver = combatResolver;
    }

    public CombatSimulationResult simulateAbility(List<BattleCombatant> combatants, BattleCombatant caster,
                                                  BattleCombatant target, AbilityDefinition ability,
                                                  float proficiencyMultiplier) {
        if (combatants == null || caster == null || target == null || ability == null) {
            return new CombatSimulationResult(0, false, 0f, false, List.of(), List.of());
        }

        List<BattleCombatant> simulatedCombatants = cloneCombatants(combatants);
        BattleCombatant simulatedCaster = findById(simulatedCombatants, caster.getId());
        BattleCombatant simulatedTarget = findById(simulatedCombatants, target.getId());
        if (simulatedCaster == null || simulatedTarget == null) {
            return new CombatSimulationResult(0, false, 0f, false, List.of(), List.of());
        }

        DamageResult result = combatResolver.resolveAndApplyAbilityDamage(
            simulatedCaster,
            simulatedTarget,
            ability,
            proficiencyMultiplier
        );

        TurnTimeline timeline = new TurnTimeline();
        timeline.initialize(simulatedCombatants);
        timeline.consumeTurn(simulatedCaster, ability.getSpeedCost());
        List<String> projectedTurns = new ArrayList<>();
        for (BattleCombatant combatant : timeline.getProjectedTurns(simulatedCombatants, 6)) {
            projectedTurns.add(combatant.getName());
        }

        List<String> replayLines = new ArrayList<>();
        replayLines.add(simulatedCaster.getName() + " previews " + ability.getName() + " on " + simulatedTarget.getName() + ".");
        if (result.damage() < 0) {
            replayLines.add(simulatedTarget.getName() + " would absorb " + Math.abs(result.damage()) + " HP.");
        } else {
            replayLines.add(simulatedTarget.getName() + " would take " + result.damage() + " damage.");
        }
        if (result.elementalBreak()) {
            replayLines.add(simulatedTarget.getName() + " would trigger an elemental break.");
        }
        if (!simulatedTarget.isAlive()) {
            replayLines.add(simulatedTarget.getName() + " would be defeated.");
        }

        return new CombatSimulationResult(
            result.damage(),
            result.elementalBreak(),
            simulatedTarget.getHealth(),
            simulatedTarget.isAlive(),
            replayLines,
            projectedTurns
        );
    }

    private List<BattleCombatant> cloneCombatants(List<BattleCombatant> combatants) {
        List<BattleCombatant> copies = new ArrayList<>();
        for (BattleCombatant combatant : combatants) {
            copies.add(cloneCombatant(combatant));
        }
        return copies;
    }

    private BattleCombatant cloneCombatant(BattleCombatant combatant) {
        SimulatedBattleCombatant copy = new SimulatedBattleCombatant(
            combatant.getId(),
            combatant.getName(),
            combatant.isAlly(),
            combatant.getPartyIndex(),
            combatant.getRank(),
            combatant.getAiProfile(),
            combatant.getCombatClass(),
            combatant.getHealth(),
            combatant.getMaxHealth(),
            combatant.getAgility(),
            combatant.getStrength(),
            combatant.getIntelligence(),
            combatant.getStamina(),
            copyAbilities(combatant.getAbilities()),
            new ArrayList<>(combatant.getWeaknesses()),
            new ArrayList<>(combatant.getResistances()),
            new ArrayList<>(combatant.getAbsorbs()),
            combatant.getRewardGold(),
            combatant.getRewardExperience(),
            combatant.getSourceReference(),
            new ArrayList<>(combatant.getUniqueBoosts())
        );
        copy.copyRuntimeStateFrom(combatant);
        return copy;
    }

    private List<AbilityInstance> copyAbilities(List<AbilityInstance> abilities) {
        List<AbilityInstance> copies = new ArrayList<>();
        if (abilities == null) {
            return copies;
        }
        for (AbilityInstance ability : abilities) {
            if (ability != null) {
                copies.add(ability.copy());
            }
        }
        return copies;
    }

    private BattleCombatant findById(List<BattleCombatant> combatants, String id) {
        for (BattleCombatant combatant : combatants) {
            if (combatant != null && combatant.getId().equals(id)) {
                return combatant;
            }
        }
        return null;
    }

    private static final class SimulatedBattleCombatant extends AbstractBattleCombatant {
        private SimulatedBattleCombatant(String id, String name, boolean ally, int partyIndex, String rank, String aiProfile,
                                         String combatClass, float health, float maxHealth, float agility, float strength,
                                         float intelligence, float stamina, List<AbilityInstance> abilities,
                                         List<Element> weaknesses, List<Element> resistances, List<Element> absorbs,
                                         int rewardGold, int rewardExperience, Object sourceReference, List<String> uniqueBoosts) {
            super(id, name, ally, partyIndex, rank, aiProfile, combatClass, health, maxHealth, agility, strength,
                intelligence, stamina, abilities, weaknesses, resistances, absorbs, rewardGold, rewardExperience,
                sourceReference, uniqueBoosts);
        }
    }
}
