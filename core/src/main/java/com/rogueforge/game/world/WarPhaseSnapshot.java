package com.rogueforge.game.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable strategic snapshot for the Act 4 war-phase world layer.
 */
public class WarPhaseSnapshot {
    private final boolean unlocked;
    private final int outpostsControlled;
    private final int activeRaidCount;
    private final int threatenedOutpostCount;
    private final int controlledRegionCount;
    private final int contestedRegionCount;
    private final int defenderBotCount;
    private final int guildCount;
    private final int playerLedGuildCount;
    private final int convoyRouteCount;
    private final int largeExpeditionCount;
    private final int playerQuestBoardCount;
    private final int worldBossFrontCount;
    private final int territoryInfluence;
    private final int settlementAttackRisk;
    private final List<FactionPressure> factionPressures;
    private final List<String> commandLines;

    public WarPhaseSnapshot(
        boolean unlocked,
        int outpostsControlled,
        int activeRaidCount,
        int threatenedOutpostCount,
        int controlledRegionCount,
        int contestedRegionCount,
        int defenderBotCount,
        int guildCount,
        int playerLedGuildCount,
        int convoyRouteCount,
        int largeExpeditionCount,
        int playerQuestBoardCount,
        int worldBossFrontCount,
        int territoryInfluence,
        int settlementAttackRisk,
        List<FactionPressure> factionPressures,
        List<String> commandLines
    ) {
        this.unlocked = unlocked;
        this.outpostsControlled = Math.max(0, outpostsControlled);
        this.activeRaidCount = Math.max(0, activeRaidCount);
        this.threatenedOutpostCount = Math.max(0, threatenedOutpostCount);
        this.controlledRegionCount = Math.max(0, controlledRegionCount);
        this.contestedRegionCount = Math.max(0, contestedRegionCount);
        this.defenderBotCount = Math.max(0, defenderBotCount);
        this.guildCount = Math.max(0, guildCount);
        this.playerLedGuildCount = Math.max(0, playerLedGuildCount);
        this.convoyRouteCount = Math.max(0, convoyRouteCount);
        this.largeExpeditionCount = Math.max(0, largeExpeditionCount);
        this.playerQuestBoardCount = Math.max(0, playerQuestBoardCount);
        this.worldBossFrontCount = Math.max(0, worldBossFrontCount);
        this.territoryInfluence = clampPercent(territoryInfluence);
        this.settlementAttackRisk = clampPercent(settlementAttackRisk);
        this.factionPressures = factionPressures != null
            ? Collections.unmodifiableList(new ArrayList<>(factionPressures))
            : Collections.emptyList();
        this.commandLines = commandLines != null
            ? Collections.unmodifiableList(new ArrayList<>(commandLines))
            : Collections.emptyList();
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public int getOutpostsControlled() {
        return outpostsControlled;
    }

    public int getActiveRaidCount() {
        return activeRaidCount;
    }

    public int getThreatenedOutpostCount() {
        return threatenedOutpostCount;
    }

    public int getControlledRegionCount() {
        return controlledRegionCount;
    }

    public int getContestedRegionCount() {
        return contestedRegionCount;
    }

    public int getDefenderBotCount() {
        return defenderBotCount;
    }

    public int getGuildCount() {
        return guildCount;
    }

    public int getPlayerLedGuildCount() {
        return playerLedGuildCount;
    }

    public int getConvoyRouteCount() {
        return convoyRouteCount;
    }

    public int getLargeExpeditionCount() {
        return largeExpeditionCount;
    }

    public int getPlayerQuestBoardCount() {
        return playerQuestBoardCount;
    }

    public int getWorldBossFrontCount() {
        return worldBossFrontCount;
    }

    public int getTerritoryInfluence() {
        return territoryInfluence;
    }

    public int getSettlementAttackRisk() {
        return settlementAttackRisk;
    }

    public List<FactionPressure> getFactionPressures() {
        return factionPressures;
    }

    public List<String> getCommandLines() {
        return commandLines;
    }

    public static final class FactionPressure {
        private final String name;
        private final int influence;
        private final String posture;

        public FactionPressure(String name, int influence, String posture) {
            this.name = name != null ? name : "";
            this.influence = clampPercent(influence);
            this.posture = posture != null ? posture : "";
        }

        public String getName() {
            return name;
        }

        public int getInfluence() {
            return influence;
        }

        public String getPosture() {
            return posture;
        }
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
