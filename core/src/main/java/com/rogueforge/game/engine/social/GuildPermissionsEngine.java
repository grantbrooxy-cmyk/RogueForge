package com.rogueforge.game.engine.social;

/**
 * Reusable permission evaluator for guild-owned actions and future shared-world systems.
 */
public class GuildPermissionsEngine {

    public boolean canPerform(GuildDefinition guild, String playerId, PermissionAction action) {
        if (guild == null || playerId == null || playerId.isEmpty() || action == null) {
            return false;
        }
        GuildMembership membership = guild.getMembership(playerId);
        if (membership == null || !membership.isActive()) {
            return false;
        }
        GuildRank rank = guild.getRank(membership.getRankId());
        return rank != null && rank.getPermissionSet().allows(action);
    }

    public boolean canActOnRecord(OwnershipRecord ownershipRecord, GuildDefinition guild, String playerId, PermissionAction action) {
        if (ownershipRecord == null || action == null || playerId == null || playerId.isEmpty()) {
            return false;
        }
        switch (ownershipRecord.getScope()) {
            case PERSONAL:
                return ownershipRecord.canPlayerEditDirectly(playerId);
            case GUILD:
                return guild != null
                    && ownershipRecord.isOwnedByGuild(guild.getGuildId())
                    && canPerform(guild, playerId, action);
            case PUBLIC:
                return ownershipRecord.isPublicInteractionAllowed();
            case SETTLEMENT:
            default:
                return false;
        }
    }
}
