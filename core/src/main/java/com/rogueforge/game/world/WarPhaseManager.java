package com.rogueforge.game.world;

import com.rogueforge.game.core.GameState;
import com.rogueforge.game.engine.base.BaseRaidState;
import com.rogueforge.game.engine.base.BaseState;
import com.rogueforge.game.engine.base.DefenderAssignment;
import com.rogueforge.game.engine.base.PlacedStructure;
import com.rogueforge.game.engine.base.StructureCategory;
import com.rogueforge.game.engine.base.StructureDefinition;
import com.rogueforge.game.engine.base.StructureDefinitionRegistry;
import com.rogueforge.game.engine.social.GuildDefinition;
import com.rogueforge.game.engine.social.GuildMembership;
import com.rogueforge.game.engine.social.PermissionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Builds a strategic Act 4 snapshot from persistent world systems.
 */
public class WarPhaseManager {
    private static final float THREATENED_OUTPOST_LEVEL = 0.55f;

    private final StructureDefinitionRegistry structureRegistry = new StructureDefinitionRegistry();

    public boolean isWarPhaseUnlocked(GameState state) {
        if (state == null) {
            return false;
        }
        return state.getForgeCoreLevel() >= 4
            || state.isWorldStateFlagActive("settlement.command_hub")
            || state.getDefeatedBossCount() >= 15;
    }

    public WarPhaseSnapshot buildSnapshot(
        GameState state,
        Map<String, BaseState> baseStatesByZoneId,
        Map<String, GuildDefinition> guildsById,
        String playerId
    ) {
        boolean unlocked = isWarPhaseUnlocked(state);
        Map<String, BaseState> bases = baseStatesByZoneId != null ? baseStatesByZoneId : Collections.emptyMap();
        Map<String, GuildDefinition> guilds = guildsById != null ? guildsById : Collections.emptyMap();

        int outpostsControlled = 0;
        int activeRaidCount = 0;
        int threatenedOutposts = 0;
        int controlledRegions = 0;
        int contestedRegions = 0;
        int defenderBots = 0;
        int logisticsStructures = 0;
        int defenseStructures = 0;
        int intactStructures = 0;

        for (BaseState baseState : bases.values()) {
            if (baseState == null) {
                continue;
            }
            boolean controlled = !baseState.getClaimedSiteIds().isEmpty();
            if (controlled) {
                outpostsControlled += baseState.getClaimedSiteIds().size();
                controlledRegions++;
            }

            BaseRaidState raidState = baseState.getRaidState();
            boolean threatened = false;
            if (raidState != null) {
                if (raidState.isActive()) {
                    activeRaidCount++;
                    threatened = true;
                } else if (raidState.getThreatLevel() >= THREATENED_OUTPOST_LEVEL) {
                    threatened = true;
                }
            }
            if (threatened) {
                threatenedOutposts++;
                contestedRegions++;
            }

            defenderBots += countDefenderBots(baseState);
            for (PlacedStructure structure : baseState.getPlacedStructures()) {
                if (structure == null || !structure.isActive() || structure.getCurrentHitPoints() <= 0) {
                    continue;
                }
                intactStructures++;
                StructureDefinition definition = structureRegistry.get(structure.getStructureDefinitionId());
                if (definition == null) {
                    continue;
                }
                if (definition.getCategory() == StructureCategory.DEFENSE || definition.getCategory() == StructureCategory.WALL) {
                    defenseStructures++;
                }
                if (definition.getCategory() == StructureCategory.STORAGE
                    || definition.getCategory() == StructureCategory.CRAFTING
                    || definition.getCategory() == StructureCategory.POWER
                    || definition.getCategory() == StructureCategory.UTILITY) {
                    logisticsStructures++;
                }
            }
        }

        int guildCount = 0;
        int playerLedGuildCount = 0;
        int playerQuestBoards = 0;
        for (GuildDefinition guild : guilds.values()) {
            if (guild == null) {
                continue;
            }
            guildCount++;
            GuildMembership membership = playerId != null ? guild.getMembership(playerId) : null;
            boolean playerLeads = membership != null
                && membership.isActive()
                && GuildDefinition.RANK_GUILD_MASTER.equals(membership.getRankId());
            if (playerLeads) {
                playerLedGuildCount++;
            }
            if (guild.getHallClaimedSiteId() != null && !guild.getHallClaimedSiteId().isEmpty()) {
                playerQuestBoards++;
            }
        }

        boolean watchtowerNetwork = state != null && state.isWorldStateFlagActive("settlement.watchtower_network");
        boolean relayExpansion = state != null && state.isWorldStateFlagActive("settlement.relay_expansion");
        boolean commandHub = state != null && state.isWorldStateFlagActive("settlement.command_hub");
        boolean hangarOnline = state != null && state.isWorldStateFlagActive("settlement.hangar_open");
        boolean trainingGrounds = state != null && state.isWorldStateFlagActive("settlement.training_grounds_open");

        int convoyRoutes = Math.max(0, Math.min(Math.max(0, outpostsControlled - 1), logisticsStructures + upgradeScore(watchtowerNetwork, relayExpansion, commandHub)));
        int largeExpeditions = unlocked
            ? Math.max(1, Math.min(4, controlledRegions + (commandHub ? 1 : 0) + (hangarOnline ? 1 : 0) - contestedRegions))
            : 0;
        int worldBossFronts = unlocked
            ? Math.max(1, Math.min(4, 1 + contestedRegions + Math.max(0, (state != null ? state.getDefeatedBossCount() : 0) - 15) / 5))
            : 0;

        int territoryInfluence = clampPercent(
            outpostsControlled * 10
                + controlledRegions * 8
                + defenderBots * 4
                + logisticsStructures * 4
                + defenseStructures * 3
                + upgradeScore(watchtowerNetwork, relayExpansion, commandHub) * 8
                - activeRaidCount * 12
                - contestedRegions * 8
        );

        int settlementAttackRisk = clampPercent(
            activeRaidCount * 24
                + threatenedOutposts * 14
                + contestedRegions * 10
                + Math.max(0, worldBossFronts - 1) * 8
                - (watchtowerNetwork ? 8 : 0)
                - (relayExpansion ? 10 : 0)
                - (commandHub ? 12 : 0)
                - (trainingGrounds ? 6 : 0)
        );

        List<WarPhaseSnapshot.FactionPressure> factions = new ArrayList<>();
        factions.add(new WarPhaseSnapshot.FactionPressure(
            "Ironhaven Command",
            clampPercent(30 + upgradeScore(watchtowerNetwork, relayExpansion, commandHub, hangarOnline, trainingGrounds) * 12 + defenderBots * 3),
            settlementAttackRisk >= 55 ? "mobilizing" : "holding"
        ));
        factions.add(new WarPhaseSnapshot.FactionPressure(
            "Guild Coalition",
            clampPercent(guildCount * 18 + playerQuestBoards * 10 + playerLedGuildCount * 14),
            guildCount == 0 ? "forming" : (playerQuestBoards > 0 ? "contracting" : "assembling")
        ));
        factions.add(new WarPhaseSnapshot.FactionPressure(
            "Frontier Hostiles",
            clampPercent(18 + activeRaidCount * 22 + threatenedOutposts * 12 + worldBossFronts * 10),
            activeRaidCount > 0 ? "raiding" : (contestedRegions > 0 ? "probing" : "scattered")
        ));

        List<String> commandLines = buildCommandLines(
            unlocked,
            outpostsControlled,
            activeRaidCount,
            threatenedOutposts,
            controlledRegions,
            contestedRegions,
            defenderBots,
            guildCount,
            playerLedGuildCount,
            convoyRoutes,
            largeExpeditions,
            playerQuestBoards,
            worldBossFronts,
            territoryInfluence,
            settlementAttackRisk,
            factions
        );

        return new WarPhaseSnapshot(
            unlocked,
            outpostsControlled,
            activeRaidCount,
            threatenedOutposts,
            controlledRegions,
            contestedRegions,
            defenderBots,
            guildCount,
            playerLedGuildCount,
            convoyRoutes,
            largeExpeditions,
            playerQuestBoards,
            worldBossFronts,
            territoryInfluence,
            settlementAttackRisk,
            factions,
            commandLines
        );
    }

    private int countDefenderBots(BaseState baseState) {
        int count = 0;
        for (DefenderAssignment assignment : baseState.getDefenderAssignments()) {
            if (assignment != null && assignment.getRobotId() != null && !assignment.getRobotId().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private List<String> buildCommandLines(
        boolean unlocked,
        int outpostsControlled,
        int activeRaidCount,
        int threatenedOutposts,
        int controlledRegions,
        int contestedRegions,
        int defenderBots,
        int guildCount,
        int playerLedGuildCount,
        int convoyRoutes,
        int largeExpeditions,
        int playerQuestBoards,
        int worldBossFronts,
        int territoryInfluence,
        int settlementAttackRisk,
        List<WarPhaseSnapshot.FactionPressure> factions
    ) {
        List<String> lines = new ArrayList<>();
        if (!unlocked) {
            lines.add("Act 4 gate: bring the command hub online or push Forge Core resonance to Lv4.");
            lines.add("War systems scale from claimed outposts, defender networks, guild halls, and late-game logistics.");
            return lines;
        }

        lines.add("World influence " + territoryInfluence + "%  |  Settlement attack risk " + settlementAttackRisk + "%.");
        lines.add("Outposts held: " + outpostsControlled + " across " + controlledRegions + " controlled regions. Contested regions: " + contestedRegions + ".");
        lines.add("Major raids: " + activeRaidCount + " active  |  Threatened outposts: " + threatenedOutposts + "  |  Defender bots on station: " + defenderBots + ".");
        lines.add("Convoy escorts available: " + convoyRoutes + "  |  Large dungeon expeditions staged: " + largeExpeditions + ".");
        lines.add("Guild fronts: " + guildCount + " active"
            + (playerLedGuildCount > 0 ? " with " + playerLedGuildCount + " under your command." : "."));
        lines.add("Player-created quest boards online: " + playerQuestBoards + "  |  World boss fronts detected: " + worldBossFronts + ".");

        for (WarPhaseSnapshot.FactionPressure faction : factions) {
            lines.add(faction.getName() + ": " + faction.getInfluence() + "% pressure, posture " + faction.getPosture() + ".");
        }

        if (activeRaidCount > 0) {
            lines.add("Priority: repel current raids before extending territory or opening new expeditions.");
        } else if (settlementAttackRisk >= 55) {
            lines.add("Priority: reinforce convoy routes and defender coverage before the next major strike lands.");
        } else if (playerQuestBoards == 0 && guildCount > 0) {
            lines.add("Priority: establish a guild hall board so contracts and defense orders can circulate across your territory.");
        } else if (convoyRoutes == 0 && outpostsControlled > 1) {
            lines.add("Priority: connect your outposts with logistics structures to unlock convoy escort pressure relief.");
        } else {
            lines.add("Priority: push the frontier. Your network can support new contracts, boss hunts, and deep expeditions.");
        }
        return lines;
    }

    private int upgradeScore(boolean... values) {
        int count = 0;
        if (values == null) {
            return 0;
        }
        for (boolean value : values) {
            if (value) {
                count++;
            }
        }
        return count;
    }

    private int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
