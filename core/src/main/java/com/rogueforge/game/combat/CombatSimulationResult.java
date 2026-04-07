package com.rogueforge.game.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Non-mutating combat preview output for debugging and UI planning.
 */
public class CombatSimulationResult {
    private final int appliedDamage;
    private final boolean elementalBreak;
    private final float targetHealthAfter;
    private final boolean targetAliveAfter;
    private final List<String> replayLines;
    private final List<String> projectedTurnOrder;

    public CombatSimulationResult(int appliedDamage, boolean elementalBreak, float targetHealthAfter,
                                  boolean targetAliveAfter, List<String> replayLines, List<String> projectedTurnOrder) {
        this.appliedDamage = appliedDamage;
        this.elementalBreak = elementalBreak;
        this.targetHealthAfter = targetHealthAfter;
        this.targetAliveAfter = targetAliveAfter;
        this.replayLines = replayLines != null ? new ArrayList<>(replayLines) : new ArrayList<>();
        this.projectedTurnOrder = projectedTurnOrder != null ? new ArrayList<>(projectedTurnOrder) : new ArrayList<>();
    }

    public int getAppliedDamage() {
        return appliedDamage;
    }

    public boolean isElementalBreak() {
        return elementalBreak;
    }

    public float getTargetHealthAfter() {
        return targetHealthAfter;
    }

    public boolean isTargetAliveAfter() {
        return targetAliveAfter;
    }

    public List<String> getReplayLines() {
        return Collections.unmodifiableList(replayLines);
    }

    public List<String> getProjectedTurnOrder() {
        return Collections.unmodifiableList(projectedTurnOrder);
    }
}
