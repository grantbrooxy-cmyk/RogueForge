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
        assertNotNull(saveFile.getQuestFlags());
        assertNotNull(saveFile.getQuestStates());
        assertNotNull(saveFile.getBestiaryScanLevels());
        assertNotNull(saveFile.getKeyItems());
        assertNotNull(saveFile.getWorldStateFlags());
        assertNotNull(saveFile.getSettlementUpgrades());
        assertNotNull(saveFile.getRobotProgressionStates());
        assertNotNull(saveFile.getForgeComponents());
        assertNotNull(saveFile.getShardInventory());
        assertNotNull(saveFile.getDefeatedBossIds());
        assertEquals(1, saveFile.getForgeCoreLevel());
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
        saveFile.setShardInventory(null);
        saveFile.setDefeatedBossIds(null);
        saveFile.setForgeCoreLevel(99);

        assertEquals(Map.of(), saveFile.getQuestStates());
        assertEquals(Map.of(), saveFile.getBestiaryScanLevels());
        assertEquals(Map.of(), saveFile.getWorldStateFlags());
        assertEquals(Map.of(), saveFile.getSettlementUpgrades());
        assertEquals(Map.of(), saveFile.getRobotProgressionStates());
        assertEquals(Map.of(), saveFile.getForgeComponents());
        assertEquals(Map.of(), saveFile.getShardInventory());
        assertEquals(List.of(), saveFile.getDefeatedBossIds());
        assertEquals(4, saveFile.getForgeCoreLevel());

        saveFile.setForgeCoreLevel(-3);
        assertEquals(1, saveFile.getForgeCoreLevel());
    }

    @Test
    void settersPersistProgressionAndInventoryPayloads() {
        SaveFile saveFile = new SaveFile();
        RobotProgressionState rust = new RobotProgressionState("rust_mk1", "Rust");
        SettlementState workshop = new SettlementState("workshop_tools", 2);

        saveFile.setOwnedEquipmentIds(List.of("bronze_sword"));
        saveFile.setKeyItems(List.of("workshop_pass"));
        saveFile.setSettlementUpgrades(Map.of("workshop_tools", workshop));
        saveFile.setRobotProgressionStates(Map.of("rust_mk1", rust));
        saveFile.setForgeComponents(Map.of("bone_fiber", 5));
        saveFile.setShardInventory(Map.of("C", 3));
        saveFile.setDefeatedBossIds(List.of("rusted_sovereign_c", "origin_core_s"));

        assertEquals(List.of("bronze_sword"), saveFile.getOwnedEquipmentIds());
        assertEquals(List.of("workshop_pass"), saveFile.getKeyItems());
        assertEquals(2, saveFile.getSettlementUpgrades().get("workshop_tools").getLevel());
        assertEquals("Rust", saveFile.getRobotProgressionStates().get("rust_mk1").getDisplayName());
        assertEquals(5, saveFile.getForgeComponents().get("bone_fiber"));
        assertEquals(3, saveFile.getShardInventory().get("C"));
        assertEquals(List.of("rusted_sovereign_c", "origin_core_s"), saveFile.getDefeatedBossIds());
    }
}
