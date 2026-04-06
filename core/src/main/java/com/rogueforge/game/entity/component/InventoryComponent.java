package com.rogueforge.game.entity.component;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal inventory payload for lightweight entity composition.
 */
public class InventoryComponent {
    public final List<String> itemIds = new ArrayList<>();
    public final List<String> equipmentIds = new ArrayList<>();
    public long currency;
}
