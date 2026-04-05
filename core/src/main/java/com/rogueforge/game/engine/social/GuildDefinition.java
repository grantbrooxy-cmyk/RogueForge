package com.rogueforge.game.engine.social;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistent social definition for one player-created guild.
 */
public class GuildDefinition {
    public static final String RANK_GUILD_MASTER = "guild_master";
    public static final String RANK_OFFICER = "officer";
    public static final String RANK_QUARTERMASTER = "quartermaster";
    public static final String RANK_ARCHITECT = "architect";
    public static final String RANK_MEMBER = "member";
    public static final String RANK_RECRUIT = "recruit";

    private final String guildId;
    private String displayName;
    private final String founderPlayerId;
    private boolean recruitingOpen = false;
    private String hallZoneId = "";
    private String hallClaimedSiteId = "";
    private final Map<String, GuildRank> ranksById = new LinkedHashMap<>();
    private final List<GuildMembership> memberships = new ArrayList<>();

    public GuildDefinition(String guildId, String displayName, String founderPlayerId) {
        this.guildId = guildId != null ? guildId : "";
        this.displayName = displayName != null ? displayName : "";
        this.founderPlayerId = founderPlayerId != null ? founderPlayerId : "";
    }

    public static GuildDefinition createWithDefaultRanks(String guildId, String displayName, String founderPlayerId) {
        GuildDefinition definition = new GuildDefinition(guildId, displayName, founderPlayerId);
        definition.registerDefaultRanks();
        if (founderPlayerId != null && !founderPlayerId.isEmpty()) {
            definition.upsertMembership(new GuildMembership(founderPlayerId, RANK_GUILD_MASTER, true));
        }
        return definition;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName != null ? displayName : "";
    }

    public String getFounderPlayerId() {
        return founderPlayerId;
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
        this.hallZoneId = hallZoneId != null ? hallZoneId : "";
    }

    public String getHallClaimedSiteId() {
        return hallClaimedSiteId;
    }

    public void setHallClaimedSiteId(String hallClaimedSiteId) {
        this.hallClaimedSiteId = hallClaimedSiteId != null ? hallClaimedSiteId : "";
    }

    public List<GuildRank> getRanks() {
        return new ArrayList<>(ranksById.values());
    }

    public GuildRank getRank(String rankId) {
        return rankId != null ? ranksById.get(rankId) : null;
    }

    public void registerRank(GuildRank rank) {
        if (rank == null || rank.getId() == null || rank.getId().isEmpty()) {
            return;
        }
        ranksById.put(rank.getId(), rank);
    }

    public List<GuildMembership> getMemberships() {
        return new ArrayList<>(memberships);
    }

    public GuildMembership getMembership(String playerId) {
        if (playerId == null || playerId.isEmpty()) {
            return null;
        }
        for (GuildMembership membership : memberships) {
            if (playerId.equals(membership.getPlayerId())) {
                return membership;
            }
        }
        return null;
    }

    public void upsertMembership(GuildMembership membership) {
        if (membership == null || membership.getPlayerId() == null || membership.getPlayerId().isEmpty()) {
            return;
        }
        GuildMembership existing = getMembership(membership.getPlayerId());
        if (existing == null) {
            memberships.add(membership);
            return;
        }
        existing.setRankId(membership.getRankId());
        existing.setActive(membership.isActive());
    }

    private void registerDefaultRanks() {
        registerRank(new GuildRank(RANK_GUILD_MASTER, "Guild Master", PermissionSet.all()));
        registerRank(new GuildRank(
            RANK_OFFICER,
            "Officer",
            PermissionSet.of(
                PermissionAction.POST_QUESTS,
                PermissionAction.MANAGE_QUESTS,
                PermissionAction.CREATE_NPCS,
                PermissionAction.EDIT_NPCS,
                PermissionAction.PUBLISH_CHALLENGES
            )
        ));
        registerRank(new GuildRank(
            RANK_QUARTERMASTER,
            "Quartermaster",
            PermissionSet.of(
                PermissionAction.MANAGE_STORAGE,
                PermissionAction.POST_QUESTS,
                PermissionAction.MANAGE_QUESTS
            )
        ));
        registerRank(new GuildRank(
            RANK_ARCHITECT,
            "Architect",
            PermissionSet.of(
                PermissionAction.BUILD,
                PermissionAction.EDIT_STRUCTURES,
                PermissionAction.REMOVE_STRUCTURES,
                PermissionAction.CLAIM_LAND
            )
        ));
        registerRank(new GuildRank(RANK_MEMBER, "Member", PermissionSet.none()));
        registerRank(new GuildRank(RANK_RECRUIT, "Recruit", PermissionSet.none()));
    }
}
