package com.rogueforge.game.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.rogueforge.game.data.DefinitionRegistries;
import com.rogueforge.game.engine.ServiceLifecycle;

/**
 * Manages settlement upgrades and their reward payloads.
 */
public class SettlementManager implements ServiceLifecycle {
    private final Map<String, SettlementUpgradeDefinition> definitions = new HashMap<>();
    private final Map<String, SettlementNpcScheduleDefinition> npcSchedules = new HashMap<>();

    public SettlementManager() {
        reloadDefinitions();
    }

    @Override
    public void initialize() {
        reloadDefinitions();
    }

    public void reloadDefinitions() {
        definitions.clear();
        for (SettlementUpgradeDefinition definition : DefinitionRegistries.SETTLEMENT_UPGRADES.getAll()) {
            if (definition != null && definition.getId() != null) {
                definitions.put(definition.getId(), definition);
            }
        }

        npcSchedules.clear();
        for (SettlementNpcScheduleDefinition schedule : DefinitionRegistries.SETTLEMENT_NPC_SCHEDULES.getAll()) {
            if (schedule != null && schedule.getNpcId() != null) {
                npcSchedules.put(schedule.getNpcId(), schedule);
            }
        }
    }

    public SettlementUpgradeDefinition get(String upgradeId) {
        return definitions.get(upgradeId);
    }

    public Collection<SettlementUpgradeDefinition> getAll() {
        return new ArrayList<>(definitions.values());
    }

    public SettlementNpcScheduleDefinition getNpcSchedule(String npcId) {
        return npcId != null ? npcSchedules.get(npcId) : null;
    }
}
