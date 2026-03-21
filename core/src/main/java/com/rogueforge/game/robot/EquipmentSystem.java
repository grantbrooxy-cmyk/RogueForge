package com.rogueforge.game.robot;

import com.rogueforge.game.data.EquipmentItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages equipment attachment to RobotDefinitions.
 * Uses nested maps keyed by robot ID and equipment slot.
 */
public class EquipmentSystem {
    /**
     * Structure: robotId -> (equipmentSlot -> equipmentItem)
     */
    private Map<String, Map<RobotDefinition.EquipmentSlot, EquipmentItem>> robotEquipment;

    /**
     * Constructor initializing empty equipment map.
     */
    public EquipmentSystem() {
        this.robotEquipment = new HashMap<>();
    }

    /**
     * Equips an item to a robot in the appropriate equipment slot.
     * Replaces any existing item in that slot and invalidates the robot's stat cache.
     *
     * @param robot The robot to equip
     * @param item  The equipment item to attach
     * @return true if equipment was successful, false if item slot doesn't match robot slots
     */
    public boolean equip(RobotDefinition robot, EquipmentItem item) {
        if (robot == null || item == null) {
            return false;
        }

        robot.setEquipmentSystem(this);

        RobotDefinition.EquipmentSlot itemSlot = RobotDefinition.EquipmentSlot.valueOf(item.getSlotType());

        // Verify the robot can equip this item (has the slot available)
        boolean hasSlot = false;
        for (RobotDefinition.EquipmentSlot slot : robot.getEquipmentSlots()) {
            if (slot == itemSlot) {
                hasSlot = true;
                break;
            }
        }

        if (!hasSlot) {
            return false;
        }

        // Get or create equipment map for this robot
        String robotId = robot.getId();
        robotEquipment.computeIfAbsent(robotId, k -> new HashMap<>());
        Map<RobotDefinition.EquipmentSlot, EquipmentItem> robotSlots = robotEquipment.get(robotId);

        // Equip the item
        robotSlots.put(itemSlot, item);

        // Invalidate cached stats
        robot.invalidateStats();

        return true;
    }

    /**
     * Unequips an item from a robot at the specified slot.
     * Invalidates the robot's stat cache.
     *
     * @param robot The robot to unequip from
     * @param slot  The equipment slot to clear
     * @return true if an item was removed, false if slot was empty or robot not found
     */
    public boolean unequip(RobotDefinition robot, RobotDefinition.EquipmentSlot slot) {
        if (robot == null || slot == null) {
            return false;
        }

        robot.setEquipmentSystem(this);

        String robotId = robot.getId();
        Map<RobotDefinition.EquipmentSlot, EquipmentItem> robotSlots = robotEquipment.get(robotId);

        if (robotSlots == null) {
            return false;
        }

        boolean wasEquipped = robotSlots.remove(slot) != null;

        if (wasEquipped) {
            robot.invalidateStats();
        }

        return wasEquipped;
    }

    /**
     * Retrieves the currently equipped item in a specific slot.
     *
     * @param robot The robot to query
     * @param slot  The equipment slot to check
     * @return The equipped item, or null if nothing is equipped in that slot
     */
    public EquipmentItem getEquipped(RobotDefinition robot, RobotDefinition.EquipmentSlot slot) {
        if (robot == null || slot == null) {
            return null;
        }

        String robotId = robot.getId();
        Map<RobotDefinition.EquipmentSlot, EquipmentItem> robotSlots = robotEquipment.get(robotId);

        if (robotSlots == null) {
            return null;
        }

        return robotSlots.get(slot);
    }

    /**
     * Returns all equipped items for a robot as a map.
     *
     * @param robot The robot to query
     * @return Map of slot -> equipment item, empty map if no equipment
     */
    public Map<RobotDefinition.EquipmentSlot, EquipmentItem> getAllEquipped(RobotDefinition robot) {
        if (robot == null) {
            return new HashMap<>();
        }

        robot.setEquipmentSystem(this);

        String robotId = robot.getId();
        Map<RobotDefinition.EquipmentSlot, EquipmentItem> robotSlots = robotEquipment.get(robotId);

        return robotSlots != null ? new HashMap<>(robotSlots) : new HashMap<>();
    }

    /**
     * Unequips all items from a robot.
     *
     * @param robot The robot to strip
     */
    public void unequipAll(RobotDefinition robot) {
        if (robot == null) {
            return;
        }

        robot.setEquipmentSystem(this);

        String robotId = robot.getId();
        Map<RobotDefinition.EquipmentSlot, EquipmentItem> robotSlots = robotEquipment.get(robotId);

        if (robotSlots != null && !robotSlots.isEmpty()) {
            robotSlots.clear();
            robot.invalidateStats();
        }
    }

    /**
     * Calculates total stat bonuses from all equipped items for a robot.
     * Used for stat computation in RobotDefinition.recalcStats().
     *
     * @param robot The robot to calculate bonuses for
     * @return A Stats object representing equipment bonuses
     */
    public RobotDefinition.Stats calculateEquipmentBonuses(RobotDefinition robot) {
        int hpBonus = 0;
        int attackBonus = 0;
        int defenseBonus = 0;
        int speedBonus = 0;

        Map<RobotDefinition.EquipmentSlot, EquipmentItem> equipped = getAllEquipped(robot);

        for (EquipmentItem item : equipped.values()) {
            hpBonus += item.getHpBonus();
            attackBonus += item.getAttackBonus();
            defenseBonus += item.getDefenseBonus();
            speedBonus += item.getSpeedBonus();
        }

        return new RobotDefinition.Stats(hpBonus, attackBonus, defenseBonus, speedBonus);
    }

    /**
     * Checks if a robot has an item equipped in a specific slot.
     *
     * @param robot The robot to check
     * @param slot  The slot to verify
     * @return true if an item is equipped in that slot
     */
    public boolean hasEquipped(RobotDefinition robot, RobotDefinition.EquipmentSlot slot) {
        return getEquipped(robot, slot) != null;
    }

    /**
     * Removes all equipment data for a robot (e.g., when robot is released).
     *
     * @param robot The robot to clear equipment from
     */
    public void removeRobot(RobotDefinition robot) {
        if (robot == null) {
            return;
        }
        robotEquipment.remove(robot.getId());
    }

    /**
     * Clears all equipment data.
     */
    public void clearAll() {
        robotEquipment.clear();
    }

    /**
     * Returns the number of robots with equipment attached.
     */
    public int getRobotEquipmentCount() {
        return robotEquipment.size();
    }

    @Override
    public String toString() {
        return String.format("EquipmentSystem{equipmentRecords=%d}", robotEquipment.size());
    }
}
