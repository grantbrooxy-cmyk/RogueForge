package com.rogueforge.game.robot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure data class representing a robot's core definition, attributes, and equipment slots.
 * Designed for serialization with LibGDX Json.
 */
public class RobotDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Robot role enum defining combat archetype.
     */
    public enum RoleEnum {
        TANK,
        DPS,
        SUPPORT,
        SCOUT
    }

    /**
     * Equipment slot types available on a robot.
     */
    public enum EquipmentSlot {
        HEAD,
        BODY,
        ARMS,
        LEGS,
        WEAPON,
        ACCESSORY
    }

    /**
     * Computed stats including equipment bonuses.
     */
    public static class Stats {
        public int hp;
        public int attack;
        public int defense;
        public int speed;

        public Stats(int hp, int attack, int defense, int speed) {
            this.hp = hp;
            this.attack = attack;
            this.defense = defense;
            this.speed = speed;
        }

        @Override
        public String toString() {
            return String.format("Stats{hp=%d, atk=%d, def=%d, spd=%d}", hp, attack, defense, speed);
        }
    }

    // Core fields
    private String id;
    private String name;
    private RoleEnum role;
    private int baseHp;
    private int baseAttack;
    private int baseDefense;
    private int baseSpeed;

    // Equipment and abilities
    private EquipmentSlot[] equipmentSlots;
    private List<String> abilityIds;

    // Current equipped items per slot (populated by EquipmentSystem)
    private transient Stats cachedStats;
    private transient EquipmentSystem equipmentSystem;

    /**
     * No-arg constructor for LibGDX Json serialization.
     */
    public RobotDefinition() {
        this.equipmentSlots = new EquipmentSlot[0];
        this.abilityIds = new ArrayList<>();
        this.cachedStats = null;
    }

    /**
     * Full constructor.
     */
    public RobotDefinition(String id, String name, RoleEnum role, int baseHp, int baseAttack,
                           int baseDefense, int baseSpeed, EquipmentSlot[] equipmentSlots,
                           List<String> abilityIds) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseSpeed = baseSpeed;
        this.equipmentSlots = equipmentSlots != null ? equipmentSlots : new EquipmentSlot[0];
        this.abilityIds = abilityIds != null ? abilityIds : new ArrayList<>();
        this.cachedStats = null;
    }

    /**
     * Recalculates and returns stats based on base values, role bonuses, and equipped items.
     * This method should be called after equipment changes.
     */
    public Stats recalcStats() {
        // Start with base stats
        int hp = baseHp;
        int attack = baseAttack;
        int defense = baseDefense;
        int speed = baseSpeed;

        // Apply role bonuses (if role bonus system is used)
        // This will be enhanced when EquipmentSystem and RoleBonus are available
        Stats roleStats = RoleBonus.applyRoleBonus(this);
        hp = roleStats.hp;
        attack = roleStats.attack;
        defense = roleStats.defense;
        speed = roleStats.speed;

        if (equipmentSystem != null) {
            Stats equipmentStats = equipmentSystem.calculateEquipmentBonuses(this);
            hp += equipmentStats.hp;
            attack += equipmentStats.attack;
            defense += equipmentStats.defense;
            speed += equipmentStats.speed;
        }

        cachedStats = new Stats(hp, attack, defense, speed);
        return cachedStats;
    }

    /**
     * Returns cached stats or recalculates if not available.
     */
    public Stats getStats() {
        if (cachedStats == null) {
            recalcStats();
        }
        return cachedStats;
    }

    /**
     * Invalidates cached stats (called when equipment changes).
     */
    public void invalidateStats() {
        this.cachedStats = null;
    }

    public void setEquipmentSystem(EquipmentSystem equipmentSystem) {
        this.equipmentSystem = equipmentSystem;
        invalidateStats();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
        invalidateStats();
    }

    public int getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
        invalidateStats();
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public void setBaseAttack(int baseAttack) {
        this.baseAttack = baseAttack;
        invalidateStats();
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public void setBaseDefense(int baseDefense) {
        this.baseDefense = baseDefense;
        invalidateStats();
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(int baseSpeed) {
        this.baseSpeed = baseSpeed;
        invalidateStats();
    }

    public EquipmentSlot[] getEquipmentSlots() {
        return equipmentSlots;
    }

    public void setEquipmentSlots(EquipmentSlot[] equipmentSlots) {
        this.equipmentSlots = equipmentSlots != null ? equipmentSlots : new EquipmentSlot[0];
    }

    public List<String> getAbilityIds() {
        return abilityIds;
    }

    public void setAbilityIds(List<String> abilityIds) {
        this.abilityIds = abilityIds != null ? abilityIds : new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("RobotDefinition{id='%s', name='%s', role=%s, hp=%d, atk=%d, def=%d, spd=%d}",
                id, name, role, baseHp, baseAttack, baseDefense, baseSpeed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RobotDefinition that = (RobotDefinition) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
