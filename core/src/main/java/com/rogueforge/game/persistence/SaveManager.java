package com.rogueforge.game.persistence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.data.SaveFile;
import com.rogueforge.game.engine.ServiceLifecycle;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages game save/load operations.
 * Handles multiple save slots plus autosave.
 */
public class SaveManager implements ServiceLifecycle {

    public static final int MAX_SLOTS = 3;
    public static final int AUTOSAVE_SLOT = 0;
    private static final String SAVE_FILE_PREFIX = "slot_";
    private static final String SAVE_FILE_SUFFIX = ".json";

    private Json json;

    public SaveManager() {
        this.json = new Json();
    }

    @Override
    public void initialize() {
        ensureSaveDirectory();
    }

    /**
     * Ensures the save directory exists.
     */
    private void ensureSaveDirectory() {
        FileHandle saveDir = Gdx.files.local(PersistencePaths.SAVE_DIR);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
    }

    /**
     * Saves the game data to the specified slot.
     *
     * @param data the save file data to persist
     * @param slot the save slot (0 = autosave, 1-3 = manual saves)
     */
    public void save(SaveFile data, int slot) {
        if (slot < 0 || slot > MAX_SLOTS) {
            throw new IllegalArgumentException("Invalid save slot: " + slot);
        }
        if (data != null) {
            upgradeLoadedSave(data);
            data.setVersion(SaveFile.CURRENT_VERSION);
        }

        FileHandle saveFile = getSaveFile(slot);
        String jsonData = json.prettyPrint(data);
        saveFile.writeString(jsonData, false);
    }

    /**
     * Loads save data from the specified slot.
     *
     * @param slot the save slot to load
     * @return the loaded SaveFile, or null if slot is empty
     */
    public SaveFile load(int slot) {
        validateSlot(slot);
        if (!hasSave(slot)) {
            return null;
        }

        FileHandle saveFile = getSaveFile(slot);
        String jsonData = saveFile.readString();
        SaveFile loaded = json.fromJson(SaveFile.class, jsonData);
        return upgradeLoadedSave(loaded);
    }

    static SaveFile upgradeLoadedSave(SaveFile saveFile) {
        if (saveFile == null) {
            return null;
        }

        int loadedVersion = Math.max(1, saveFile.getVersion());
        if (loadedVersion < 8) {
            if (saveFile.getPlayerWorldX() == null) {
                saveFile.setPlayerWorldX(saveFile.getPlayerX());
            }
            if (saveFile.getPlayerWorldY() == null) {
                saveFile.setPlayerWorldY(saveFile.getPlayerY());
            }
            if (saveFile.getFloatingOriginX() == null) {
                saveFile.setFloatingOriginX(0f);
            }
            if (saveFile.getFloatingOriginY() == null) {
                saveFile.setFloatingOriginY(0f);
            }
            if (saveFile.getSettlementTimeOfDayHours() == null) {
                saveFile.setSettlementTimeOfDayHours(12f);
            }
        }

        saveFile.setQuestStates(saveFile.getQuestStates());
        saveFile.setBestiaryScanLevels(saveFile.getBestiaryScanLevels());
        saveFile.setWorldStateFlags(saveFile.getWorldStateFlags());
        saveFile.setSettlementUpgrades(saveFile.getSettlementUpgrades());
        saveFile.setRobotProgressionStates(saveFile.getRobotProgressionStates());
        saveFile.setForgeComponents(saveFile.getForgeComponents());
        saveFile.setUnbankedForgeComponents(saveFile.getUnbankedForgeComponents());
        saveFile.setShardInventory(saveFile.getShardInventory());
        saveFile.setUnbankedShardInventory(saveFile.getUnbankedShardInventory());
        saveFile.setBlueprintFragments(saveFile.getBlueprintFragments());
        saveFile.setUnbankedBlueprintFragments(saveFile.getUnbankedBlueprintFragments());
        saveFile.setDefeatedBossIds(saveFile.getDefeatedBossIds());
        saveFile.setHarvestedFrontierFeatureIds(saveFile.getHarvestedFrontierFeatureIds());
        saveFile.setClaimedFrontierBaseSiteIds(saveFile.getClaimedFrontierBaseSiteIds());
        saveFile.setBaseStates(saveFile.getBaseStates());
        saveFile.setGuilds(saveFile.getGuilds());
        saveFile.setFactionInfluenceById(saveFile.getFactionInfluenceById());
        saveFile.setActiveWorldBossFrontsByZoneId(saveFile.getActiveWorldBossFrontsByZoneId());
        saveFile.setActiveRegionalIncidentsByZoneId(saveFile.getActiveRegionalIncidentsByZoneId());
        saveFile.setActiveSettlementCrisesByZoneId(saveFile.getActiveSettlementCrisesByZoneId());
        saveFile.setPlayerQuestContracts(saveFile.getPlayerQuestContracts());
        saveFile.setPlayerCreatedNpcs(saveFile.getPlayerCreatedNpcs());
        saveFile.setOwnedEquipmentIds(saveFile.getOwnedEquipmentIds());
        saveFile.setKeyItems(saveFile.getKeyItems());
        saveFile.setPlayerEquipment(saveFile.getPlayerEquipment());
        saveFile.setRobotEquipment(saveFile.getRobotEquipment());
        saveFile.setCollectedRobotIds(saveFile.getCollectedRobotIds());
        saveFile.setActiveRobotIds(saveFile.getActiveRobotIds());
        if (saveFile.getCurrentZoneId() == null || saveFile.getCurrentZoneId().isEmpty()) {
            saveFile.setCurrentZoneId("town");
        }
        saveFile.setVersion(SaveFile.CURRENT_VERSION);
        return saveFile;
    }

    /**
     * Automatically saves the game (slot 0).
     *
     * @param data the save file data to persist
     */
    public void autosave(SaveFile data) {
        save(data, AUTOSAVE_SLOT);
    }

    /**
     * Checks if a save exists in the given slot.
     *
     * @param slot the save slot to check
     * @return true if save file exists and is readable
     */
    public boolean hasSave(int slot) {
        validateSlot(slot);
        FileHandle saveFile = getSaveFile(slot);
        return saveFile.exists();
    }

    /**
     * Deletes the save file in the given slot.
     *
     * @param slot the save slot to delete
     */
    public void deleteSave(int slot) {
        validateSlot(slot);
        if (hasSave(slot)) {
            FileHandle saveFile = getSaveFile(slot);
            saveFile.delete();
        }
    }

    /**
     * Lists which save slots have saves.
     *
     * @return list of slot indices that contain saves
     */
    public List<Integer> listSaves() {
        List<Integer> availableSlots = new ArrayList<>();

        for (int i = 0; i <= MAX_SLOTS; i++) {
            if (hasSave(i)) {
                availableSlots.add(i);
            }
        }

        return availableSlots;
    }

    /**
     * Returns the most recently modified save slot, or -1 if no saves exist.
     */
    public int getLatestSaveSlot() {
        int latestSlot = -1;
        long latestModified = Long.MIN_VALUE;

        for (int slot = 0; slot <= MAX_SLOTS; slot++) {
            if (!hasSave(slot)) {
                continue;
            }

            long modified = getSaveFile(slot).lastModified();
            if (modified >= latestModified) {
                latestModified = modified;
                latestSlot = slot;
            }
        }

        return latestSlot;
    }

    /**
     * Loads the most recently modified save, or null if no saves exist.
     */
    public SaveFile loadLatestSave() {
        int latestSlot = getLatestSaveSlot();
        return latestSlot >= 0 ? load(latestSlot) : null;
    }

    /**
     * Gets the FileHandle for a specific save slot.
     *
     * @param slot the save slot
     * @return FileHandle for the save file
     */
    private FileHandle getSaveFile(int slot) {
        validateSlot(slot);
        String filename = PersistencePaths.SAVE_DIR + "/" + SAVE_FILE_PREFIX + slot + SAVE_FILE_SUFFIX;
        return Gdx.files.local(filename);
    }

    /**
     * Gets the display name for a save slot.
     *
     * @param slot the save slot
     * @return display name (e.g., "Autosave" or "Save Slot 1")
     */
    public String getSlotDisplayName(int slot) {
        validateSlot(slot);
        if (slot == AUTOSAVE_SLOT) {
            return "Autosave";
        }
        return "Save Slot " + slot;
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot > MAX_SLOTS) {
            throw new IllegalArgumentException("Invalid save slot: " + slot);
        }
    }
}
