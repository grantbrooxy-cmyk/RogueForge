package com.rogueforge.game.robot;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the robot roster: collected robots and active deployment.
 * Enforces constraints on active robot count (max 8).
 */
public class RosterManager {
    public static final int MAX_ACTIVE = 8;

    private List<RobotDefinition> allRobots;
    private List<RobotDefinition> activeRobots;

    /**
     * Constructor initializing empty lists.
     */
    public RosterManager() {
        this.allRobots = new ArrayList<>();
        this.activeRobots = new ArrayList<>();
    }

    /**
     * Adds a robot to the collected roster.
     * If already collected, does nothing.
     */
    public void collect(RobotDefinition robot) {
        if (robot == null) return;
        if (!allRobots.contains(robot)) {
            allRobots.add(robot);
        }
    }

    /**
     * Removes a robot from both the collected roster and active deployment.
     */
    public void release(RobotDefinition robot) {
        if (robot == null) return;
        allRobots.remove(robot);
        activeRobots.remove(robot);
    }

    /**
     * Deploys a robot to active slots if:
     * - Robot is in collected roster
     * - Not already active
     * - Active count is below MAX_ACTIVE
     */
    public boolean deploy(RobotDefinition robot) {
        if (robot == null || !allRobots.contains(robot)) {
            return false;
        }

        if (activeRobots.contains(robot)) {
            return false; // Already deployed
        }

        if (activeRobots.size() >= MAX_ACTIVE) {
            return false; // Roster full
        }

        activeRobots.add(robot);
        return true;
    }

    /**
     * Removes a robot from active deployment (benches it).
     * Robot remains in collected roster.
     */
    public boolean bench(RobotDefinition robot) {
        if (robot == null) {
            return false;
        }
        return activeRobots.remove(robot);
    }

    /**
     * Returns the list of actively deployed robots.
     */
    public List<RobotDefinition> getActiveRobots() {
        return new ArrayList<>(activeRobots);
    }

    /**
     * Returns the list of all collected robots.
     */
    public List<RobotDefinition> getAllRobots() {
        return new ArrayList<>(allRobots);
    }

    /**
     * Returns the count of actively deployed robots.
     */
    public int getActiveCount() {
        return activeRobots.size();
    }

    /**
     * Returns the count of all collected robots.
     */
    public int getTotalCount() {
        return allRobots.size();
    }

    /**
     * Checks if a robot is actively deployed.
     */
    public boolean isActive(RobotDefinition robot) {
        return robot != null && activeRobots.contains(robot);
    }

    /**
     * Checks if a robot is in the collected roster.
     */
    public boolean isCollected(RobotDefinition robot) {
        return robot != null && allRobots.contains(robot);
    }

    /**
     * Returns available deployment slots (MAX_ACTIVE - current active count).
     */
    public int getAvailableSlots() {
        return MAX_ACTIVE - activeRobots.size();
    }

    /**
     * Clears all robots from the roster.
     */
    public void clearAll() {
        allRobots.clear();
        activeRobots.clear();
    }

    /**
     * Benches all active robots without removing them from collected roster.
     */
    public void benchAll() {
        activeRobots.clear();
    }

    @Override
    public String toString() {
        return String.format("RosterManager{collected=%d, active=%d/%d}",
                allRobots.size(), activeRobots.size(), MAX_ACTIVE);
    }
}
