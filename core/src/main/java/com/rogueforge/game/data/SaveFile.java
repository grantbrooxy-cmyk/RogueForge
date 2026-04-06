package com.rogueforge.game.data;

import com.rogueforge.game.progression.RobotProgressionState;
import com.rogueforge.game.world.SettlementState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Full serializable game state.
 * Contains all player, robot, and progression data for a save game.
 */
public class SaveFile {
    private int version = 8;

    private String playerName;
    private int playerHp;
    private int playerMaxHp;
    private float playerX;
    private float playerY;
    private Float playerWorldX;
    private Float playerWorldY;
    private Float floatingOriginX;
    private Float floatingOriginY;
    private Float settlementTimeOfDayHours;
    private String currentZoneId;
    private long currencyBalance;
    private long worldSeed;
    private int healingPotions;
    private int playerLevel;
    private int playerExperience;
    private long unbankedCurrencyBalance;
    private Map<String, String> playerEquipment;
    private List<String> ownedEquipmentIds;
    private Map<String, String> questStates;
    private Map<String, Integer> bestiaryScanLevels;
    private List<String> keyItems;
    private Map<String, Boolean> worldStateFlags;
    private Map<String, SettlementState> settlementUpgrades;
    private Map<String, Integer> forgeComponents;
    private Map<String, Integer> unbankedForgeComponents;
    private Map<String, Integer> shardInventory;
    private Map<String, Integer> unbankedShardInventory;
    private Map<String, Integer> blueprintFragments;
    private Map<String, Integer> unbankedBlueprintFragments;
    private List<String> defeatedBossIds;
    private List<String> harvestedFrontierFeatureIds;
    private List<String> claimedFrontierBaseSiteIds;
    private List<BaseStateData> baseStates;
    private List<GuildData> guilds;
    private String activeClaimGuildId;
    private String pinnedExpeditionContractTitle;
    private String pinnedExpeditionContractText;
    private String pinnedExpeditionContractZoneId;
    private String pinnedExpeditionContractKind;
    private String pinnedExpeditionContractTargetId;
    private boolean pinnedExpeditionContractCompleted;
    private int expeditionBoardReputation;
    private Map<String, Integer> factionInfluenceById;
    private Map<String, String> activeWorldBossFrontsByZoneId;
    private Map<String, String> activeRegionalIncidentsByZoneId;
    private Map<String, String> activeSettlementCrisesByZoneId;
    private List<PlayerQuestContractData> playerQuestContracts;
    private List<PlayerCreatedNpcData> playerCreatedNpcs;
    private String expeditionBoardMode = "STANDARD";
    private String activeChallengeModifierId;
    private int hardModeSeedIndex;
    private int selectedChallengeModifierIndex;

    // Robot data: robotId -> Map of equipped items (slotType -> equipmentId)
    private Map<String, Map<String, String>> robotEquipment;

    // Collection and active roster
    private List<String> collectedRobotIds;
    private List<String> activeRobotIds;
    private float[] robotHealth;
    private float[] robotMaxHealth;
    private float[] robotBaseMaxHealth;
    private float[] robotX;
    private float[] robotY;
    private float[] robotAngleDeg;
    private float[] robotAttackTimers;
    private Map<String, RobotProgressionState> robotProgressionStates;

    // Current run state
    private int totalEnemiesKilled;
    private List<EnemyState> enemies;
    private List<ChestState> chests;

    // Forge Core progression
    private int forgeCoreLevel = 1;
    private int infiniteDungeonCurrentFloor = 0;
    private int infiniteDungeonBestFloor = 0;
    private int infiniteDungeonFloorsCleared = 0;
    private boolean infiniteDungeonRunActive = false;

    // Progress tracking
    private long playTimeSeconds;
    private String saveTimestamp;
    private int saveSlot;

    /**
     * No-arg constructor required for JSON deserialization.
     */
    public SaveFile() {
        this.robotEquipment = new HashMap<>();
        this.playerEquipment = new HashMap<>();
        this.ownedEquipmentIds = new ArrayList<>();
        this.questStates = new HashMap<>();
        this.bestiaryScanLevels = new HashMap<>();
        this.keyItems = new ArrayList<>();
        this.worldStateFlags = new HashMap<>();
        this.settlementUpgrades = new HashMap<>();
        this.robotProgressionStates = new HashMap<>();
        this.forgeComponents = new HashMap<>();
        this.unbankedForgeComponents = new HashMap<>();
        this.shardInventory = new HashMap<>();
        this.unbankedShardInventory = new HashMap<>();
        this.blueprintFragments = new HashMap<>();
        this.unbankedBlueprintFragments = new HashMap<>();
        this.defeatedBossIds = new ArrayList<>();
        this.harvestedFrontierFeatureIds = new ArrayList<>();
        this.claimedFrontierBaseSiteIds = new ArrayList<>();
        this.baseStates = new ArrayList<>();
        this.guilds = new ArrayList<>();
        this.factionInfluenceById = new HashMap<>();
        this.activeWorldBossFrontsByZoneId = new HashMap<>();
        this.activeRegionalIncidentsByZoneId = new HashMap<>();
        this.activeSettlementCrisesByZoneId = new HashMap<>();
        this.playerQuestContracts = new ArrayList<>();
        this.playerCreatedNpcs = new ArrayList<>();
    }

    public SaveFile(String playerName, int playerHp, int playerMaxHp, float playerX, float playerY,
                   String currentZoneId, long currencyBalance, long playTimeSeconds,
                   String saveTimestamp, int saveSlot) {
        this.playerName = playerName;
        this.playerHp = playerHp;
        this.playerMaxHp = playerMaxHp;
        this.playerX = playerX;
        this.playerY = playerY;
        this.currentZoneId = currentZoneId;
        this.currencyBalance = currencyBalance;
        this.playTimeSeconds = playTimeSeconds;
        this.saveTimestamp = saveTimestamp;
        this.saveSlot = saveSlot;
        this.robotEquipment = new HashMap<>();
        this.playerEquipment = new HashMap<>();
        this.ownedEquipmentIds = new ArrayList<>();
        this.questStates = new HashMap<>();
        this.bestiaryScanLevels = new HashMap<>();
        this.keyItems = new ArrayList<>();
        this.worldStateFlags = new HashMap<>();
        this.settlementUpgrades = new HashMap<>();
        this.robotProgressionStates = new HashMap<>();
        this.forgeComponents = new HashMap<>();
        this.unbankedForgeComponents = new HashMap<>();
        this.shardInventory = new HashMap<>();
        this.unbankedShardInventory = new HashMap<>();
        this.blueprintFragments = new HashMap<>();
        this.unbankedBlueprintFragments = new HashMap<>();
        this.defeatedBossIds = new ArrayList<>();
        this.harvestedFrontierFeatureIds = new ArrayList<>();
        this.claimedFrontierBaseSiteIds = new ArrayList<>();
        this.baseStates = new ArrayList<>();
        this.guilds = new ArrayList<>();
        this.factionInfluenceById = new HashMap<>();
        this.activeWorldBossFrontsByZoneId = new HashMap<>();
        this.activeRegionalIncidentsByZoneId = new HashMap<>();
        this.activeSettlementCrisesByZoneId = new HashMap<>();
        this.playerCreatedNpcs = new ArrayList<>();
    }

    // Getters
    public String getPlayerName() {
        return playerName;
    }

    public int getVersion() {
        return version;
    }

    public int getPlayerHp() {
        return playerHp;
    }

    public int getPlayerMaxHp() {
        return playerMaxHp;
    }

    public float getPlayerX() {
        return playerX;
    }

    public float getPlayerY() {
        return playerY;
    }

    public Float getPlayerWorldX() {
        return playerWorldX;
    }

    public Float getPlayerWorldY() {
        return playerWorldY;
    }

    public Float getFloatingOriginX() {
        return floatingOriginX;
    }

    public Float getFloatingOriginY() {
        return floatingOriginY;
    }

    public Float getSettlementTimeOfDayHours() {
        return settlementTimeOfDayHours;
    }

    public String getCurrentZoneId() {
        return currentZoneId;
    }

    public String getPinnedExpeditionContractTitle() {
        return pinnedExpeditionContractTitle;
    }

    public String getPinnedExpeditionContractText() {
        return pinnedExpeditionContractText;
    }

    public String getPinnedExpeditionContractZoneId() {
        return pinnedExpeditionContractZoneId;
    }

    public String getPinnedExpeditionContractKind() {
        return pinnedExpeditionContractKind;
    }

    public String getPinnedExpeditionContractTargetId() {
        return pinnedExpeditionContractTargetId;
    }

    public boolean isPinnedExpeditionContractCompleted() {
        return pinnedExpeditionContractCompleted;
    }

    public int getExpeditionBoardReputation() {
        return expeditionBoardReputation;
    }

    public Map<String, Integer> getFactionInfluenceById() {
        if (factionInfluenceById == null) {
            factionInfluenceById = new HashMap<>();
        }
        return factionInfluenceById;
    }

    public Map<String, String> getActiveWorldBossFrontsByZoneId() {
        if (activeWorldBossFrontsByZoneId == null) {
            activeWorldBossFrontsByZoneId = new HashMap<>();
        }
        return activeWorldBossFrontsByZoneId;
    }

    public Map<String, String> getActiveRegionalIncidentsByZoneId() {
        if (activeRegionalIncidentsByZoneId == null) {
            activeRegionalIncidentsByZoneId = new HashMap<>();
        }
        return activeRegionalIncidentsByZoneId;
    }

    public Map<String, String> getActiveSettlementCrisesByZoneId() {
        if (activeSettlementCrisesByZoneId == null) {
            activeSettlementCrisesByZoneId = new HashMap<>();
        }
        return activeSettlementCrisesByZoneId;
    }

    public List<PlayerQuestContractData> getPlayerQuestContracts() {
        if (playerQuestContracts == null) {
            playerQuestContracts = new ArrayList<>();
        }
        return playerQuestContracts;
    }

    public List<PlayerCreatedNpcData> getPlayerCreatedNpcs() {
        if (playerCreatedNpcs == null) {
            playerCreatedNpcs = new ArrayList<>();
        }
        return playerCreatedNpcs;
    }

    public String getExpeditionBoardMode() {
        return expeditionBoardMode != null && !expeditionBoardMode.isEmpty() ? expeditionBoardMode : "STANDARD";
    }

    public String getActiveChallengeModifierId() {
        return activeChallengeModifierId;
    }

    public int getHardModeSeedIndex() {
        return Math.max(0, hardModeSeedIndex);
    }

    public int getSelectedChallengeModifierIndex() {
        return Math.max(0, selectedChallengeModifierIndex);
    }

    public long getCurrencyBalance() {
        return currencyBalance;
    }

    public long getWorldSeed() {
        return worldSeed;
    }

    public int getHealingPotions() {
        return healingPotions;
    }

    public int getPlayerLevel() {
        return playerLevel;
    }

    public int getPlayerExperience() {
        return playerExperience;
    }

    public long getUnbankedCurrencyBalance() {
        return Math.max(0L, unbankedCurrencyBalance);
    }

    public Map<String, String> getPlayerEquipment() {
        return playerEquipment;
    }

    public List<String> getOwnedEquipmentIds() {
        return ownedEquipmentIds;
    }

    public Map<String, String> getQuestStates() {
        if (questStates == null) {
            questStates = new HashMap<>();
        }
        return questStates;
    }

    public Map<String, Integer> getBestiaryScanLevels() {
        if (bestiaryScanLevels == null) {
            bestiaryScanLevels = new HashMap<>();
        }
        return bestiaryScanLevels;
    }

    public List<String> getKeyItems() {
        return keyItems;
    }

    public Map<String, Boolean> getWorldStateFlags() {
        if (worldStateFlags == null) {
            worldStateFlags = new HashMap<>();
        }
        return worldStateFlags;
    }

    public Map<String, SettlementState> getSettlementUpgrades() {
        if (settlementUpgrades == null) {
            settlementUpgrades = new HashMap<>();
        }
        return settlementUpgrades;
    }

    public Map<String, Integer> getForgeComponents() {
        if (forgeComponents == null) {
            forgeComponents = new HashMap<>();
        }
        return forgeComponents;
    }

    public Map<String, Integer> getUnbankedForgeComponents() {
        if (unbankedForgeComponents == null) {
            unbankedForgeComponents = new HashMap<>();
        }
        return unbankedForgeComponents;
    }

    public Map<String, Integer> getShardInventory() {
        if (shardInventory == null) {
            shardInventory = new HashMap<>();
        }
        return shardInventory;
    }

    public Map<String, Integer> getUnbankedShardInventory() {
        if (unbankedShardInventory == null) {
            unbankedShardInventory = new HashMap<>();
        }
        return unbankedShardInventory;
    }

    public Map<String, Integer> getBlueprintFragments() {
        if (blueprintFragments == null) {
            blueprintFragments = new HashMap<>();
        }
        return blueprintFragments;
    }

    public Map<String, Integer> getUnbankedBlueprintFragments() {
        if (unbankedBlueprintFragments == null) {
            unbankedBlueprintFragments = new HashMap<>();
        }
        return unbankedBlueprintFragments;
    }

    public List<String> getDefeatedBossIds() {
        if (defeatedBossIds == null) {
            defeatedBossIds = new ArrayList<>();
        }
        return defeatedBossIds;
    }

    public Map<String, Map<String, String>> getRobotEquipment() {
        return robotEquipment;
    }

    public List<String> getHarvestedFrontierFeatureIds() {
        if (harvestedFrontierFeatureIds == null) {
            harvestedFrontierFeatureIds = new ArrayList<>();
        }
        return harvestedFrontierFeatureIds;
    }

    public List<String> getClaimedFrontierBaseSiteIds() {
        if (claimedFrontierBaseSiteIds == null) {
            claimedFrontierBaseSiteIds = new ArrayList<>();
        }
        return claimedFrontierBaseSiteIds;
    }

    public List<BaseStateData> getBaseStates() {
        if (baseStates == null) {
            baseStates = new ArrayList<>();
        }
        return baseStates;
    }

    public List<GuildData> getGuilds() {
        if (guilds == null) {
            guilds = new ArrayList<>();
        }
        return guilds;
    }

    public String getActiveClaimGuildId() {
        return activeClaimGuildId;
    }

    public List<String> getCollectedRobotIds() {
        return collectedRobotIds;
    }

    public List<String> getActiveRobotIds() {
        return activeRobotIds;
    }

    public float[] getRobotHealth() {
        return robotHealth;
    }

    public float[] getRobotMaxHealth() {
        return robotMaxHealth;
    }

    public float[] getRobotBaseMaxHealth() {
        return robotBaseMaxHealth;
    }

    public float[] getRobotX() {
        return robotX;
    }

    public float[] getRobotY() {
        return robotY;
    }

    public float[] getRobotAngleDeg() {
        return robotAngleDeg;
    }

    public float[] getRobotAttackTimers() {
        return robotAttackTimers;
    }

    public Map<String, RobotProgressionState> getRobotProgressionStates() {
        if (robotProgressionStates == null) {
            robotProgressionStates = new HashMap<>();
        }
        return robotProgressionStates;
    }

    public int getTotalEnemiesKilled() {
        return totalEnemiesKilled;
    }

    public List<EnemyState> getEnemies() {
        return enemies;
    }

    public List<ChestState> getChests() {
        return chests;
    }

    public long getPlayTimeSeconds() {
        return playTimeSeconds;
    }

    public String getSaveTimestamp() {
        return saveTimestamp;
    }

    public int getSaveSlot() {
        return saveSlot;
    }

    // Setters
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setPlayerHp(int playerHp) {
        this.playerHp = playerHp;
    }

    public void setPlayerMaxHp(int playerMaxHp) {
        this.playerMaxHp = playerMaxHp;
    }

    public void setPlayerX(float playerX) {
        this.playerX = playerX;
    }

    public void setPlayerY(float playerY) {
        this.playerY = playerY;
    }

    public void setPlayerWorldX(Float playerWorldX) {
        this.playerWorldX = playerWorldX;
    }

    public void setPlayerWorldY(Float playerWorldY) {
        this.playerWorldY = playerWorldY;
    }

    public void setFloatingOriginX(Float floatingOriginX) {
        this.floatingOriginX = floatingOriginX;
    }

    public void setFloatingOriginY(Float floatingOriginY) {
        this.floatingOriginY = floatingOriginY;
    }

    public void setSettlementTimeOfDayHours(Float settlementTimeOfDayHours) {
        this.settlementTimeOfDayHours = settlementTimeOfDayHours;
    }

    public void setCurrentZoneId(String currentZoneId) {
        this.currentZoneId = currentZoneId;
    }

    public void setPinnedExpeditionContractTitle(String pinnedExpeditionContractTitle) {
        this.pinnedExpeditionContractTitle = pinnedExpeditionContractTitle;
    }

    public void setPinnedExpeditionContractText(String pinnedExpeditionContractText) {
        this.pinnedExpeditionContractText = pinnedExpeditionContractText;
    }

    public void setPinnedExpeditionContractZoneId(String pinnedExpeditionContractZoneId) {
        this.pinnedExpeditionContractZoneId = pinnedExpeditionContractZoneId;
    }

    public void setPinnedExpeditionContractKind(String pinnedExpeditionContractKind) {
        this.pinnedExpeditionContractKind = pinnedExpeditionContractKind;
    }

    public void setPinnedExpeditionContractTargetId(String pinnedExpeditionContractTargetId) {
        this.pinnedExpeditionContractTargetId = pinnedExpeditionContractTargetId;
    }

    public void setPinnedExpeditionContractCompleted(boolean pinnedExpeditionContractCompleted) {
        this.pinnedExpeditionContractCompleted = pinnedExpeditionContractCompleted;
    }

    public void setExpeditionBoardReputation(int expeditionBoardReputation) {
        this.expeditionBoardReputation = Math.max(0, expeditionBoardReputation);
    }

    public void setFactionInfluenceById(Map<String, Integer> factionInfluenceById) {
        this.factionInfluenceById = factionInfluenceById != null ? factionInfluenceById : new HashMap<>();
    }

    public void setActiveWorldBossFrontsByZoneId(Map<String, String> activeWorldBossFrontsByZoneId) {
        this.activeWorldBossFrontsByZoneId = activeWorldBossFrontsByZoneId != null ? activeWorldBossFrontsByZoneId : new HashMap<>();
    }

    public void setActiveRegionalIncidentsByZoneId(Map<String, String> activeRegionalIncidentsByZoneId) {
        this.activeRegionalIncidentsByZoneId = activeRegionalIncidentsByZoneId != null ? activeRegionalIncidentsByZoneId : new HashMap<>();
    }

    public void setActiveSettlementCrisesByZoneId(Map<String, String> activeSettlementCrisesByZoneId) {
        this.activeSettlementCrisesByZoneId = activeSettlementCrisesByZoneId != null ? activeSettlementCrisesByZoneId : new HashMap<>();
    }

    public void setPlayerQuestContracts(List<PlayerQuestContractData> playerQuestContracts) {
        this.playerQuestContracts = playerQuestContracts != null ? playerQuestContracts : new ArrayList<>();
    }

    public void setPlayerCreatedNpcs(List<PlayerCreatedNpcData> playerCreatedNpcs) {
        this.playerCreatedNpcs = playerCreatedNpcs != null ? playerCreatedNpcs : new ArrayList<>();
    }

    public void setExpeditionBoardMode(String expeditionBoardMode) {
        this.expeditionBoardMode = expeditionBoardMode != null && !expeditionBoardMode.isEmpty() ? expeditionBoardMode : "STANDARD";
    }

    public void setActiveChallengeModifierId(String activeChallengeModifierId) {
        this.activeChallengeModifierId = activeChallengeModifierId;
    }

    public void setHardModeSeedIndex(int hardModeSeedIndex) {
        this.hardModeSeedIndex = Math.max(0, hardModeSeedIndex);
    }

    public void setSelectedChallengeModifierIndex(int selectedChallengeModifierIndex) {
        this.selectedChallengeModifierIndex = Math.max(0, selectedChallengeModifierIndex);
    }

    public void setCurrencyBalance(long currencyBalance) {
        this.currencyBalance = currencyBalance;
    }

    public void setWorldSeed(long worldSeed) {
        this.worldSeed = worldSeed;
    }

    public void setHealingPotions(int healingPotions) {
        this.healingPotions = healingPotions;
    }

    public void setPlayerLevel(int playerLevel) {
        this.playerLevel = playerLevel;
    }

    public void setPlayerExperience(int playerExperience) {
        this.playerExperience = playerExperience;
    }

    public void setUnbankedCurrencyBalance(long unbankedCurrencyBalance) {
        this.unbankedCurrencyBalance = Math.max(0L, unbankedCurrencyBalance);
    }

    public void setPlayerEquipment(Map<String, String> playerEquipment) {
        this.playerEquipment = playerEquipment;
    }

    public void setOwnedEquipmentIds(List<String> ownedEquipmentIds) {
        this.ownedEquipmentIds = ownedEquipmentIds;
    }

    public void setQuestStates(Map<String, String> questStates) {
        this.questStates = questStates != null ? questStates : new HashMap<>();
    }

    public void setBestiaryScanLevels(Map<String, Integer> bestiaryScanLevels) {
        this.bestiaryScanLevels = bestiaryScanLevels != null ? bestiaryScanLevels : new HashMap<>();
    }

    public void setKeyItems(List<String> keyItems) {
        this.keyItems = keyItems;
    }

    public void setWorldStateFlags(Map<String, Boolean> worldStateFlags) {
        this.worldStateFlags = worldStateFlags != null ? worldStateFlags : new HashMap<>();
    }

    public void setSettlementUpgrades(Map<String, SettlementState> settlementUpgrades) {
        this.settlementUpgrades = settlementUpgrades != null ? settlementUpgrades : new HashMap<>();
    }

    public void setForgeComponents(Map<String, Integer> forgeComponents) {
        this.forgeComponents = forgeComponents != null ? forgeComponents : new HashMap<>();
    }

    public void setUnbankedForgeComponents(Map<String, Integer> unbankedForgeComponents) {
        this.unbankedForgeComponents = unbankedForgeComponents != null ? unbankedForgeComponents : new HashMap<>();
    }

    public void setShardInventory(Map<String, Integer> shardInventory) {
        this.shardInventory = shardInventory != null ? shardInventory : new HashMap<>();
    }

    public void setUnbankedShardInventory(Map<String, Integer> unbankedShardInventory) {
        this.unbankedShardInventory = unbankedShardInventory != null ? unbankedShardInventory : new HashMap<>();
    }

    public void setBlueprintFragments(Map<String, Integer> blueprintFragments) {
        this.blueprintFragments = blueprintFragments != null ? blueprintFragments : new HashMap<>();
    }

    public void setUnbankedBlueprintFragments(Map<String, Integer> unbankedBlueprintFragments) {
        this.unbankedBlueprintFragments = unbankedBlueprintFragments != null ? unbankedBlueprintFragments : new HashMap<>();
    }

    public void setDefeatedBossIds(List<String> defeatedBossIds) {
        this.defeatedBossIds = defeatedBossIds != null ? defeatedBossIds : new ArrayList<>();
    }

    public void setHarvestedFrontierFeatureIds(List<String> harvestedFrontierFeatureIds) {
        this.harvestedFrontierFeatureIds = harvestedFrontierFeatureIds != null ? harvestedFrontierFeatureIds : new ArrayList<>();
    }

    public void setClaimedFrontierBaseSiteIds(List<String> claimedFrontierBaseSiteIds) {
        this.claimedFrontierBaseSiteIds = claimedFrontierBaseSiteIds != null ? claimedFrontierBaseSiteIds : new ArrayList<>();
    }

    public void setBaseStates(List<BaseStateData> baseStates) {
        this.baseStates = baseStates != null ? baseStates : new ArrayList<>();
    }

    public void setGuilds(List<GuildData> guilds) {
        this.guilds = guilds != null ? guilds : new ArrayList<>();
    }

    public void setActiveClaimGuildId(String activeClaimGuildId) {
        this.activeClaimGuildId = activeClaimGuildId;
    }

    public void setRobotEquipment(Map<String, Map<String, String>> robotEquipment) {
        this.robotEquipment = robotEquipment;
    }

    public void setCollectedRobotIds(List<String> collectedRobotIds) {
        this.collectedRobotIds = collectedRobotIds;
    }

    public void setActiveRobotIds(List<String> activeRobotIds) {
        this.activeRobotIds = activeRobotIds;
    }

    public void setRobotHealth(float[] robotHealth) {
        this.robotHealth = robotHealth;
    }

    public void setRobotMaxHealth(float[] robotMaxHealth) {
        this.robotMaxHealth = robotMaxHealth;
    }

    public void setRobotBaseMaxHealth(float[] robotBaseMaxHealth) {
        this.robotBaseMaxHealth = robotBaseMaxHealth;
    }

    public void setRobotX(float[] robotX) {
        this.robotX = robotX;
    }

    public void setRobotY(float[] robotY) {
        this.robotY = robotY;
    }

    public void setRobotAngleDeg(float[] robotAngleDeg) {
        this.robotAngleDeg = robotAngleDeg;
    }

    public void setRobotAttackTimers(float[] robotAttackTimers) {
        this.robotAttackTimers = robotAttackTimers;
    }

    public void setRobotProgressionStates(Map<String, RobotProgressionState> robotProgressionStates) {
        this.robotProgressionStates = robotProgressionStates != null ? robotProgressionStates : new HashMap<>();
    }

    public void setTotalEnemiesKilled(int totalEnemiesKilled) {
        this.totalEnemiesKilled = totalEnemiesKilled;
    }

    public void setEnemies(List<EnemyState> enemies) {
        this.enemies = enemies;
    }

    public void setChests(List<ChestState> chests) {
        this.chests = chests;
    }

    public void setPlayTimeSeconds(long playTimeSeconds) {
        this.playTimeSeconds = playTimeSeconds;
    }

    public void setSaveTimestamp(String saveTimestamp) {
        this.saveTimestamp = saveTimestamp;
    }

    public void setSaveSlot(int saveSlot) {
        this.saveSlot = saveSlot;
    }

    public int getForgeCoreLevel() {
        return forgeCoreLevel;
    }

    public void setForgeCoreLevel(int forgeCoreLevel) {
        this.forgeCoreLevel = Math.max(1, Math.min(4, forgeCoreLevel));
    }

    public int getInfiniteDungeonCurrentFloor() {
        return Math.max(0, infiniteDungeonCurrentFloor);
    }

    public void setInfiniteDungeonCurrentFloor(int infiniteDungeonCurrentFloor) {
        this.infiniteDungeonCurrentFloor = Math.max(0, infiniteDungeonCurrentFloor);
    }

    public int getInfiniteDungeonBestFloor() {
        return Math.max(0, infiniteDungeonBestFloor);
    }

    public void setInfiniteDungeonBestFloor(int infiniteDungeonBestFloor) {
        this.infiniteDungeonBestFloor = Math.max(0, infiniteDungeonBestFloor);
    }

    public int getInfiniteDungeonFloorsCleared() {
        return Math.max(0, infiniteDungeonFloorsCleared);
    }

    public void setInfiniteDungeonFloorsCleared(int infiniteDungeonFloorsCleared) {
        this.infiniteDungeonFloorsCleared = Math.max(0, infiniteDungeonFloorsCleared);
    }

    public boolean isInfiniteDungeonRunActive() {
        return infiniteDungeonRunActive;
    }

    public void setInfiniteDungeonRunActive(boolean infiniteDungeonRunActive) {
        this.infiniteDungeonRunActive = infiniteDungeonRunActive;
    }

    /**
     * Serializable live enemy state for restoring an in-progress run.
     */
    public static class EnemyState {
        private String monsterId;
        private float x;
        private float y;
        private float hp;
        private float maxHp;
        private float speed;
        private float size;
        private float defense;
        private float agility;
        private float strength;
        private float intelligence;
        private float stamina;
        private int rewardGold;
        private int rewardExperience;
        private String name;
        private boolean alive;
        private float attackTimer;
        private float patrolTargetX;
        private float patrolTargetY;
        private int dungeonFloor;
        private boolean raidSpawned;

        public EnemyState() {
        }

        public String getMonsterId() {
            return monsterId;
        }

        public void setMonsterId(String monsterId) {
            this.monsterId = monsterId;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getHp() {
            return hp;
        }

        public void setHp(float hp) {
            this.hp = hp;
        }

        public float getMaxHp() {
            return maxHp;
        }

        public void setMaxHp(float maxHp) {
            this.maxHp = maxHp;
        }

        public float getSpeed() {
            return speed;
        }

        public void setSpeed(float speed) {
            this.speed = speed;
        }

        public float getSize() {
            return size;
        }

        public void setSize(float size) {
            this.size = size;
        }

        public float getDefense() {
            return defense;
        }

        public void setDefense(float defense) {
            this.defense = defense;
        }

        public float getAgility() {
            return agility;
        }

        public void setAgility(float agility) {
            this.agility = agility;
        }

        public float getStrength() {
            return strength;
        }

        public void setStrength(float strength) {
            this.strength = strength;
        }

        public float getIntelligence() {
            return intelligence;
        }

        public void setIntelligence(float intelligence) {
            this.intelligence = intelligence;
        }

        public float getStamina() {
            return stamina;
        }

        public void setStamina(float stamina) {
            this.stamina = stamina;
        }

        public int getRewardGold() {
            return rewardGold;
        }

        public void setRewardGold(int rewardGold) {
            this.rewardGold = rewardGold;
        }

        public int getRewardExperience() {
            return rewardExperience;
        }

        public void setRewardExperience(int rewardExperience) {
            this.rewardExperience = rewardExperience;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isAlive() {
            return alive;
        }

        public void setAlive(boolean alive) {
            this.alive = alive;
        }

        public float getAttackTimer() {
            return attackTimer;
        }

        public void setAttackTimer(float attackTimer) {
            this.attackTimer = attackTimer;
        }

        public float getPatrolTargetX() {
            return patrolTargetX;
        }

        public void setPatrolTargetX(float patrolTargetX) {
            this.patrolTargetX = patrolTargetX;
        }

        public float getPatrolTargetY() {
            return patrolTargetY;
        }

        public void setPatrolTargetY(float patrolTargetY) {
            this.patrolTargetY = patrolTargetY;
        }

        public int getDungeonFloor() {
            return dungeonFloor;
        }

        public void setDungeonFloor(int dungeonFloor) {
            this.dungeonFloor = Math.max(0, dungeonFloor);
        }

        public boolean isRaidSpawned() {
            return raidSpawned;
        }

        public void setRaidSpawned(boolean raidSpawned) {
            this.raidSpawned = raidSpawned;
        }
    }

    /**
     * Serializable chest state for house interiors.
     */
    public static class ChestState {
        private String zoneId;
        private int houseId;
        private String chestId;
        private boolean opened;

        public ChestState() {
        }

        public String getZoneId() {
            return zoneId;
        }

        public void setZoneId(String zoneId) {
            this.zoneId = zoneId;
        }

        public int getHouseId() {
            return houseId;
        }

        public void setHouseId(int houseId) {
            this.houseId = houseId;
        }

        public String getChestId() {
            return chestId;
        }

        public void setChestId(String chestId) {
            this.chestId = chestId;
        }

        public boolean isOpened() {
            return opened;
        }

        public void setOpened(boolean opened) {
            this.opened = opened;
        }
    }

    public static class BaseStateData {
        private String zoneId;
        private List<String> claimedSiteIds;
        private Map<String, OwnershipRecordData> claimedSiteOwnershipById;
        private List<PlacedStructureData> placedStructures;
        private Map<String, OwnershipRecordData> structureOwnershipByInstanceId;
        private List<DefenderAssignmentData> defenderAssignments;
        private Map<String, Float> defenderHealthByRobotId;
        private boolean raidActive;
        private float raidThreatLevel;
        private float raidCooldownSeconds;
        private int raidWaveIndex;

        public BaseStateData() {
            this.claimedSiteIds = new ArrayList<>();
            this.claimedSiteOwnershipById = new HashMap<>();
            this.placedStructures = new ArrayList<>();
            this.structureOwnershipByInstanceId = new HashMap<>();
            this.defenderAssignments = new ArrayList<>();
            this.defenderHealthByRobotId = new HashMap<>();
        }

        public String getZoneId() {
            return zoneId;
        }

        public void setZoneId(String zoneId) {
            this.zoneId = zoneId;
        }

        public List<String> getClaimedSiteIds() {
            if (claimedSiteIds == null) {
                claimedSiteIds = new ArrayList<>();
            }
            return claimedSiteIds;
        }

        public void setClaimedSiteIds(List<String> claimedSiteIds) {
            this.claimedSiteIds = claimedSiteIds != null ? claimedSiteIds : new ArrayList<>();
        }

        public Map<String, OwnershipRecordData> getClaimedSiteOwnershipById() {
            if (claimedSiteOwnershipById == null) {
                claimedSiteOwnershipById = new HashMap<>();
            }
            return claimedSiteOwnershipById;
        }

        public void setClaimedSiteOwnershipById(Map<String, OwnershipRecordData> claimedSiteOwnershipById) {
            this.claimedSiteOwnershipById = claimedSiteOwnershipById != null ? claimedSiteOwnershipById : new HashMap<>();
        }

        public List<PlacedStructureData> getPlacedStructures() {
            if (placedStructures == null) {
                placedStructures = new ArrayList<>();
            }
            return placedStructures;
        }

        public void setPlacedStructures(List<PlacedStructureData> placedStructures) {
            this.placedStructures = placedStructures != null ? placedStructures : new ArrayList<>();
        }

        public Map<String, OwnershipRecordData> getStructureOwnershipByInstanceId() {
            if (structureOwnershipByInstanceId == null) {
                structureOwnershipByInstanceId = new HashMap<>();
            }
            return structureOwnershipByInstanceId;
        }

        public void setStructureOwnershipByInstanceId(Map<String, OwnershipRecordData> structureOwnershipByInstanceId) {
            this.structureOwnershipByInstanceId = structureOwnershipByInstanceId != null ? structureOwnershipByInstanceId : new HashMap<>();
        }

        public List<DefenderAssignmentData> getDefenderAssignments() {
            if (defenderAssignments == null) {
                defenderAssignments = new ArrayList<>();
            }
            return defenderAssignments;
        }

        public void setDefenderAssignments(List<DefenderAssignmentData> defenderAssignments) {
            this.defenderAssignments = defenderAssignments != null ? defenderAssignments : new ArrayList<>();
        }

        public Map<String, Float> getDefenderHealthByRobotId() {
            if (defenderHealthByRobotId == null) {
                defenderHealthByRobotId = new HashMap<>();
            }
            return defenderHealthByRobotId;
        }

        public void setDefenderHealthByRobotId(Map<String, Float> defenderHealthByRobotId) {
            this.defenderHealthByRobotId = defenderHealthByRobotId != null ? defenderHealthByRobotId : new HashMap<>();
        }

        public boolean isRaidActive() {
            return raidActive;
        }

        public void setRaidActive(boolean raidActive) {
            this.raidActive = raidActive;
        }

        public float getRaidThreatLevel() {
            return raidThreatLevel;
        }

        public void setRaidThreatLevel(float raidThreatLevel) {
            this.raidThreatLevel = raidThreatLevel;
        }

        public float getRaidCooldownSeconds() {
            return raidCooldownSeconds;
        }

        public void setRaidCooldownSeconds(float raidCooldownSeconds) {
            this.raidCooldownSeconds = raidCooldownSeconds;
        }

        public int getRaidWaveIndex() {
            return raidWaveIndex;
        }

        public void setRaidWaveIndex(int raidWaveIndex) {
            this.raidWaveIndex = raidWaveIndex;
        }
    }

    public static class PlacedStructureData {
        private String instanceId;
        private String structureDefinitionId;
        private String zoneId;
        private String claimedSiteId;
        private float x;
        private float y;
        private float width;
        private float height;
        private int currentHitPoints;
        private boolean active = true;

        public String getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
        }

        public String getStructureDefinitionId() {
            return structureDefinitionId;
        }

        public void setStructureDefinitionId(String structureDefinitionId) {
            this.structureDefinitionId = structureDefinitionId;
        }

        public String getZoneId() {
            return zoneId;
        }

        public void setZoneId(String zoneId) {
            this.zoneId = zoneId;
        }

        public String getClaimedSiteId() {
            return claimedSiteId;
        }

        public void setClaimedSiteId(String claimedSiteId) {
            this.claimedSiteId = claimedSiteId;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public int getCurrentHitPoints() {
            return currentHitPoints;
        }

        public void setCurrentHitPoints(int currentHitPoints) {
            this.currentHitPoints = currentHitPoints;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    public static class OwnershipRecordData {
        private String scope;
        private String ownerPlayerId;
        private String ownerGuildId;
        private String settlementId;
        private boolean publicInteractionAllowed;
        private List<String> editorPlayerIds;

        public OwnershipRecordData() {
            this.editorPlayerIds = new ArrayList<>();
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getOwnerPlayerId() {
            return ownerPlayerId;
        }

        public void setOwnerPlayerId(String ownerPlayerId) {
            this.ownerPlayerId = ownerPlayerId;
        }

        public String getOwnerGuildId() {
            return ownerGuildId;
        }

        public void setOwnerGuildId(String ownerGuildId) {
            this.ownerGuildId = ownerGuildId;
        }

        public String getSettlementId() {
            return settlementId;
        }

        public void setSettlementId(String settlementId) {
            this.settlementId = settlementId;
        }

        public boolean isPublicInteractionAllowed() {
            return publicInteractionAllowed;
        }

        public void setPublicInteractionAllowed(boolean publicInteractionAllowed) {
            this.publicInteractionAllowed = publicInteractionAllowed;
        }

        public List<String> getEditorPlayerIds() {
            if (editorPlayerIds == null) {
                editorPlayerIds = new ArrayList<>();
            }
            return editorPlayerIds;
        }

        public void setEditorPlayerIds(List<String> editorPlayerIds) {
            this.editorPlayerIds = editorPlayerIds != null ? editorPlayerIds : new ArrayList<>();
        }
    }

    public static class GuildData {
        private String guildId;
        private String displayName;
        private String founderPlayerId;
        private boolean recruitingOpen;
        private String hallZoneId;
        private String hallClaimedSiteId;
        private List<GuildRankData> ranks;
        private List<GuildMembershipData> memberships;

        public GuildData() {
            this.ranks = new ArrayList<>();
            this.memberships = new ArrayList<>();
        }

        public String getGuildId() {
            return guildId;
        }

        public void setGuildId(String guildId) {
            this.guildId = guildId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getFounderPlayerId() {
            return founderPlayerId;
        }

        public void setFounderPlayerId(String founderPlayerId) {
            this.founderPlayerId = founderPlayerId;
        }

        public boolean isRecruitingOpen() {
            return recruitingOpen;
        }

        public void setRecruitingOpen(boolean recruitingOpen) {
            this.recruitingOpen = recruitingOpen;
        }

        public String getHallZoneId() {
            return hallZoneId;
        }

        public void setHallZoneId(String hallZoneId) {
            this.hallZoneId = hallZoneId;
        }

        public String getHallClaimedSiteId() {
            return hallClaimedSiteId;
        }

        public void setHallClaimedSiteId(String hallClaimedSiteId) {
            this.hallClaimedSiteId = hallClaimedSiteId;
        }

        public List<GuildRankData> getRanks() {
            if (ranks == null) {
                ranks = new ArrayList<>();
            }
            return ranks;
        }

        public void setRanks(List<GuildRankData> ranks) {
            this.ranks = ranks != null ? ranks : new ArrayList<>();
        }

        public List<GuildMembershipData> getMemberships() {
            if (memberships == null) {
                memberships = new ArrayList<>();
            }
            return memberships;
        }

        public void setMemberships(List<GuildMembershipData> memberships) {
            this.memberships = memberships != null ? memberships : new ArrayList<>();
        }
    }

    public static class PlayerQuestContractData {
        private String contractId;
        private String guildId;
        private String zoneId;
        private String kind;
        private String title;
        private String description;
        private String targetId;
        private String authorPlayerId;
        private boolean active = true;

        public String getContractId() {
            return contractId;
        }

        public void setContractId(String contractId) {
            this.contractId = contractId;
        }

        public String getGuildId() {
            return guildId;
        }

        public void setGuildId(String guildId) {
            this.guildId = guildId;
        }

        public String getZoneId() {
            return zoneId;
        }

        public void setZoneId(String zoneId) {
            this.zoneId = zoneId;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTargetId() {
            return targetId;
        }

        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }

        public String getAuthorPlayerId() {
            return authorPlayerId;
        }

        public void setAuthorPlayerId(String authorPlayerId) {
            this.authorPlayerId = authorPlayerId;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    public static class PlayerCreatedNpcData {
        private String npcId;
        private String guildId;
        private String zoneId;
        private String role;
        private String name;
        private String dialog;

        public String getNpcId() {
            return npcId;
        }

        public void setNpcId(String npcId) {
            this.npcId = npcId;
        }

        public String getGuildId() {
            return guildId;
        }

        public void setGuildId(String guildId) {
            this.guildId = guildId;
        }

        public String getZoneId() {
            return zoneId;
        }

        public void setZoneId(String zoneId) {
            this.zoneId = zoneId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDialog() {
            return dialog;
        }

        public void setDialog(String dialog) {
            this.dialog = dialog;
        }
    }

    public static class GuildRankData {
        private String rankId;
        private String displayName;
        private List<String> allowedActions;

        public GuildRankData() {
            this.allowedActions = new ArrayList<>();
        }

        public String getRankId() {
            return rankId;
        }

        public void setRankId(String rankId) {
            this.rankId = rankId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public List<String> getAllowedActions() {
            if (allowedActions == null) {
                allowedActions = new ArrayList<>();
            }
            return allowedActions;
        }

        public void setAllowedActions(List<String> allowedActions) {
            this.allowedActions = allowedActions != null ? allowedActions : new ArrayList<>();
        }
    }

    public static class GuildMembershipData {
        private String playerId;
        private String rankId;
        private boolean active;

        public String getPlayerId() {
            return playerId;
        }

        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }

        public String getRankId() {
            return rankId;
        }

        public void setRankId(String rankId) {
            this.rankId = rankId;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    public static class DefenderAssignmentData {
        private String structureInstanceId;
        private String robotId;
        private String role;

        public String getStructureInstanceId() {
            return structureInstanceId;
        }

        public void setStructureInstanceId(String structureInstanceId) {
            this.structureInstanceId = structureInstanceId;
        }

        public String getRobotId() {
            return robotId;
        }

        public void setRobotId(String robotId) {
            this.robotId = robotId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
