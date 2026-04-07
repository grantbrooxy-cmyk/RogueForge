package com.rogueforge.game.persistence;

import com.rogueforge.game.data.SaveFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SaveManagerVersioningTest {

    @Test
    void upgradeLoadedSaveMigratesLegacyCoordinatesAndCollections() {
        SaveFile saveFile = new SaveFile();
        saveFile.setVersion(7);
        saveFile.setCurrentZoneId(null);
        saveFile.setPlayerX(144f);
        saveFile.setPlayerY(288f);
        saveFile.setPlayerWorldX(null);
        saveFile.setPlayerWorldY(null);
        saveFile.setFloatingOriginX(null);
        saveFile.setFloatingOriginY(null);
        saveFile.setSettlementTimeOfDayHours(null);
        saveFile.setPlayerQuestContracts(null);
        saveFile.setPlayerCreatedNpcs(null);

        SaveFile upgraded = SaveManager.upgradeLoadedSave(saveFile);

        assertEquals(SaveFile.CURRENT_VERSION, upgraded.getVersion());
        assertEquals("town", upgraded.getCurrentZoneId());
        assertEquals(144f, upgraded.getPlayerWorldX());
        assertEquals(288f, upgraded.getPlayerWorldY());
        assertEquals(0f, upgraded.getFloatingOriginX());
        assertEquals(0f, upgraded.getFloatingOriginY());
        assertEquals(12f, upgraded.getSettlementTimeOfDayHours());
        assertNotNull(upgraded.getPlayerQuestContracts());
        assertNotNull(upgraded.getPlayerCreatedNpcs());
    }
}
