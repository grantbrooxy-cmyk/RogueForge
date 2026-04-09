package com.rogueforge.game.core;

import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.data.EquipmentItem;
import com.rogueforge.game.data.SaveFile;
import com.rogueforge.game.progression.RobotProgressionState;
import com.rogueforge.game.world.SettlementState;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntUnaryOperator;

/**
 * Central game state container. Holds all mutable player, robot, equipment,
 * and progression data. Extracted from GameScreen to decouple state from rendering.
 */
public class GameState {
    // Grade progression order
    private static final String[] GRADE_ORDER = {"G", "F", "E", "D", "C", "B", "A", "S", "S+", "S++", "S+++"};

    // Player base stats (before equipment/level)
    private static final float BASE_PLAYER_AGILITY = 26f;
    private static final float BASE_PLAYER_STRENGTH = 18f;
    private static final float BASE_PLAYER_INTELLIGENCE = 14f;
    private static final float BASE_PLAYER_STAMINA = 20f;
    private static final float BASE_PLAYER_MAX_HEALTH = 100f;

    // Player state
    private String playerName;
    private final Vector2 playerPos = new Vector2(640, 360);
    private float playerHealth = 100f;
    private float playerMaxHealth = 100f;
    private int playerLevel = 1;
    private int playerExperience = 0;
    private int healingPotions = 3;
    private long totalGold = 0;
    private int totalEnemiesKilled = 0;
    private float survivalTime = 0f;
    private String currentZoneId = "town";
    private int currentSaveSlot = 0;
    private int infiniteDungeonCurrentFloor = 0;
    private int infiniteDungeonBestFloor = 0;
    private int infiniteDungeonFloorsCleared = 0;
    private boolean infiniteDungeonRunActive = false;
    private long forgeShards = 0;
    private long spentForgeShards = 0;
    private final List<String> legacyUnlocks = new ArrayList<>();
    private int shardRunBestFloor = 0;
    private int totalShardRuns = 0;
    private boolean shardRunActive = false;

    // Equipment
    private final Map<String, String> playerEquipment = new HashMap<>();
    private final Map<String, Map<String, String>> robotEquipment = new HashMap<>();
    private final List<EquipmentItem> equipmentCatalog = new ArrayList<>();
    private final List<String> ownedEquipmentIds = new ArrayList<>();
    private final Map<String, String> questStates = new HashMap<>();
    private final List<String> keyItems = new ArrayList<>();
    private final Map<String, Integer> bestiaryScanLevels = new HashMap<>();
    private final Map<String, Boolean> worldStateFlags = new HashMap<>();
    private final Map<String, SettlementState> settlementUpgrades = new HashMap<>();
    private final Map<String, RobotProgressionState> robotProgressionStates = new HashMap<>();
    private final Map<String, Integer> forgeComponents = new HashMap<>();
    private final Map<String, Integer> shardInventory = new HashMap<>();
    private final Map<String, Integer> blueprintFragments = new HashMap<>();
    private long unbankedGold = 0;
    private final Map<String, Integer> unbankedForgeComponents = new HashMap<>();
    private final Map<String, Integer> unbankedShards = new HashMap<>();
    private final Map<String, Integer> unbankedBlueprintFragments = new HashMap<>();

    // Forge Core level (1 = base, 2/3/4 unlocked by boss milestones)
    // Gates robot evolution tiers: Tier 2 requires Lv2, Tier 3 requires Lv3.
    private int forgeCoreLevel = 1;
    private final List<String> defeatedBossIds = new ArrayList<>();

    // Robot roster tracking
    private final List<String> collectedRobotIds = new ArrayList<>();
    private final List<String> activeRobotIds = new ArrayList<>();

    public GameState(String playerName) {
        this.playerName = playerName != null ? playerName : "Player";
    }

    // --- Player accessors ---
    public String getPlayerName() { return playerName; }
    public Vector2 getPlayerPos() { return playerPos; }
    public float getPlayerHealth() { return playerHealth; }
    public void setPlayerHealth(float hp) { this.playerHealth = Math.max(0f, hp); }
    public float getPlayerMaxHealth() { return playerMaxHealth; }
    public void setPlayerMaxHealth(float maxHp) { this.playerMaxHealth = maxHp; }
    public int getPlayerLevel() { return playerLevel; }
    public void setPlayerLevel(int level) { this.playerLevel = Math.max(1, level); }
    public int getPlayerExperience() { return playerExperience; }
    public void setPlayerExperience(int xp) { this.playerExperience = Math.max(0, xp); }
    public int getHealingPotions() { return healingPotions; }
    public void setHealingPotions(int potions) { this.healingPotions = potions; }
    public void addHealingPotions(int amount) { this.healingPotions += amount; }
    public long getTotalGold() { return totalGold; }
    public void addGold(long amount) { this.totalGold += amount; }
    public void setTotalGold(long gold) { this.totalGold = gold; }
    public long getUnbankedGold() { return Math.max(0L, unbankedGold); }
    public void setUnbankedGold(long amount) { this.unbankedGold = Math.max(0L, amount); }
    public void addUnbankedGold(long amount) { this.unbankedGold = Math.max(0L, this.unbankedGold + amount); }
    public boolean spendGold(long amount) {
        if (amount < 0 || totalGold < amount) {
            return false;
        }
        totalGold -= amount;
        return true;
    }
    public int getTotalEnemiesKilled() { return totalEnemiesKilled; }
    public void incrementEnemiesKilled() { this.totalEnemiesKilled++; }
    public void setTotalEnemiesKilled(int count) { this.totalEnemiesKilled = count; }
    public float getSurvivalTime() { return survivalTime; }
    public void addSurvivalTime(float delta) { this.survivalTime += delta; }
    public void setSurvivalTime(float time) { this.survivalTime = time; }
    public String getCurrentZoneId() { return currentZoneId; }
    public void setCurrentZoneId(String zoneId) { this.currentZoneId = zoneId; }
    public int getCurrentSaveSlot() { return currentSaveSlot; }
    public void setCurrentSaveSlot(int slot) { this.currentSaveSlot = slot; }
    public int getInfiniteDungeonCurrentFloor() { return infiniteDungeonCurrentFloor; }
    public void setInfiniteDungeonCurrentFloor(int floor) { this.infiniteDungeonCurrentFloor = Math.max(0, floor); }
    public int getInfiniteDungeonBestFloor() { return infiniteDungeonBestFloor; }
    public void setInfiniteDungeonBestFloor(int floor) { this.infiniteDungeonBestFloor = Math.max(0, floor); }
    public int getInfiniteDungeonFloorsCleared() { return infiniteDungeonFloorsCleared; }
    public void setInfiniteDungeonFloorsCleared(int floorsCleared) { this.infiniteDungeonFloorsCleared = Math.max(0, floorsCleared); }
    public boolean isInfiniteDungeonRunActive() { return infiniteDungeonRunActive; }
    public void setInfiniteDungeonRunActive(boolean active) { this.infiniteDungeonRunActive = active; }

    // --- Shard Run and Legacy accessors ---
    public long getForgeShards() { return forgeShards; }
    public void setForgeShards(long shards) { this.forgeShards = Math.max(0, shards); }
    public void addForgeShards(long amount) { this.forgeShards = Math.max(0, this.forgeShards + amount); }
    
    public long getSpentForgeShards() { return spentForgeShards; }
    public void setSpentForgeShards(long spent) { this.spentForgeShards = Math.max(0, spent); }
    
    public List<String> getLegacyUnlocks() { return new ArrayList<>(legacyUnlocks); }
    public void setLegacyUnlocks(List<String> unlocks) {
        this.legacyUnlocks.clear();
        if (unlocks != null) this.legacyUnlocks.addAll(unlocks);
    }
    public void addLegacyUnlock(String unlockId) {
        if (unlockId != null && !legacyUnlocks.contains(unlockId)) {
            legacyUnlocks.add(unlockId);
        }
    }
    public boolean hasLegacyUnlock(String unlockId) {
        return unlockId != null && legacyUnlocks.contains(unlockId);
    }

    public int getShardRunBestFloor() { return shardRunBestFloor; }
    public void setShardRunBestFloor(int floor) { this.shardRunBestFloor = Math.max(shardRunBestFloor, floor); }
    
    public int getTotalShardRuns() { return totalShardRuns; }
    public void incrementTotalShardRuns() { this.totalShardRuns++; }
    public void setTotalShardRuns(int count) { this.totalShardRuns = count; }

    public boolean isShardRunActive() { return shardRunActive; }
    public void setShardRunActive(boolean active) { this.shardRunActive = active; }

    // --- Experience and leveling ---
    public int getExperienceForNextLevel() {
        return 40 + (playerLevel * 20);
    }

    public void addExperience(int amount) {
        addExperience(amount, level -> 40 + (level * 20));
    }

    public void addExperience(int amount, IntUnaryOperator experienceRequirementProvider) {
        int adjustedAmount = Math.max(0, applyUniqueExperienceBoost(amount));
        playerExperience += adjustedAmount;
        IntUnaryOperator provider = experienceRequirementProvider != null
            ? experienceRequirementProvider
            : level -> 40 + (level * 20);
        while (playerExperience >= provider.applyAsInt(playerLevel)) {
            playerExperience -= provider.applyAsInt(playerLevel);
            playerLevel++;
            // Heal a bit on level up
            StatBlock stats = getPlayerStats();
            playerHealth = Math.min(stats.maxHealth, playerHealth + 18f);
        }
    }

    // --- Forge Core ---
    /** Returns the current Forge Core level (1–4). */
    public int getForgeCoreLevel() {
        return forgeCoreLevel;
    }

    /**
     * Sets the Forge Core level, clamped to [1, 4].
     * Level 1 is the starting state; levels 2–4 are unlocked by boss milestones.
     */
    public void setForgeCoreLevel(int level) {
        this.forgeCoreLevel = Math.max(1, Math.min(4, level));
    }

    public List<String> getDefeatedBossIds() {
        return new ArrayList<>(defeatedBossIds);
    }

    public void setDefeatedBossIds(List<String> bossIds) {
        defeatedBossIds.clear();
        if (bossIds == null) {
            return;
        }
        for (String bossId : bossIds) {
            if (bossId != null && !bossId.isEmpty() && !defeatedBossIds.contains(bossId)) {
                defeatedBossIds.add(bossId);
            }
        }
    }

    public boolean markBossDefeated(String bossId) {
        if (bossId == null || bossId.isEmpty() || defeatedBossIds.contains(bossId)) {
            return false;
        }
        defeatedBossIds.add(bossId);
        return true;
    }

    public boolean hasDefeatedBoss(String bossId) {
        return bossId != null && defeatedBossIds.contains(bossId);
    }

    public int getDefeatedBossCount() {
        return defeatedBossIds.size();
    }

    // --- Grade system ---
    public String getUnlockedGrade() {
        return getGradeForLevel(playerLevel);
    }

    public String getGradeForLevel(int level) {
        if (level >= 99) return "S+++";
        if (level >= 90) return "S++";
        if (level >= 80) return "S+";
        if (level >= 70) return "S";
        if (level >= 60) return "A";
        if (level >= 50) return "B";
        if (level >= 40) return "C";
        if (level >= 30) return "D";
        if (level >= 20) return "E";
        if (level >= 10) return "F";
        return "G";
    }

    public boolean gradeMeetsRequirement(String ownerGrade, String requiredGrade) {
        return getGradeIndex(ownerGrade) >= getGradeIndex(requiredGrade);
    }

    private int getGradeIndex(String grade) {
        if (grade == null || grade.isEmpty()) return 0;
        for (int i = 0; i < GRADE_ORDER.length; i++) {
            if (GRADE_ORDER[i].equals(grade)) return i;
        }
        return 0;
    }

    // --- Equipment catalog ---
    public List<EquipmentItem> getEquipmentCatalog() {
        return new ArrayList<>(equipmentCatalog);
    }

    public void addEquipmentToCatalog(EquipmentItem item) {
        equipmentCatalog.add(item);
    }

    public void clearEquipmentCatalog() {
        equipmentCatalog.clear();
    }

    public List<String> getOwnedEquipmentIds() {
        return new ArrayList<>(ownedEquipmentIds);
    }

    public void setOwnedEquipmentIds(List<String> itemIds) {
        ownedEquipmentIds.clear();
        if (itemIds != null) {
            ownedEquipmentIds.addAll(itemIds);
        }
    }

    public boolean unlockEquipment(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return false;
        }
        if (findEquipmentItem(itemId) == null) {
            return false;
        }
        if (!ownedEquipmentIds.contains(itemId)) {
            ownedEquipmentIds.add(itemId);
        }
        return true;
    }

    public boolean removeOwnedEquipment(String itemId) {
        return ownedEquipmentIds.remove(itemId);
    }

    public EquipmentItem findEquipmentItem(String itemId) {
        for (EquipmentItem item : equipmentCatalog) {
            if (item.getId().equals(itemId)) return item;
        }
        return null;
    }

    // --- Player equipment ---
    public Map<String, String> getPlayerEquipmentSlots() {
        return new HashMap<>(playerEquipment);
    }

    public boolean equipPlayerItem(EquipmentItem item) {
        if (item == null
            || !item.isPlayerEquipment()
            || !gradeMeetsRequirement(getUnlockedGrade(), item.getGradeRequirement())) {
            return false;
        }
        unlockEquipment(item.getId());
        playerEquipment.put(item.getSlotType(), item.getId());
        playerHealth = Math.min(getPlayerStats().maxHealth, playerHealth);
        return true;
    }

    public void setPlayerEquipment(Map<String, String> equipment) {
        playerEquipment.clear();
        if (equipment != null) playerEquipment.putAll(equipment);
    }

    // --- Robot equipment ---
    public Map<String, String> getRobotEquipmentSlots(String robotId) {
        Map<String, String> slots = robotEquipment.get(robotId);
        return slots != null ? new HashMap<>(slots) : new HashMap<>();
    }

    public boolean equipRobotItem(String robotId, String robotGrade, EquipmentItem item) {
        if (item == null
            || !item.isRobotEquipment()
            || !gradeMeetsRequirement(robotGrade, item.getGradeRequirement())) {
            return false;
        }
        unlockEquipment(item.getId());
        robotEquipment.computeIfAbsent(robotId, k -> new HashMap<>());
        robotEquipment.get(robotId).put(item.getSlotType(), item.getId());
        return true;
    }

    public Map<String, Map<String, String>> getRobotEquipment() {
        return robotEquipment;
    }

    public void setRobotEquipment(Map<String, Map<String, String>> equipment) {
        robotEquipment.clear();
        if (equipment == null) return;
        for (Map.Entry<String, Map<String, String>> entry : equipment.entrySet()) {
            robotEquipment.put(entry.getKey(),
                entry.getValue() != null ? new HashMap<>(entry.getValue()) : new HashMap<>());
        }
    }

    public Map<String, Integer> getForgeComponents() {
        return new HashMap<>(forgeComponents);
    }

    public void setForgeComponents(Map<String, Integer> components) {
        forgeComponents.clear();
        if (components == null) {
            return;
        }
        for (Map.Entry<String, Integer> entry : components.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null && entry.getValue() > 0) {
                forgeComponents.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public int getForgeComponentCount(String componentId) {
        return forgeComponents.getOrDefault(componentId, 0);
    }

    public Map<String, Integer> getUnbankedForgeComponents() {
        return new HashMap<>(unbankedForgeComponents);
    }

    public void setUnbankedForgeComponents(Map<String, Integer> components) {
        unbankedForgeComponents.clear();
        if (components == null) {
            return;
        }
        for (Map.Entry<String, Integer> entry : components.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null && entry.getValue() > 0) {
                unbankedForgeComponents.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public int getUnbankedForgeComponentCount(String componentId) {
        return unbankedForgeComponents.getOrDefault(componentId, 0);
    }

    public void addUnbankedForgeComponent(String componentId, int amount) {
        if (componentId == null || componentId.isEmpty() || amount == 0) {
            return;
        }
        unbankedForgeComponents.put(componentId, Math.max(0, unbankedForgeComponents.getOrDefault(componentId, 0) + amount));
        if (unbankedForgeComponents.get(componentId) <= 0) {
            unbankedForgeComponents.remove(componentId);
        }
    }

    public void clearUnbankedForgeComponents() {
        unbankedForgeComponents.clear();
    }

    public void addForgeComponent(String componentId, int amount) {
        if (componentId == null || componentId.isEmpty() || amount == 0) {
            return;
        }
        forgeComponents.put(componentId, Math.max(0, forgeComponents.getOrDefault(componentId, 0) + amount));
        if (forgeComponents.get(componentId) <= 0) {
            forgeComponents.remove(componentId);
        }
    }

    public boolean consumeForgeComponents(Map<String, Integer> costs) {
        if (costs == null || costs.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, Integer> entry : costs.entrySet()) {
            if (getForgeComponentCount(entry.getKey()) < Math.max(0, entry.getValue())) {
                return false;
            }
        }
        for (Map.Entry<String, Integer> entry : costs.entrySet()) {
            addForgeComponent(entry.getKey(), -Math.max(0, entry.getValue()));
        }
        return true;
    }

    public Map<String, Integer> getShardInventory() {
        return new HashMap<>(shardInventory);
    }

    public void setShardInventory(Map<String, Integer> shards) {
        shardInventory.clear();
        if (shards == null) {
            return;
        }
        for (Map.Entry<String, Integer> entry : shards.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null && entry.getValue() > 0) {
                shardInventory.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public int getShardCount(String grade) {
        return shardInventory.getOrDefault(grade, 0);
    }

    public Map<String, Integer> getUnbankedShards() {
        return new HashMap<>(unbankedShards);
    }

    public void setUnbankedShards(Map<String, Integer> shards) {
        unbankedShards.clear();
        if (shards == null) {
            return;
        }
        for (Map.Entry<String, Integer> entry : shards.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null && entry.getValue() > 0) {
                unbankedShards.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public int getUnbankedShardCount(String grade) {
        return unbankedShards.getOrDefault(grade, 0);
    }

    public void addUnbankedShard(String grade, int amount) {
        if (grade == null || grade.isEmpty() || amount == 0) {
            return;
        }
        unbankedShards.put(grade, Math.max(0, unbankedShards.getOrDefault(grade, 0) + amount));
        if (unbankedShards.get(grade) <= 0) {
            unbankedShards.remove(grade);
        }
    }

    public void clearUnbankedShards() {
        unbankedShards.clear();
    }

    public void addShard(String grade, int amount) {
        if (grade == null || grade.isEmpty() || amount == 0) {
            return;
        }
        shardInventory.put(grade, Math.max(0, shardInventory.getOrDefault(grade, 0) + amount));
        if (shardInventory.get(grade) <= 0) {
            shardInventory.remove(grade);
        }
    }

    public boolean consumeShards(String grade, int amount) {
        if (amount <= 0) {
            return true;
        }
        if (getShardCount(grade) < amount) {
            return false;
        }
        addShard(grade, -amount);
        return true;
    }

    public Map<String, Integer> getBlueprintFragments() {
        return new HashMap<>(blueprintFragments);
    }

    public void setBlueprintFragments(Map<String, Integer> fragments) {
        blueprintFragments.clear();
        if (fragments == null) {
            return;
        }
        for (Map.Entry<String, Integer> entry : fragments.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null && entry.getValue() > 0) {
                blueprintFragments.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public int getBlueprintFragmentCount(String fragmentId) {
        return blueprintFragments.getOrDefault(fragmentId, 0);
    }

    public void addBlueprintFragment(String fragmentId, int amount) {
        if (fragmentId == null || fragmentId.isEmpty() || amount == 0) {
            return;
        }
        blueprintFragments.put(fragmentId, Math.max(0, blueprintFragments.getOrDefault(fragmentId, 0) + amount));
        if (blueprintFragments.get(fragmentId) <= 0) {
            blueprintFragments.remove(fragmentId);
        }
    }

    public Map<String, Integer> getUnbankedBlueprintFragments() {
        return new HashMap<>(unbankedBlueprintFragments);
    }

    public void setUnbankedBlueprintFragments(Map<String, Integer> fragments) {
        unbankedBlueprintFragments.clear();
        if (fragments == null) {
            return;
        }
        for (Map.Entry<String, Integer> entry : fragments.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null && entry.getValue() > 0) {
                unbankedBlueprintFragments.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public int getUnbankedBlueprintFragmentCount(String fragmentId) {
        return unbankedBlueprintFragments.getOrDefault(fragmentId, 0);
    }

    public void addUnbankedBlueprintFragment(String fragmentId, int amount) {
        if (fragmentId == null || fragmentId.isEmpty() || amount == 0) {
            return;
        }
        unbankedBlueprintFragments.put(fragmentId, Math.max(0, unbankedBlueprintFragments.getOrDefault(fragmentId, 0) + amount));
        if (unbankedBlueprintFragments.get(fragmentId) <= 0) {
            unbankedBlueprintFragments.remove(fragmentId);
        }
    }

    public void clearUnbankedBlueprintFragments() {
        unbankedBlueprintFragments.clear();
    }

    public boolean consumeBlueprintFragments(String fragmentId, int amount) {
        if (amount <= 0) {
            return true;
        }
        if (getBlueprintFragmentCount(fragmentId) < amount) {
            return false;
        }
        addBlueprintFragment(fragmentId, -amount);
        return true;
    }

    // --- Robot roster ---
    public List<String> getCollectedRobotIds() { return new ArrayList<>(collectedRobotIds); }
    public List<String> getActiveRobotIds() { return new ArrayList<>(activeRobotIds); }
    public void setCollectedRobotIds(List<String> ids) {
        collectedRobotIds.clear();
        if (ids != null) {
            for (String id : ids) {
                if (id != null && !id.isEmpty()) {
                    collectedRobotIds.add(id);
                }
            }
        }
    }
    public void setActiveRobotIds(List<String> ids) {
        activeRobotIds.clear();
        if (ids != null) {
            activeRobotIds.addAll(ids);
        }
    }

    public Map<String, RobotProgressionState> getRobotProgressionStates() {
        return new HashMap<>(robotProgressionStates);
    }

    public void setRobotProgressionStates(Map<String, RobotProgressionState> states) {
        robotProgressionStates.clear();
        if (states != null) {
            robotProgressionStates.putAll(states);
        }
    }

    public RobotProgressionState getRobotProgressionState(String robotId) {
        return robotProgressionStates.get(robotId);
    }

    public void putRobotProgressionState(RobotProgressionState state) {
        if (state != null && state.getRobotId() != null) {
            robotProgressionStates.put(state.getRobotId(), state);
        }
    }

    public void removeRobotProgressionState(String robotId) {
        if (robotId != null) {
            robotProgressionStates.remove(robotId);
        }
    }

    public Map<String, String> getQuestStates() {
        return new HashMap<>(questStates);
    }

    public void setQuestStates(Map<String, String> states) {
        questStates.clear();
        if (states != null) {
            questStates.putAll(states);
        }
    }

    public String getQuestState(String questId) {
        return questStates.getOrDefault(questId, "NOT_STARTED");
    }

    public void setQuestState(String questId, String state) {
        if (questId != null && !questId.isEmpty()) {
            questStates.put(questId, state != null ? state : "NOT_STARTED");
        }
    }

    public List<String> getKeyItems() {
        return new ArrayList<>(keyItems);
    }

    public void setKeyItems(List<String> items) {
        keyItems.clear();
        if (items != null) {
            keyItems.addAll(items);
        }
    }

    public boolean hasKeyItem(String keyItem) {
        return keyItems.contains(keyItem);
    }

    public void addKeyItem(String keyItem) {
        if (keyItem != null && !keyItem.isEmpty() && !keyItems.contains(keyItem)) {
            keyItems.add(keyItem);
        }
    }

    public Map<String, Integer> getBestiaryScanLevels() {
        return new HashMap<>(bestiaryScanLevels);
    }

    public void setBestiaryScanLevels(Map<String, Integer> scanLevels) {
        bestiaryScanLevels.clear();
        if (scanLevels != null) {
            bestiaryScanLevels.putAll(scanLevels);
        }
    }

    public int getBestiaryScanLevel(String monsterId) {
        return bestiaryScanLevels.getOrDefault(monsterId, 0);
    }

    public void setBestiaryScanLevel(String monsterId, int scanLevel) {
        if (monsterId != null && !monsterId.isEmpty()) {
            bestiaryScanLevels.put(monsterId, Math.max(0, Math.min(3, scanLevel)));
        }
    }

    public Map<String, Boolean> getWorldStateFlags() {
        return new HashMap<>(worldStateFlags);
    }

    public void setWorldStateFlags(Map<String, Boolean> flags) {
        worldStateFlags.clear();
        if (flags != null) {
            worldStateFlags.putAll(flags);
        }
    }

    public boolean isWorldStateFlagActive(String flag) {
        return Boolean.TRUE.equals(worldStateFlags.get(flag));
    }

    public void setWorldStateFlag(String flag, boolean value) {
        if (flag != null && !flag.isEmpty()) {
            worldStateFlags.put(flag, value);
        }
    }

    public Map<String, SettlementState> getSettlementUpgrades() {
        return new HashMap<>(settlementUpgrades);
    }

    public void setSettlementUpgrades(Map<String, SettlementState> upgrades) {
        settlementUpgrades.clear();
        if (upgrades != null) {
            settlementUpgrades.putAll(upgrades);
        }
    }

    public SettlementState getSettlementUpgrade(String upgradeId) {
        return settlementUpgrades.get(upgradeId);
    }

    public void putSettlementUpgrade(SettlementState state) {
        if (state != null && state.getUpgradeId() != null) {
            settlementUpgrades.put(state.getUpgradeId(), state);
        }
    }

    // --- Equipment stat calculations ---
    public EquipmentTotals getPlayerEquipmentTotals() {
        EquipmentTotals totals = new EquipmentTotals();
        for (String itemId : playerEquipment.values()) {
            EquipmentItem item = findEquipmentItem(itemId);
            if (item != null) applyEquipmentToTotals(totals, item);
        }
        return totals;
    }

    public EquipmentTotals getEquipmentTotals(String robotId) {
        EquipmentTotals totals = new EquipmentTotals();
        Map<String, String> equipped = robotEquipment.get(robotId);
        if (equipped == null) return totals;
        for (String itemId : equipped.values()) {
            EquipmentItem item = findEquipmentItem(itemId);
            if (item != null) applyEquipmentToTotals(totals, item);
        }
        return totals;
    }

    private void applyEquipmentToTotals(EquipmentTotals totals, EquipmentItem item) {
        totals.hpBonus += item.getHpBonus();
        totals.strengthBonus += item.getAttackBonus();
        totals.staminaBonus += item.getDefenseBonus();
        totals.agilityBonus += item.getSpeedBonus();
        totals.intelligenceBonus += item.getIntelligenceBonus();
        if ("XP_BOOST".equals(item.getUniqueBoost())) {
            totals.xpMultiplier += 0.2f;
        } else if ("FIRST_STRIKE".equals(item.getUniqueBoost())) {
            totals.strengthBonus += 4f;
        } else if ("ARCANE_SURGE".equals(item.getUniqueBoost())) {
            totals.intelligenceBonus += 6f;
        }
    }

    private int applyUniqueExperienceBoost(int amount) {
        EquipmentTotals totals = getPlayerEquipmentTotals();
        return Math.round(amount * totals.getXpMultiplier());
    }

    public int previewLevelUps(int amount) {
        int levels = 0;
        int simulatedLevel = playerLevel;
        int simulatedExperience = playerExperience + Math.max(0, applyUniqueExperienceBoost(amount));
        while (simulatedExperience >= (40 + (simulatedLevel * 20))) {
            simulatedExperience -= (40 + (simulatedLevel * 20));
            simulatedLevel++;
            levels++;
        }
        return levels;
    }

    // --- Stat blocks ---
    public StatBlock getPlayerStats() {
        EquipmentTotals eq = getPlayerEquipmentTotals();
        float levelOffset = playerLevel - 1;
        return new StatBlock(
            playerHealth,
            playerMaxHealth + (levelOffset * 6f) + eq.hpBonus,
            BASE_PLAYER_AGILITY + (levelOffset * 0.5f) + eq.agilityBonus,
            BASE_PLAYER_STRENGTH + (levelOffset * 0.7f) + eq.strengthBonus,
            BASE_PLAYER_INTELLIGENCE + (levelOffset * 0.65f) + eq.intelligenceBonus,
            BASE_PLAYER_STAMINA + (levelOffset * 0.6f) + eq.staminaBonus
        );
    }

    // --- Inner classes ---
    public static class StatBlock {
        public float currentHealth;
        public float maxHealth;
        public float agility;
        public float strength;
        public float intelligence;
        public float stamina;

        public StatBlock(float currentHealth, float maxHealth, float agility,
                        float strength, float intelligence, float stamina) {
            this.currentHealth = currentHealth;
            this.maxHealth = maxHealth;
            this.agility = agility;
            this.strength = strength;
            this.intelligence = intelligence;
            this.stamina = stamina;
        }
    }

    public static class EquipmentTotals {
        public float hpBonus;
        public float agilityBonus;
        public float strengthBonus;
        public float intelligenceBonus;
        public float staminaBonus;
        public float xpMultiplier = 1f;

        public float getXpMultiplier() { return xpMultiplier; }
    }
}
