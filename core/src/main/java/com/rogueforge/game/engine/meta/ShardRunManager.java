package com.rogueforge.game.engine.meta;

import com.rogueforge.game.core.GameState;
import com.rogueforge.game.data.SaveFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages the lifecycle of a Shard Run, handling state backup,
 * loadout stripping, and reward calculation.
 */
public class ShardRunManager {
    private final GameState gameState;
    private final ForgeLegacyEngine legacyEngine;
    private SaveFile overworldBackup;

    public ShardRunManager(GameState gameState, ForgeLegacyEngine legacyEngine) {
        this.gameState = gameState;
        this.legacyEngine = legacyEngine;
    }

    /**
     * Starts a new Shard Run. Backs up the current overworld state and
     * initializes the run loadout.
     */
    public void startShardRun(String starterRobotId) {
        if (gameState.isShardRunActive()) {
            return;
        }

        // 1. Create backup of current overworld state
        overworldBackup = new SaveFile();
        captureOverworldState(overworldBackup);

        // 2. Clear current loadout and stats for the run
        gameState.setShardRunActive(true);
        gameState.setInfiniteDungeonCurrentFloor(1);
        gameState.setInfiniteDungeonRunActive(true);
        
        // Strip stats
        gameState.setPlayerHealth(100f);
        gameState.setPlayerMaxHealth(100f);
        gameState.setTotalGold(100L); // Starting gold for the run
        gameState.setHealingPotions(3); // Starting potions
        
        // Strip equipment
        gameState.setPlayerEquipment(new HashMap<>());
        gameState.setRobotEquipment(new HashMap<>());
        
        // Set starter robot
        List<String> starterRobots = new ArrayList<>();
        starterRobots.add(starterRobotId != null ? starterRobotId : "scout_mk1");
        gameState.setActiveRobotIds(starterRobots);
        
        // Reset floor flags
        gameState.setInfiniteDungeonFloorsCleared(0);
        
        gameState.incrementTotalShardRuns();
    }

    /**
     * Completes or fails a Shard Run. Calculates rewards and restores overworld state.
     */
    public void endShardRun(boolean success) {
        if (!gameState.isShardRunActive()) {
            return;
        }

        int floorsCleared = gameState.getInfiniteDungeonFloorsCleared();
        int floorReached = gameState.getInfiniteDungeonCurrentFloor();
        
        // 1. Calculate rewards
        long earnedShards = calculateShardReward(success, floorReached, floorsCleared);
        gameState.addForgeShards(earnedShards);
        
        // Update best floor if applicable
        if (floorReached > gameState.getShardRunBestFloor()) {
            gameState.setShardRunBestFloor(floorReached);
        }

        // 2. Restore overworld state from backup
        if (overworldBackup != null) {
            restoreOverworldState(overworldBackup);
        }

        gameState.setShardRunActive(false);
        gameState.setInfiniteDungeonRunActive(false);
        gameState.setCurrentZoneId("town");
    }

    private long calculateShardReward(boolean success, int floorReached, int floorsCleared) {
        long shards = 0;
        
        // Base reward for participation
        shards += 5;
        
        // Reward per floor cleared
        shards += (floorsCleared * legacyEngine.getFloorClearReward());
        
        // Bonus for reaching boss floors (every 10)
        int bossFloors = floorReached / 10;
        shards += (bossFloors * legacyEngine.getBossFloorReward());
        
        // Success bonus (if they exited via Seal Run)
        if (success) {
            shards += legacyEngine.getSealRunBonus();
        } else {
            // Death bonus (partial consolation based on depth)
            shards += legacyEngine.getDeathShardBonus(floorReached);
        }
        
        return shards;
    }

    private void captureOverworldState(SaveFile backup) {
        backup.setPlayerHp((int)gameState.getPlayerHealth());
        backup.setPlayerMaxHp((int)gameState.getPlayerMaxHealth());
        backup.setPlayerLevel(gameState.getPlayerLevel());
        backup.setPlayerExperience(gameState.getPlayerExperience());
        backup.setCurrencyBalance(gameState.getTotalGold());
        backup.setHealingPotions(gameState.getHealingPotions());
        backup.setPlayerEquipment(gameState.getPlayerEquipmentSlots());
        backup.setRobotEquipment(gameState.getRobotEquipment());
        backup.setActiveRobotIds(gameState.getActiveRobotIds());
        backup.setInfiniteDungeonCurrentFloor(gameState.getInfiniteDungeonCurrentFloor());
        backup.setInfiniteDungeonRunActive(gameState.isInfiniteDungeonRunActive());
        backup.setCurrentZoneId(gameState.getCurrentZoneId());
    }

    private void restoreOverworldState(SaveFile backup) {
        gameState.setPlayerHealth(backup.getPlayerHp());
        gameState.setPlayerMaxHealth(backup.getPlayerMaxHp());
        gameState.setPlayerLevel(backup.getPlayerLevel());
        gameState.setPlayerExperience(backup.getPlayerExperience());
        gameState.setTotalGold(backup.getCurrencyBalance());
        gameState.setHealingPotions(backup.getHealingPotions());
        gameState.setPlayerEquipment(backup.getPlayerEquipment());
        gameState.setRobotEquipment(backup.getRobotEquipment());
        gameState.setActiveRobotIds(backup.getActiveRobotIds());
        gameState.setInfiniteDungeonCurrentFloor(backup.getInfiniteDungeonCurrentFloor());
        gameState.setInfiniteDungeonRunActive(backup.isInfiniteDungeonRunActive());
        gameState.setCurrentZoneId(backup.getCurrentZoneId());
    }
}
