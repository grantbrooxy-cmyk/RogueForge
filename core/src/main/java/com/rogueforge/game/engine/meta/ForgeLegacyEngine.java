package com.rogueforge.game.engine.meta;

import com.rogueforge.game.data.MetaProgressionState;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Endgame progression engine for Forge Shards, legacy unlocks, and Act 5 milestones.
 */
public class ForgeLegacyEngine {
    private static final int LEGENDARY_UNLOCK_INTERVAL = 10;
    private static final int HARD_MODE_SEED_INTERVAL = 15;

    private final Map<String, ForgeLegacyNodeDefinition> nodesById = new LinkedHashMap<>();
    private final List<String> orderedNodeIds = new ArrayList<>();

    public ForgeLegacyEngine() {
        registerDefaults();
    }

    public ForgeLegacyBonuses getBonuses(MetaProgressionState state) {
        ForgeLegacyBonuses bonuses = new ForgeLegacyBonuses();
        if (state == null) {
            return bonuses;
        }
        for (String nodeId : state.getPurchasedLegacyNodeIds()) {
            applyNodeBonus(bonuses, nodeId);
        }
        return bonuses;
    }

    public ForgeLegacyNodeDefinition getNode(String nodeId) {
        return nodeId != null ? nodesById.get(nodeId) : null;
    }

    public ForgeLegacyNodeDefinition getNextAffordableNode(MetaProgressionState state) {
        if (state == null) {
            return null;
        }
        for (String nodeId : orderedNodeIds) {
            if (state.getPurchasedLegacyNodeIds().contains(nodeId)) {
                continue;
            }
            ForgeLegacyNodeDefinition node = nodesById.get(nodeId);
            if (node != null && state.getForgeShards() >= node.getCost()) {
                return node;
            }
        }
        return null;
    }

    public ForgeLegacyNodeDefinition getNextLockedNode(MetaProgressionState state) {
        if (state == null) {
            return null;
        }
        for (String nodeId : orderedNodeIds) {
            if (!state.getPurchasedLegacyNodeIds().contains(nodeId)) {
                return nodesById.get(nodeId);
            }
        }
        return null;
    }

    public boolean purchaseNode(MetaProgressionState state, String nodeId) {
        if (state == null || nodeId == null || nodeId.isEmpty()) {
            return false;
        }
        ForgeLegacyNodeDefinition node = nodesById.get(nodeId);
        if (node == null || state.getPurchasedLegacyNodeIds().contains(nodeId) || state.getForgeShards() < node.getCost()) {
            return false;
        }
        state.setForgeShards(state.getForgeShards() - node.getCost());
        state.getPurchasedLegacyNodeIds().add(nodeId);
        return true;
    }

    public int getDeathShardBonus(int floorReached) {
        if (floorReached >= 31) {
            return 60;
        }
        if (floorReached >= 16) {
            return 35;
        }
        if (floorReached >= 6) {
            return 15;
        }
        return 0;
    }

    public int getSealRunBonus() {
        return 50;
    }

    public int getFloorClearReward() {
        return 10;
    }

    public int getBossFloorReward() {
        return 25;
    }

    public int getLegendaryRobotsUnlocked(int bestFloor) {
        return Math.max(0, Math.min(3, bestFloor / LEGENDARY_UNLOCK_INTERVAL));
    }

    public int getHardModeSeedsUnlocked(int bestFloor) {
        return Math.max(0, Math.min(3, bestFloor / HARD_MODE_SEED_INTERVAL));
    }

    public boolean areChallengeRunsUnlocked(int bestFloor) {
        return bestFloor >= 15;
    }

    public boolean isBossRushUnlocked(int defeatedEndgameBosses) {
        return defeatedEndgameBosses >= 2;
    }

    public String describeEmpireTier(int territories, int guildSettlements) {
        int score = Math.max(0, territories) + (Math.max(0, guildSettlements) * 2);
        if (score >= 8) {
            return "Frontier Empire";
        }
        if (score >= 5) {
            return "Regional Power";
        }
        if (score >= 2) {
            return "Rising Dominion";
        }
        return "Expedition Crew";
    }

    private void applyNodeBonus(ForgeLegacyBonuses bonuses, String nodeId) {
        if (bonuses == null || nodeId == null || nodeId.isEmpty()) {
            return;
        }
        switch (nodeId) {
            case "reserve_cell":
                bonuses.addStartingPotionBonus(1);
                break;
            case "loaded_manifest":
                bonuses.addStartingGoldBonus(100);
                break;
            case "toughened_frame":
                bonuses.multiplyMaxHealth(1.05f);
                break;
            case "hardened_core":
                bonuses.multiplyAttack(1.10f);
                break;
            case "adaptive_systems":
                bonuses.addRobotSpeedBonus(1f);
                break;
            default:
                break;
        }
    }

    private void registerDefaults() {
        registerNode(new ForgeLegacyNodeDefinition(
            "reserve_cell", "Forge Foundation", "Reserve Cell", 100,
            "Start each fresh run with one extra healing consumable."
        ));
        registerNode(new ForgeLegacyNodeDefinition(
            "loaded_manifest", "Forge Foundation", "Loaded Manifest", 600,
            "Start each fresh run with an additional 100 gold."
        ));
        registerNode(new ForgeLegacyNodeDefinition(
            "toughened_frame", "Ironhaven Legacy", "Toughened Frame", 100,
            "Increase player max HP by 5 percent in the overworld and endgame loops."
        ));
        registerNode(new ForgeLegacyNodeDefinition(
            "hardened_core", "Ironhaven Legacy", "Hardened Core", 350,
            "Increase physical attack scaling by 10 percent."
        ));
        registerNode(new ForgeLegacyNodeDefinition(
            "adaptive_systems", "Ironhaven Legacy", "Adaptive Systems", 500,
            "All robots gain +1 base speed."
        ));
    }

    private void registerNode(ForgeLegacyNodeDefinition node) {
        if (node == null || node.getId() == null || node.getId().isEmpty()) {
            return;
        }
        nodesById.put(node.getId(), node);
        orderedNodeIds.add(node.getId());
    }
}
