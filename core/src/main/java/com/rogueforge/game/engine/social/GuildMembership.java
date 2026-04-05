package com.rogueforge.game.engine.social;

/**
 * Tracks one player's active guild membership and assigned rank.
 */
public class GuildMembership {
    private final String playerId;
    private String rankId;
    private boolean active;

    public GuildMembership(String playerId, String rankId, boolean active) {
        this.playerId = playerId != null ? playerId : "";
        this.rankId = rankId != null ? rankId : "";
        this.active = active;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getRankId() {
        return rankId;
    }

    public void setRankId(String rankId) {
        this.rankId = rankId != null ? rankId : "";
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
