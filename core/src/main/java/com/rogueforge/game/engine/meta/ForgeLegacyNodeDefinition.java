package com.rogueforge.game.engine.meta;

/**
 * Permanent endgame unlock purchased with Forge Shards.
 */
public class ForgeLegacyNodeDefinition {
    private final String id;
    private final String branch;
    private final String name;
    private final int cost;
    private final String description;

    public ForgeLegacyNodeDefinition(String id, String branch, String name, int cost, String description) {
        this.id = id;
        this.branch = branch;
        this.name = name;
        this.cost = Math.max(0, cost);
        this.description = description != null ? description : "";
    }

    public String getId() {
        return id;
    }

    public String getBranch() {
        return branch;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public String getDescription() {
        return description;
    }
}
