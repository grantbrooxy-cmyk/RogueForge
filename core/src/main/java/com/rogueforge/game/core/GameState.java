package com.rogueforge.game.core;

import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.data.EquipmentItem;
import com.rogueforge.game.data.SaveFile;
import com.rogueforge.game.progression.RobotProgressionState;
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
    private String currentZoneId = "verdant_fields";
    private int currentSaveSlot = 0;

    // Equipment
    private final Map<String, String> playerEquipment = new HashMap<>();
    private final Map<String, Map<String, String>> robotEquipment = new HashMap<>();
    private final List<EquipmentItem> equipmentCatalog = new ArrayList<>();
    private final List<String> ownedEquipmentIds = new ArrayList<>();
    private final Map<String, Boolean> questFlags = new HashMap<>();
    private final List<String> keyItems = new ArrayList<>();
    private final Map<String, Integer> bestiaryScanLevels = new HashMap<>();
    private final Map<String, RobotProgressionState> robotProgressionStates = new HashMap<>();

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
        if (item == null || !gradeMeetsRequirement(getUnlockedGrade(), item.getGradeRequirement())) {
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

    // --- Robot roster ---
    public List<String> getCollectedRobotIds() { return new ArrayList<>(collectedRobotIds); }
    public List<String> getActiveRobotIds() { return new ArrayList<>(activeRobotIds); }
    public void setCollectedRobotIds(List<String> ids) {
        collectedRobotIds.clear();
        if (ids != null) collectedRobotIds.addAll(ids);
    }
    public void setActiveRobotIds(List<String> ids) {
        activeRobotIds.clear();
        if (ids != null) activeRobotIds.addAll(ids);
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

    public Map<String, Boolean> getQuestFlags() {
        return new HashMap<>(questFlags);
    }

    public void setQuestFlags(Map<String, Boolean> flags) {
        questFlags.clear();
        if (flags != null) {
            questFlags.putAll(flags);
        }
    }

    public boolean hasQuestFlag(String flag) {
        return Boolean.TRUE.equals(questFlags.get(flag));
    }

    public void setQuestFlag(String flag, boolean value) {
        questFlags.put(flag, value);
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
