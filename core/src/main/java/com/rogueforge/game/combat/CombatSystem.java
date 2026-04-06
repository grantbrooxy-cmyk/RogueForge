package com.rogueforge.game.combat;

import java.util.List;

/**
 * Small combat-layer coordinator that owns battle state and resolver access.
 */
public class CombatSystem {
    private final BattleState battleState;
    private final CombatResolver combatResolver;

    public CombatSystem(List<BattleCombatant> combatants, CombatResolver combatResolver) {
        this.battleState = new BattleState(combatants);
        this.combatResolver = combatResolver;
    }

    public BattleState getBattleState() {
        return battleState;
    }

    public CombatResolver getCombatResolver() {
        return combatResolver;
    }

    public List<BattleCombatant> getCombatants() {
        return battleState.getCombatants();
    }

    public List<BattleCombatant> getAllies() {
        return battleState.getAllies();
    }

    public List<BattleCombatant> getEnemies() {
        return battleState.getEnemies();
    }

    public BattleCombatant getCurrentActor() {
        return battleState.getTurnTimeline().getCurrentActor(battleState.getCombatants());
    }

    public void consumeTurn(BattleCombatant combatant, int speedCost) {
        battleState.getTurnTimeline().consumeTurn(combatant, speedCost);
    }
}
