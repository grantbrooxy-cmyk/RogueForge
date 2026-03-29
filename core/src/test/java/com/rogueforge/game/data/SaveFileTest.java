package com.rogueforge.game.data;

import com.rogueforge.game.progression.RobotProgressionState;
import com.rogueforge.game.world.SettlementState;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SaveFileTest {

    @Test
    void noArgConstructorInitializesCollections() {
        SaveFile saveFile = new SaveFile();

        assertNotNull(saveFile.getPlayerEquipment());
        assertNotNull(saveFile.getOwnedEquipmentIds());
        assertNotNull(saveFile.getQuestStates());
        assertNotNull(saveFile.getBestiaryScanLevels());
        assertNotNull(saveFile.getKeyItems());
        assertNotNull(saveFile.getWorldStateFlags());
        assertNotNull(saveFile.getSettlementUpgrades());
        assertNotNull(saveFile.getRobotProgressionStates());
        assertNotNull(saveFile.getForgeComponents());
        assertNotNull(saveFile.getUnbankedForgeComponents());
        assertNotNull(saveFile.getShardInventory());
        assertNotNull(saveFile.getUnbankedShardInventory());
        assertNotNull(saveFile.getDefeatedBossIds());
        assertNotNull(saveFile.getHarvestedFrontierFeatureIds());
        assertNotNull(saveFile.getClaimedFrontierBaseSiteIds());
        assertNotNull(saveFile.getBaseStates());
        assertEquals(0L, saveFile.getWorldSeed());
        assertEquals(1, saveFile.getForgeCoreLevel());
        assertEquals(0, saveFile.getInfiniteDungeonCurrentFloor());
        assertEquals(0, saveFile.getInfiniteDungeonBestFloor());
        assertEquals(0, saveFile.getInfiniteDungeonFloorsCleared());
    }

    @Test
    void settersNormalizeNullsAndClampForgeCoreLevel() {
        SaveFile saveFile = new SaveFile();

        saveFile.setQuestStates(null);
        saveFile.setBestiaryScanLevels(null);
        saveFile.setWorldStateFlags(null);
        saveFile.setSettlementUpgrades(null);
        saveFile.setRobotProgressionStates(null);
        saveFile.setForgeComponents(null);
        saveFile.setUnbankedForgeComponents(null);
        saveFile.setShardInventory(null);
        saveFile.setUnbankedShardInventory(null);
        saveFile.setDefeatedBossIds(null);
        saveFile.setHarvestedFrontierFeatureIds(null);
        saveFile.setClaimedFrontierBaseSiteIds(null);
        saveFile.setBaseStates(null);
        saveFile.setForgeCoreLevel(99);

        assertEquals(Map.of(), saveFile.getQuestStates());
        assertEquals(Map.of(), saveFile.getBestiaryScanLevels());
        assertEquals(Map.of(), saveFile.getWorldStateFlags());
        assertEquals(Map.of(), saveFile.getSettlementUpgrades());
        assertEquals(Map.of(), saveFile.getRobotProgressionStates());
        assertEquals(Map.of(), saveFile.getForgeComponents());
        assertEquals(Map.of(), saveFile.getUnbankedForgeComponents());
        assertEquals(Map.of(), saveFile.getShardInventory());
        assertEquals(Map.of(), saveFile.getUnbankedShardInventory());
        assertEquals(List.of(), saveFile.getDefeatedBossIds());
        assertEquals(List.of(), saveFile.getHarvestedFrontierFeatureIds());
        assertEquals(List.of(), saveFile.getClaimedFrontierBaseSiteIds());
        assertEquals(List.of(), saveFile.getBaseStates());
        assertEquals(4, saveFile.getForgeCoreLevel());

        saveFile.setForgeCoreLevel(-3);
        assertEquals(1, saveFile.getForgeCoreLevel());

        saveFile.setInfiniteDungeonCurrentFloor(-2);
        saveFile.setInfiniteDungeonBestFloor(-4);
        saveFile.setInfiniteDungeonFloorsCleared(-8);
        assertEquals(0, saveFile.getInfiniteDungeonCurrentFloor());
        assertEquals(0, saveFile.getInfiniteDungeonBestFloor());
        assertEquals(0, saveFile.getInfiniteDungeonFloorsCleared());
    }

    @Test
    void settersPersistProgressionAndInventoryPayloads() {
        SaveFile saveFile = new SaveFile();
        RobotProgressionState rust = new RobotProgressionState("rust_mk1", "Rust");
        SettlementState workshop = new SettlementState("workshop_tools", 2);
        SaveFile.PlacedStructureData structureData = new SaveFile.PlacedStructureData();
        structureData.setInstanceId("sentry_post_001");
        structureData.setStructureDefinitionId("sentry_post");
        structureData.setZoneId("verdant_fields");
        structureData.setClaimedSiteId("frontier_base_site_1");
        structureData.setX(720f);
        structureData.setY(720f);
        structureData.setWidth(96f);
        structureData.setHeight(96f);
        structureData.setCurrentHitPoints(180);
        SaveFile.DefenderAssignmentData assignmentData = new SaveFile.DefenderAssignmentData();
        assignmentData.setStructureInstanceId("sentry_post_001");
        assignmentData.setRobotId("scout_mk1");
        assignmentData.setRole("GUARD");
        SaveFile.BaseStateData baseStateData = new SaveFile.BaseStateData();
        baseStateData.setZoneId("verdant_fields");
        baseStateData.setClaimedSiteIds(List.of("frontier_base_site_1"));
        baseStateData.setPlacedStructures(List.of(structureData));
        baseStateData.setDefenderAssignments(List.of(assignmentData));
        baseStateData.setDefenderHealthByRobotId(Map.of("scout_mk1", 64f));
        baseStateData.setRaidActive(true);
        baseStateData.setRaidThreatLevel(0.8f);
        baseStateData.setRaidCooldownSeconds(32f);
        baseStateData.setRaidWaveIndex(2);

        saveFile.setOwnedEquipmentIds(List.of("bronze_sword"));
        saveFile.setKeyItems(List.of("workshop_pass"));
        saveFile.setSettlementUpgrades(Map.of("workshop_tools", workshop));
        saveFile.setRobotProgressionStates(Map.of("rust_mk1", rust));
        saveFile.setForgeComponents(Map.of("bone_fiber", 5));
        saveFile.setUnbankedForgeComponents(Map.of("scrap_alloy", 2));
        saveFile.setShardInventory(Map.of("C", 3));
        saveFile.setUnbankedShardInventory(Map.of("B", 1));
        saveFile.setDefeatedBossIds(List.of("rusted_sovereign_c", "origin_core_s"));
        saveFile.setHarvestedFrontierFeatureIds(List.of("scrap_alloy_node_2"));
        saveFile.setClaimedFrontierBaseSiteIds(List.of("frontier_base_site_1"));
        saveFile.setBaseStates(List.of(baseStateData));
        saveFile.setWorldSeed(987654321L);
        saveFile.setUnbankedCurrencyBalance(145L);
        saveFile.setInfiniteDungeonCurrentFloor(14);
        saveFile.setInfiniteDungeonBestFloor(22);
        saveFile.setInfiniteDungeonFloorsCleared(21);
        saveFile.setInfiniteDungeonRunActive(true);

        assertEquals(List.of("bronze_sword"), saveFile.getOwnedEquipmentIds());
        assertEquals(List.of("workshop_pass"), saveFile.getKeyItems());
        assertEquals(2, saveFile.getSettlementUpgrades().get("workshop_tools").getLevel());
        assertEquals("Rust", saveFile.getRobotProgressionStates().get("rust_mk1").getDisplayName());
        assertEquals(5, saveFile.getForgeComponents().get("bone_fiber"));
        assertEquals(2, saveFile.getUnbankedForgeComponents().get("scrap_alloy"));
        assertEquals(3, saveFile.getShardInventory().get("C"));
        assertEquals(1, saveFile.getUnbankedShardInventory().get("B"));
        assertEquals(List.of("rusted_sovereign_c", "origin_core_s"), saveFile.getDefeatedBossIds());
        assertEquals(List.of("scrap_alloy_node_2"), saveFile.getHarvestedFrontierFeatureIds());
        assertEquals(List.of("frontier_base_site_1"), saveFile.getClaimedFrontierBaseSiteIds());
        assertEquals("verdant_fields", saveFile.getBaseStates().get(0).getZoneId());
        assertEquals("sentry_post", saveFile.getBaseStates().get(0).getPlacedStructures().get(0).getStructureDefinitionId());
        assertEquals("GUARD", saveFile.getBaseStates().get(0).getDefenderAssignments().get(0).getRole());
        assertEquals(64f, saveFile.getBaseStates().get(0).getDefenderHealthByRobotId().get("scout_mk1"));
        assertEquals(true, saveFile.getBaseStates().get(0).isRaidActive());
        assertEquals(0.8f, saveFile.getBaseStates().get(0).getRaidThreatLevel());
        assertEquals(32f, saveFile.getBaseStates().get(0).getRaidCooldownSeconds());
        assertEquals(2, saveFile.getBaseStates().get(0).getRaidWaveIndex());
        assertEquals(987654321L, saveFile.getWorldSeed());
        assertEquals(145L, saveFile.getUnbankedCurrencyBalance());
        assertEquals(14, saveFile.getInfiniteDungeonCurrentFloor());
        assertEquals(22, saveFile.getInfiniteDungeonBestFloor());
        assertEquals(21, saveFile.getInfiniteDungeonFloorsCleared());
        assertEquals(true, saveFile.isInfiniteDungeonRunActive());
    }

}
