package com.rogueforge.game.event;

import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.data.MonsterDefinition;

/**
 * Event fired when a monster drops loot upon defeat.
 */
public class LootDropEvent {
    private MonsterDefinition monsterDefinition;
    private Vector2 position;

    public LootDropEvent(MonsterDefinition monsterDefinition, Vector2 position) {
        this.monsterDefinition = monsterDefinition;
        this.position = new Vector2(position);
    }

    public MonsterDefinition getMonsterDefinition() {
        return monsterDefinition;
    }

    public Vector2 getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "LootDropEvent{" +
                "monsterDefinition=" + (monsterDefinition != null ? monsterDefinition.getId() : "null") +
                ", position=" + position +
                '}';
    }
}
