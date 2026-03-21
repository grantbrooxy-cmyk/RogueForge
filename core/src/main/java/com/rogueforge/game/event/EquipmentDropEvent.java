package com.rogueforge.game.event;

import com.rogueforge.game.data.EquipmentItem;

/**
 * Event fired when an equipment item is dropped as loot.
 */
public class EquipmentDropEvent {
    private EquipmentItem item;
    private String sourceMonsterName;

    public EquipmentDropEvent(EquipmentItem item, String sourceMonsterName) {
        this.item = item;
        this.sourceMonsterName = sourceMonsterName;
    }

    public EquipmentItem getItem() {
        return item;
    }

    public String getSourceMonsterName() {
        return sourceMonsterName;
    }

    @Override
    public String toString() {
        return "EquipmentDropEvent{" +
                "item=" + (item != null ? item.getName() : "null") +
                ", source='" + sourceMonsterName + '\'' +
                '}';
    }
}
