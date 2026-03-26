package com.rogueforge.game.core;

import com.rogueforge.game.data.EquipmentItem;
import com.rogueforge.game.progression.RobotProgressionState;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTest {

    @Test
    void equipPlayerItemRequiresCatalogEntryAndGrade() {
        GameState gameState = new GameState("Tester");
        EquipmentItem sword = new EquipmentItem(
            "bronze_sword",
            "Bronze Sword",
            "weapon",
            0,
            4,
            0,
            0,
            0,
            100,
            1,
            "E",
            ""
        );
        gameState.addEquipmentToCatalog(sword);

        assertFalse(gameState.equipPlayerItem(sword));

        gameState.setPlayerLevel(25);

        assertTrue(gameState.equipPlayerItem(sword));
        assertEquals("bronze_sword", gameState.getPlayerEquipmentSlots().get("weapon"));
        assertTrue(gameState.getOwnedEquipmentIds().contains("bronze_sword"));
    }

    @Test
    void equipRobotItemRequiresGradeAndPersistsSlot() {
        GameState gameState = new GameState("Tester");
        EquipmentItem chassis = new EquipmentItem(
            "reinforced_plating",
            "Reinforced Plating",
            "armor",
            12,
            0,
            3,
            -1,
            0,
            180,
            1,
            "D",
            ""
        );
        gameState.addEquipmentToCatalog(chassis);

        assertFalse(gameState.equipRobotItem("rust_mk1", "E", chassis));
        assertTrue(gameState.equipRobotItem("rust_mk1", "C", chassis));
        assertEquals("reinforced_plating", gameState.getRobotEquipmentSlots("rust_mk1").get("armor"));
    }

    @Test
    void forgeComponentsAndShardsFilterInvalidValuesAndConsumeAtomically() {
        GameState gameState = new GameState("Tester");

        Map<String, Integer> initialComponents = new HashMap<>();
        initialComponents.put("bone_fiber", 3);
        initialComponents.put("bad_zero", 0);
        initialComponents.put("", 4);
        gameState.setForgeComponents(initialComponents);

        Map<String, Integer> initialShards = new HashMap<>();
        initialShards.put("C", 2);
        initialShards.put("G", -1);
        gameState.setShardInventory(initialShards);

        assertEquals(Map.of("bone_fiber", 3), gameState.getForgeComponents());
        assertEquals(Map.of("C", 2), gameState.getShardInventory());

        assertTrue(gameState.consumeForgeComponents(Map.of("bone_fiber", 2)));
        assertEquals(1, gameState.getForgeComponentCount("bone_fiber"));

        assertFalse(gameState.consumeForgeComponents(Map.of("bone_fiber", 2)));
        assertEquals(1, gameState.getForgeComponentCount("bone_fiber"));

        assertTrue(gameState.consumeShards("C", 1));
        assertEquals(1, gameState.getShardCount("C"));
        assertFalse(gameState.consumeShards("C", 2));
        assertEquals(1, gameState.getShardCount("C"));
    }

    @Test
    void rosterAndProgressionCollectionsReturnCopies() {
        GameState gameState = new GameState("Tester");
        RobotProgressionState progressionState = new RobotProgressionState("rust_mk1", "Rust");
        Map<String, RobotProgressionState> progressionStates = new HashMap<>();
        progressionStates.put("rust_mk1", progressionState);

        gameState.setCollectedRobotIds(Arrays.asList("rust_mk1", "", null, "ivy_mk1"));
        gameState.setActiveRobotIds(Arrays.asList("rust_mk1", null, "ivy_mk1"));
        gameState.setRobotProgressionStates(progressionStates);

        List<String> collected = gameState.getCollectedRobotIds();
        Map<String, RobotProgressionState> statesCopy = gameState.getRobotProgressionStates();

        assertEquals(List.of("rust_mk1", "ivy_mk1"), collected);
        assertEquals(Arrays.asList("rust_mk1", null, "ivy_mk1"), gameState.getActiveRobotIds());
        assertEquals(progressionState, statesCopy.get("rust_mk1"));
        assertNotSame(progressionStates, statesCopy);

        collected.clear();
        statesCopy.clear();

        assertEquals(2, gameState.getCollectedRobotIds().size());
        assertEquals(1, gameState.getRobotProgressionStates().size());
    }

    @Test
    void bossTrackingDeduplicatesAndCountsDefeats() {
        GameState gameState = new GameState("Tester");

        assertTrue(gameState.markBossDefeated("rusted_sovereign_c"));
        assertFalse(gameState.markBossDefeated("rusted_sovereign_c"));

        gameState.setDefeatedBossIds(Arrays.asList("volt_specter_b", "", null, "volt_specter_b", "origin_core_s"));

        assertTrue(gameState.hasDefeatedBoss("volt_specter_b"));
        assertEquals(2, gameState.getDefeatedBossCount());
    }
}
