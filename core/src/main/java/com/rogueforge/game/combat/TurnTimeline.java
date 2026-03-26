package com.rogueforge.game.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CTB turn timeline. Each combatant has a tick counter; whoever has the lowest
 * tick acts next. After acting, their tick advances by (speedCost * 100 / speed).
 *
 * Initialisation: all combatants start at tick 0, so the very first action
 * begins immediately. Ties are broken by effective speed (higher speed goes
 * first), then alphabetically — no built-in ally advantage.
 *
 * getProjectedTurns() simulates future turns so the UI can show a full
 * 8-10 slot preview (with the same combatant appearing multiple times).
 */
public class TurnTimeline {
    /** Default speed cost assumed when projecting future turns with no known action. */
    private static final int DEFAULT_ACTION_COST = 80;

    private final Map<String, Integer> scheduledTicks = new HashMap<>();

    // ── Lifecycle ──────────────────────────────────────────────────────────

    public void initialize(List<BattleCombatant> combatants) {
        scheduledTicks.clear();
        if (combatants == null) {
            return;
        }
        // All combatants begin at tick 0. The speed tiebreak in the sorter
        // ensures the fastest combatant acts first when ticks are equal.
        for (BattleCombatant combatant : combatants) {
            scheduledTicks.put(combatant.getId(), 0);
        }
    }

    // ── Querying ───────────────────────────────────────────────────────────

    /** Returns the combatant whose turn it is right now. */
    public BattleCombatant getCurrentActor(List<BattleCombatant> combatants) {
        List<BattleCombatant> sorted = sortByTick(combatants, scheduledTicks);
        return sorted.isEmpty() ? null : sorted.get(0);
    }

    /**
     * Returns up to {@code count} upcoming turns in order, simulating future
     * turns so that the same combatant can appear more than once. This is used
     * by the UI preview bar.
     */
    public List<BattleCombatant> getProjectedTurns(List<BattleCombatant> combatants, int count) {
        if (combatants == null || count <= 0) {
            return new ArrayList<>();
        }

        // Build a working copy of the tick map so simulation does not
        // affect the real state.
        Map<String, Integer> simTicks = new HashMap<>(scheduledTicks);

        // Include only alive combatants.
        List<BattleCombatant> alive = new ArrayList<>();
        for (BattleCombatant c : combatants) {
            if (c.isAlive()) {
                alive.add(c);
            }
        }

        List<BattleCombatant> result = new ArrayList<>();
        for (int i = 0; i < count && !alive.isEmpty(); i++) {
            List<BattleCombatant> sorted = sortByTick(alive, simTicks);
            if (sorted.isEmpty()) {
                break;
            }
            BattleCombatant next = sorted.get(0);
            result.add(next);

            // Advance simulated tick using default action cost.
            int current = simTicks.getOrDefault(next.getId(), 0);
            simTicks.put(next.getId(), current + getDelay(next, DEFAULT_ACTION_COST));
        }
        return result;
    }

    /**
     * @deprecated Use {@link #getProjectedTurns(List, int)} for a proper
     *             preview that can repeat combatants. This method simply
     *             returns the current queue order without simulation.
     */
    @Deprecated
    public List<BattleCombatant> getNextTurns(List<BattleCombatant> combatants, int count) {
        List<BattleCombatant> sorted = sortByTick(combatants, scheduledTicks);
        if (sorted.size() <= count) {
            return sorted;
        }
        return new ArrayList<>(sorted.subList(0, count));
    }

    // ── Mutation ───────────────────────────────────────────────────────────

    /**
     * Advances the acting combatant's tick by the delay corresponding to the
     * chosen action's speed cost. Call this immediately after any action is
     * resolved.
     */
    public void consumeTurn(BattleCombatant combatant, int speedCost) {
        if (combatant == null) {
            return;
        }
        int current = scheduledTicks.getOrDefault(combatant.getId(), 0);
        scheduledTicks.put(combatant.getId(), current + getDelay(combatant, speedCost));
    }

    /** Removes a dead combatant from the timeline. */
    public void remove(String combatantId) {
        scheduledTicks.remove(combatantId);
    }

    // ── Internal helpers ───────────────────────────────────────────────────

    /**
     * Sorts alive combatants by ascending tick. Ties are broken by descending
     * effective speed (faster combatant acts first), then alphabetically by
     * name for determinism.
     */
    private List<BattleCombatant> sortByTick(List<BattleCombatant> combatants,
                                              Map<String, Integer> ticks) {
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
            .comparingInt((BattleCombatant c) -> ticks.getOrDefault(c.getId(), Integer.MAX_VALUE))
            .thenComparingInt((BattleCombatant c) -> -(int) c.getEffectiveSpeed()) // higher speed wins ties
            .thenComparing(BattleCombatant::getName));
        return sorted;
    }

    /**
     * Converts a speed cost into a tick delay for a specific combatant.
     * Formula: delay = max(1, round(speedCost * 100 / effectiveSpeed))
     * A faster combatant (high speed) produces a smaller delay, so they act
     * sooner. A Defend action (speedCost=40) delays ~half as long as a heavy
     * ability (speedCost=120), giving Defend its tactical timeline benefit.
     */
    private int getDelay(BattleCombatant combatant, int speedCost) {
        return Math.max(1, Math.round((speedCost * 100f) / combatant.getEffectiveSpeed()));
    }
}
