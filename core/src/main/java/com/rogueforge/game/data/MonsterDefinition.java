package com.rogueforge.game.data;

/**
 * Data POJO for monster definitions loaded from JSON.
 * Represents the base stats and configuration of a monster type.
 */
public class MonsterDefinition {

    private String id;
    private String name;
    private String rank;
    private int hp;
    private int attack;
    private int defense;
    private int speed;
    private int baseLoot;
    private String aiProfile;
    private String[] lootTableIds;

    /**
     * No-arg constructor required for JSON deserialization.
     */
    public MonsterDefinition() {
    }

    public MonsterDefinition(String id, String name, String rank, int hp, int attack,
                           int defense, int speed, int baseLoot, String aiProfile, String[] lootTableIds) {
        this.id = id;
        this.name = name;
        this.rank = rank;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.baseLoot = baseLoot;
        this.aiProfile = aiProfile;
        this.lootTableIds = lootTableIds;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRank() {
        return rank;
    }

    public int getHp() {
        return hp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpeed() {
        return speed;
    }

    public int getBaseLoot() {
        return baseLoot;
    }

    public String getAiProfile() {
        return aiProfile;
    }

    public String[] getLootTableIds() {
        return lootTableIds;
    }
}
