package com.rogueforge.game.combat;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks reveal levels for scanned monsters.
 */
public class BestiaryManager {
    private final Map<String, Integer> scanLevels = new HashMap<>();

    public int recordScan(String monsterId, int bonusLevels) {
        if (monsterId == null || monsterId.isEmpty()) {
            return 0;
        }
        int current = scanLevels.getOrDefault(monsterId, 0);
        int next = Math.min(3, current + Math.max(1, bonusLevels));
        scanLevels.put(monsterId, next);
        return next;
    }

    public int getScanLevel(String monsterId) {
        return scanLevels.getOrDefault(monsterId, 0);
    }

    public Map<String, Integer> exportData() {
        return new HashMap<>(scanLevels);
    }

    public void importData(Map<String, Integer> data) {
        scanLevels.clear();
        if (data != null) {
            scanLevels.putAll(data);
        }
    }
}
