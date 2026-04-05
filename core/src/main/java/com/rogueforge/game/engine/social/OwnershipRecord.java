package com.rogueforge.game.engine.social;

import java.util.HashSet;
import java.util.Set;

/**
 * Reusable ownership descriptor for player-created world objects.
 */
public class OwnershipRecord {
    private final OwnershipScope scope;
    private final String ownerPlayerId;
    private final String ownerGuildId;
    private final String settlementId;
    private final boolean publicInteractionAllowed;
    private final Set<String> editorPlayerIds = new HashSet<>();

    public OwnershipRecord(
        OwnershipScope scope,
        String ownerPlayerId,
        String ownerGuildId,
        String settlementId,
        boolean publicInteractionAllowed,
        Set<String> editorPlayerIds
    ) {
        this.scope = scope != null ? scope : OwnershipScope.PUBLIC;
        this.ownerPlayerId = ownerPlayerId != null ? ownerPlayerId : "";
        this.ownerGuildId = ownerGuildId != null ? ownerGuildId : "";
        this.settlementId = settlementId != null ? settlementId : "";
        this.publicInteractionAllowed = publicInteractionAllowed;
        if (editorPlayerIds != null) {
            for (String playerId : editorPlayerIds) {
                if (playerId != null && !playerId.isEmpty()) {
                    this.editorPlayerIds.add(playerId);
                }
            }
        }
    }

    public OwnershipScope getScope() {
        return scope;
    }

    public String getOwnerPlayerId() {
        return ownerPlayerId;
    }

    public String getOwnerGuildId() {
        return ownerGuildId;
    }

    public String getSettlementId() {
        return settlementId;
    }

    public boolean isPublicInteractionAllowed() {
        return publicInteractionAllowed;
    }

    public Set<String> getEditorPlayerIds() {
        return new HashSet<>(editorPlayerIds);
    }

    public boolean isOwnedByPlayer(String playerId) {
        return playerId != null && !playerId.isEmpty() && playerId.equals(ownerPlayerId);
    }

    public boolean isOwnedByGuild(String guildId) {
        return guildId != null && !guildId.isEmpty() && guildId.equals(ownerGuildId);
    }

    public boolean canPlayerEditDirectly(String playerId) {
        return isOwnedByPlayer(playerId) || (playerId != null && editorPlayerIds.contains(playerId));
    }
}
