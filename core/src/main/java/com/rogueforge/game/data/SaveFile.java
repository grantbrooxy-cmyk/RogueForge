package com.rogueforge.game.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Full serializable game state.
 * Contains all player, robot, and progression data for a save game.
 */
public class SaveFile {

    private String playerName;
    private int playerHp;
    private int playerMaxHp;
    private float playerX;
    private float playerY;
    private String currentZoneId;
    private long currencyBalance;
    private int healingPotions;
    private int playerLevel;
    private int playerExperience;
    private Map<String, String> playerEquipment;
    private List<String> ownedEquipmentIds;
    private Map<String, Boolean> questFlags;
    private List<String> keyItems;

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

    // Current run state
    private int totalEnemiesKilled;
    private List<EnemyState> enemies;
    private List<ChestState> chests;

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
        this.questFlags = new HashMap<>();
        this.keyItems = new ArrayList<>();
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
        this.questFlags = new HashMap<>();
        this.keyItems = new ArrayList<>();
    }

    // Getters
    public String getPlayerName() {
        return playerName;
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

    public String getCurrentZoneId() {
        return currentZoneId;
    }

    public long getCurrencyBalance() {
        return currencyBalance;
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

    public Map<String, String> getPlayerEquipment() {
        return playerEquipment;
    }

    public List<String> getOwnedEquipmentIds() {
        return ownedEquipmentIds;
    }

    public Map<String, Boolean> getQuestFlags() {
        return questFlags;
    }

    public List<String> getKeyItems() {
        return keyItems;
    }

    public Map<String, Map<String, String>> getRobotEquipment() {
        return robotEquipment;
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

    public void setCurrentZoneId(String currentZoneId) {
        this.currentZoneId = currentZoneId;
    }

    public void setCurrencyBalance(long currencyBalance) {
        this.currencyBalance = currencyBalance;
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

    public void setPlayerEquipment(Map<String, String> playerEquipment) {
        this.playerEquipment = playerEquipment;
    }

    public void setOwnedEquipmentIds(List<String> ownedEquipmentIds) {
        this.ownedEquipmentIds = ownedEquipmentIds;
    }

    public void setQuestFlags(Map<String, Boolean> questFlags) {
        this.questFlags = questFlags;
    }

    public void setKeyItems(List<String> keyItems) {
        this.keyItems = keyItems;
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

    /**
     * Serializable live enemy state for restoring an in-progress run.
     */
    public static class EnemyState {
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
        private String name;
        private boolean alive;
        private float attackTimer;
        private float patrolTargetX;
        private float patrolTargetY;

        public EnemyState() {
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
}
