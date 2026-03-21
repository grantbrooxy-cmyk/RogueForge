package com.rogueforge.game.combat;

/**
 * Battle action metadata used to feed the timeline.
 */
public class BattleAction {
    public enum Kind {
        ATTACK,
        ABILITY,
        ITEM,
        DEFEND,
        ANALYZE,
        FLEE
    }

    private final Kind kind;
    private final int speedCost;
    private final String label;

    public BattleAction(Kind kind, int speedCost, String label) {
        this.kind = kind;
        this.speedCost = speedCost;
        this.label = label;
    }

    public Kind getKind() {
        return kind;
    }

    public int getSpeedCost() {
        return speedCost;
    }

    public String getLabel() {
        return label;
    }
}
