package com.rogueforge.game.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CTB-lite timeline that schedules turns based on speed costs.
 */
public class TurnTimeline {
    private final Map<String, Integer> scheduledTicks = new HashMap<>();

    public void initialize(List<BattleCombatant> combatants) {
        scheduledTicks.clear();
        if (combatants == null) {
            return;
        }
        for (BattleCombatant combatant : combatants) {
            scheduledTicks.put(combatant.getId(), getDelay(combatant, 80));
        }
    }

    public BattleCombatant getCurrentActor(List<BattleCombatant> combatants) {
        List<BattleCombatant> sorted = sortByTick(combatants, scheduledTicks);
        return sorted.isEmpty() ? null : sorted.get(0);
    }

    public void consumeTurn(BattleCombatant combatant, int speedCost) {
        if (combatant == null) {
            return;
        }
        int current = scheduledTicks.getOrDefault(combatant.getId(), 0);
        scheduledTicks.put(combatant.getId(), current + getDelay(combatant, speedCost));
    }

    public List<BattleCombatant> getNextTurns(List<BattleCombatant> combatants, int count) {
        List<BattleCombatant> sorted = sortByTick(combatants, scheduledTicks);
        if (sorted.size() <= count) {
            return sorted;
        }
        return new ArrayList<>(sorted.subList(0, count));
    }

    public void remove(String combatantId) {
        scheduledTicks.remove(combatantId);
    }

    private List<BattleCombatant> sortByTick(List<BattleCombatant> combatants, Map<String, Integer> ticks) {
        List<BattleCombatant> sorted = new ArrayList<>();
        if (combatants == null) {
            return sorted;
        }
        for (BattleCombatant combatant : combatants) {
            if (combatant.isAlive()) {
                sorted.add(combatant);
            }
        }
        sorted.sort(Comparator
            .comparingInt((BattleCombatant combatant) -> ticks.getOrDefault(combatant.getId(), Integer.MAX_VALUE))
            .thenComparing((BattleCombatant combatant) -> combatant.isAlly() ? 0 : 1)
            .thenComparing(BattleCombatant::getName));
        return sorted;
    }

    private int getDelay(BattleCombatant combatant, int speedCost) {
        return Math.max(1, Math.round((speedCost * 100f) / combatant.getEffectiveSpeed()));
    }
}
