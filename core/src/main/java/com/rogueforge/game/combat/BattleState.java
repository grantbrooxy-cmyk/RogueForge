package com.rogueforge.game.combat;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared battle state owned by the combat layer instead of the screen.
 */
public class BattleState {
    private final List<BattleCombatant> combatants = new ArrayList<>();
    private final TurnTimeline turnTimeline = new TurnTimeline();

    public BattleState(List<BattleCombatant> combatants) {
        if (combatants != null) {
            this.combatants.addAll(combatants);
        }
        turnTimeline.initialize(this.combatants);
    }

    public List<BattleCombatant> getCombatants() {
        return combatants;
    }

    public TurnTimeline getTurnTimeline() {
        return turnTimeline;
    }

    public List<BattleCombatant> getAllies() {
        return filter(true);
    }

    public List<BattleCombatant> getEnemies() {
        return filter(false);
    }

    public boolean hasLivingAllies() {
        for (BattleCombatant combatant : combatants) {
            if (combatant.isAlly() && combatant.isAlive()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasLivingEnemies() {
        for (BattleCombatant combatant : combatants) {
            if (!combatant.isAlly() && combatant.isAlive()) {
                return true;
            }
        }
        return false;
    }

    private List<BattleCombatant> filter(boolean ally) {
        List<BattleCombatant> filtered = new ArrayList<>();
        for (BattleCombatant combatant : combatants) {
            if (combatant.isAlly() == ally) {
                filtered.add(combatant);
            }
        }
        return filtered;
    }
}
