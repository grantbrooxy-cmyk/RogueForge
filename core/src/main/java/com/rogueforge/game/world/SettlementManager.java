package com.rogueforge.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages settlement upgrades and their reward payloads.
 */
public class SettlementManager {
    private final Map<String, SettlementUpgradeDefinition> definitions = new HashMap<>();

    public SettlementManager() {
        SettlementUpgradeDefinition[] loaded = new Json().fromJson(
            SettlementUpgradeDefinition[].class,
            Gdx.files.internal("data/settlement_upgrades.json").readString()
        );
        if (loaded != null) {
            for (SettlementUpgradeDefinition definition : loaded) {
                if (definition != null && definition.getId() != null) {
                    definitions.put(definition.getId(), definition);
                }
            }
        }
    }

    public SettlementUpgradeDefinition get(String upgradeId) {
        return definitions.get(upgradeId);
    }

    public Collection<SettlementUpgradeDefinition> getAll() {
        return new ArrayList<>(definitions.values());
    }
}
