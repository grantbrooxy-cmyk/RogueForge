package com.rogueforge.game.entity.component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight progression payload for weapons, abilities, and ranks.
 */
public class ProficiencyComponent implements Component {
    public final Map<String, Integer> weaponExperience = new LinkedHashMap<>();
    public final Map<String, Integer> abilityExperience = new LinkedHashMap<>();
    public final Map<String, Integer> rankLevels = new LinkedHashMap<>();

    public void addWeaponExperience(String weaponId, int amount) {
        addToMap(weaponExperience, weaponId, amount);
    }

    public void addAbilityExperience(String abilityId, int amount) {
        addToMap(abilityExperience, abilityId, amount);
    }

    public void setRankLevel(String rankId, int level) {
        if (rankId != null && !rankId.isEmpty()) {
            rankLevels.put(rankId, Math.max(0, level));
        }
    }

    private void addToMap(Map<String, Integer> values, String id, int amount) {
        if (id == null || id.isEmpty() || amount == 0) {
            return;
        }
        values.put(id, Math.max(0, values.getOrDefault(id, 0) + amount));
    }
}
