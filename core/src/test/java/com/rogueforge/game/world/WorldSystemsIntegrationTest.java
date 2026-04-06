package com.rogueforge.game.world;

import com.rogueforge.game.core.GameState;
import com.rogueforge.game.data.StoryEventDefinition;
import com.rogueforge.game.engine.base.BaseState;
import com.rogueforge.game.engine.base.DefenderAssignment;
import com.rogueforge.game.engine.base.DefenderRole;
import com.rogueforge.game.engine.base.PlacedStructure;
import com.rogueforge.game.engine.social.GuildDefinition;
import com.rogueforge.game.support.GdxTestSupport;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Json;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldSystemsIntegrationTest {

    @BeforeAll
    static void bootGdx() {
        GdxTestSupport.init();
    }

    @Test
    void worldStateManagerInitializesKnownFlagsAndSupportsMutations() {
        GameState state = new GameState("Tester");
        WorldStateManager manager = new WorldStateManager();

        manager.initialize(state);

        assertFalse(manager.isFlagActive(state, "access.workshop_pass"));
        manager.setFlag(state, "access.workshop_pass", true);
        assertTrue(manager.isFlagActive(state, "access.workshop_pass"));
    }

    @Test
    void questManagerBootstrapsStartsAndAdvancesQuests() {
        GameState state = new GameState("Tester");
        QuestManager quests = new QuestManager();
        WorldStateManager world = new WorldStateManager();

        quests.startQuest(state, "shard_hunt");
        assertEquals("talk_mira", quests.getQuestState(state, "shard_hunt"));

        quests.startQuest(state, "field_medic");
        assertEquals("speak_iris", quests.getQuestState(state, "field_medic"));

        quests.setQuestStep(state, "field_medic", "recruit_medic");
        state.setWorldStateFlag("recruit.medic_joined", true);
        quests.syncProgress(state, world);

        assertEquals(QuestManager.COMPLETED, quests.getQuestState(state, "field_medic"));
        assertTrue(quests.getCurrentObjective(state).length() > 0);
        assertTrue(quests.getActiveQuestLines(state, false).isEmpty());
    }

    @Test
    void ironhavenArrivalSurveyCompletesAfterTalkingToTownNpcs() {
        GameState state = new GameState("Tester");
        QuestManager quests = new QuestManager();
        WorldStateManager world = new WorldStateManager();

        quests.initialize(state);
        quests.setQuestStep(state, "ironhaven_arrival", "survey_town");

        quests.recordNpcConversation(state, world, "verdant_fields", "mira");
        quests.syncProgress(state, world);
        assertEquals("survey_town", quests.getQuestState(state, "ironhaven_arrival"));

        quests.recordNpcConversation(state, world, "verdant_fields", "tor");
        quests.syncProgress(state, world);
        assertEquals("survey_town", quests.getQuestState(state, "ironhaven_arrival"));

        quests.recordNpcConversation(state, world, "verdant_fields", "edda_town");
        quests.syncProgress(state, world);

        assertTrue(world.isFlagActive(state, "arrival.town_surveyed"));
        assertEquals("return_bram", quests.getQuestState(state, "ironhaven_arrival"));
    }

    @Test
    void dialogueSystemResolvesStatefulNpcDialogueFromRealData() {
        GameState state = new GameState("Tester");
        QuestManager quests = new QuestManager();
        WorldStateManager world = new WorldStateManager();
        DialogueSystem dialogue = new DialogueSystem();
        world.setFlag(state, "arrival.first_battle_won", true);
        world.setFlag(state, "tutorial.frontier_outpost_claimed", true);
        world.setFlag(state, "tutorial.frontier_storage_built", true);
        world.setFlag(state, "tutorial.frontier_outpost_banked", true);
        quests.startQuest(state, "frontier_foothold");
        quests.syncProgress(state, world);
        quests.syncProgress(state, world);
        quests.syncProgress(state, world);

        DialogueSystem.DialogueResult start = dialogue.resolve("mira", "verdant_fields", state, quests, world);
        assertNotNull(start);
        assertEquals("workshop_pass", start.addKeyItem);
        String startPages = start.pages.stream().map(page -> page.text).collect(Collectors.joining(" "));
        assertTrue(startPages.contains("Workshop Pass"));
        assertTrue(startPages.contains("Shadow Caves"));
        world.setFlag(state, "access.workshop_pass", true);

        quests.startQuest(state, "shard_hunt");
        state.setQuestState("shard_hunt", "search_shard");
        DialogueSystem.DialogueResult mid = dialogue.resolve("mira", "verdant_fields", state, quests, world);
        assertNotNull(mid);
        String midPages = mid.pages.stream().map(page -> page.text).collect(Collectors.joining(" "));
        assertTrue(midPages.contains("cave gate"));

        state.setQuestState("shard_hunt", "return_shard");
        DialogueSystem.DialogueResult turnIn = dialogue.resolve("mira", "verdant_fields", state, quests, world);
        assertEquals("peak_sigil", turnIn.addKeyItem);
        assertEquals("shard_hunt", turnIn.completeQuestId);
        assertEquals("frontier.peak_lift_unlocked", turnIn.setWorldFlag);
    }

    @Test
    void miraFirstStepsReturnDoesNotResolveBeforeFrontierBattle() {
        GameState state = new GameState("Tester");
        QuestManager quests = new QuestManager();
        WorldStateManager world = new WorldStateManager();
        DialogueSystem dialogue = new DialogueSystem();

        quests.startQuest(state, "first_steps");
        quests.setQuestStep(state, "first_steps", "verdant_patrol");

        DialogueSystem.DialogueResult beforeBattle = dialogue.resolve("mira", "verdant_fields", state, quests, world);
        if (beforeBattle != null) {
            assertFalse(beforeBattle.text.contains("Back in one piece"));
        }

        world.setFlag(state, "arrival.first_battle_won", true);
        quests.syncProgress(state, world);
        assertEquals("return_mira_intro", quests.getQuestState(state, "first_steps"));
        DialogueSystem.DialogueResult afterBattle = dialogue.resolve("mira", "verdant_fields", state, quests, world);
        assertNotNull(afterBattle);
        assertTrue(afterBattle.text.contains("Back in one piece"));
    }

    @Test
    void bramFrontierAnnexDebriefResolvesAfterShadowCavesSettlementExpansion() {
        GameState state = new GameState("Tester");
        QuestManager quests = new QuestManager();
        WorldStateManager world = new WorldStateManager();
        DialogueSystem dialogue = new DialogueSystem();

        world.setFlag(state, "settlement.frontier_annex", true);

        DialogueSystem.DialogueResult result = dialogue.resolve("bram", "town", state, quests, world);
        assertNotNull(result);
        String pages = result.pages.stream().map(page -> page.text).collect(Collectors.joining(" "));
        assertTrue(pages.contains("east gate road"));
        assertEquals("event.bram_frontier_annex_briefed", result.setWorldFlag);
    }

    @Test
    void annexResupplyQuestAdvancesWhenDepotResupplyFlagIsSet() {
        GameState state = new GameState("Tester");
        QuestManager quests = new QuestManager();
        WorldStateManager world = new WorldStateManager();

        quests.startQuest(state, "annex_resupply");
        quests.setQuestStep(state, "annex_resupply", "bank_supply");
        world.setFlag(state, "event.frontier_annex_resupplied", true);
        quests.syncProgress(state, world);

        assertEquals("return_hale", quests.getQuestState(state, "annex_resupply"));
    }

    @Test
    void forgeCoreRisingDialogueChainHandsOffAcrossActTwoTownSpecialists() {
        GameState state = new GameState("Tester");
        QuestManager quests = new QuestManager();
        WorldStateManager world = new WorldStateManager();
        DialogueSystem dialogue = new DialogueSystem();

        world.setFlag(state, "event.forge_core_lv2_online", true);

        DialogueSystem.DialogueResult bramStart = dialogue.resolve("bram", "town", state, quests, world);
        assertNotNull(bramStart);
        assertEquals("forge_core_rising", bramStart.setQuestId);
        assertEquals("meet_silas", bramStart.setQuestStep);
        assertEquals("event.act2_core_briefed", bramStart.setWorldFlag);

        quests.startQuest(state, bramStart.setQuestId);
        quests.setQuestStep(state, bramStart.setQuestId, bramStart.setQuestStep);
        world.setFlag(state, bramStart.setWorldFlag, true);

        DialogueSystem.DialogueResult silas = dialogue.resolve("master_silas", "town", state, quests, world);
        assertNotNull(silas);
        assertEquals("meet_rex", silas.setQuestStep);
        assertEquals("event.act2_met_silas", silas.setWorldFlag);

        quests.setQuestStep(state, "forge_core_rising", silas.setQuestStep);
        world.setFlag(state, silas.setWorldFlag, true);

        DialogueSystem.DialogueResult rex = dialogue.resolve("commander_rex", "town", state, quests, world);
        assertNotNull(rex);
        assertEquals("meet_cogs", rex.setQuestStep);
        assertEquals("event.act2_met_rex", rex.setWorldFlag);

        quests.setQuestStep(state, "forge_core_rising", rex.setQuestStep);
        world.setFlag(state, rex.setWorldFlag, true);

        DialogueSystem.DialogueResult cogs = dialogue.resolve("professor_cogs", "town", state, quests, world);
        assertNotNull(cogs);
        assertEquals("return_bram", cogs.setQuestStep);
        assertEquals("event.act2_met_cogs", cogs.setWorldFlag);
    }

    @Test
    void recruitmentAndSettlementManagersLoadAndApplyRealDefinitions() {
        RobotRecruitmentManager recruitment = new RobotRecruitmentManager();
        SettlementManager settlements = new SettlementManager();
        List<String> collected = new ArrayList<>(List.of("rust_mk1", "ivy_mk1"));
        List<String> active = new ArrayList<>(List.of("rust_mk1", "ivy_mk1", "bolt_mk1"));

        RobotRecruitmentManager.RecruitmentResult medic = recruitment.apply("medic_join", collected, active);
        RobotRecruitmentManager.RecruitmentResult artificer = recruitment.apply("artificer_join", collected, active);

        assertNotNull(medic);
        assertEquals("medic_mk1", medic.robotId);
        assertEquals("bolt_mk1", medic.replacedRobotId);
        assertTrue(medic.newlyCollected);
        assertTrue(active.contains("medic_mk1"));

        assertNotNull(artificer);
        assertTrue(collected.contains("artificer_mk1"));
        assertFalse(active.contains("artificer_mk1"));
        assertEquals(3, active.size());

        assertNotNull(settlements.get("workshop_tools"));
        assertNotNull(settlements.get("frontier_annex"));
        assertTrue(settlements.getAll().size() >= 4);
        assertEquals("settlement_plan", settlements.get("data_vaults").getRequiredBlueprintFragmentId());
        assertEquals(3, settlements.get("data_vaults").getRequiredBlueprintFragmentCount());
        assertEquals("forge_schema", settlements.get("prototype_lab").getRequiredBlueprintFragmentId());
        assertEquals(4, settlements.get("prototype_lab").getRequiredBlueprintFragmentCount());
    }

    @Test
    void mk2RecruitmentConsumesBlueprintFragmentsWhenAppliedToLiveState() {
        RobotRecruitmentManager recruitment = new RobotRecruitmentManager();
        GameState state = new GameState("Tester");
        List<String> collected = new ArrayList<>(List.of("rust_mk1", "ivy_mk1"));
        List<String> active = new ArrayList<>(List.of("rust_mk1", "ivy_mk1", "bolt_mk1"));

        RobotRecruitmentManager.RecruitmentResult blocked = recruitment.apply("scout_mk2_join", collected, active, state);
        assertNotNull(blocked);
        assertTrue(blocked.blocked);
        assertEquals("bot_chassis_schema", blocked.requiredBlueprintFragmentId);
        assertEquals(2, blocked.requiredBlueprintFragmentCount);

        state.addBlueprintFragment("bot_chassis_schema", 2);
        RobotRecruitmentManager.RecruitmentResult success = recruitment.apply("scout_mk2_join", collected, active, state);
        assertNotNull(success);
        assertFalse(success.blocked);
        assertTrue(collected.contains("scout_mk2"));
        assertEquals(0, state.getBlueprintFragmentCount("bot_chassis_schema"));
    }

    @Test
    void cogsDataVaultDialogueStartsResearchQuestAndNamesFragmentGoal() {
        GameState state = new GameState("Tester");
        QuestManager quests = new QuestManager();
        WorldStateManager world = new WorldStateManager();
        DialogueSystem dialogue = new DialogueSystem();

        world.setFlag(state, "settlement.survey_drones", true);

        DialogueSystem.DialogueResult result = dialogue.resolve("professor_cogs", "town", state, quests, world);
        assertNotNull(result);
        assertEquals("data_vaults", result.setQuestId);
        assertEquals("build_vaults", result.setQuestStep);
        String pages = result.pages.stream().map(page -> page.text).collect(Collectors.joining(" "));
        assertTrue(pages.contains("Settlement Plan Fragments"));
    }

    @Test
    void advancedSettlementDialoguesAssignFollowUpQuestSteps() {
        assertSettlementQuestHook("master_silas", "settlement.workshop_tools", "advanced_alloys", "upgrade_alloys");
        assertSettlementQuestHook("master_silas", "settlement.advanced_alloys", "fusion_forge", "restore_cradle");
        assertSettlementQuestHook("elena_apothecary", "settlement.apothecary_stock", "field_kit_supply", "stock_kits");
        assertSettlementQuestHook("elena_apothecary", "settlement.field_kit_supply", "experimental_compounds", "fund_compounds");
        assertSettlementQuestHook("commander_rex", "settlement.watchtower_network", "relay_expansion", "fund_relay");
        assertSettlementQuestHook("commander_rex", "settlement.relay_expansion", "command_hub", "build_hub");
        assertSettlementQuestHook("professor_cogs", "settlement.survey_drones", "data_vaults", "build_vaults");
        assertSettlementQuestHook("professor_cogs", "settlement.data_vaults", "prototype_lab", "build_lab");
    }

    private void assertSettlementQuestHook(String npcId, String requiredFlag, String questId, String questStep) {
        GameState state = new GameState("Tester");
        QuestManager quests = new QuestManager();
        WorldStateManager world = new WorldStateManager();
        DialogueSystem dialogue = new DialogueSystem();

        world.setFlag(state, requiredFlag, true);

        DialogueSystem.DialogueResult result = dialogue.resolve(npcId, "town", state, quests, world);
        assertNotNull(result, "Expected dialogue for " + npcId + " with " + requiredFlag);
        assertEquals(questId, result.setQuestId, "Quest hook mismatch for " + npcId);
        assertEquals(questStep, result.setQuestStep, "Quest step mismatch for " + npcId);
    }

    @Test
    void shadowCavesBossStoryEventUnlocksFrontierAnnex() {
        StoryEventDefinition[] events = new Json().fromJson(
            StoryEventDefinition[].class,
            Gdx.files.internal("data/story_events.json").readString()
        );

        assertNotNull(events);
        StoryEventDefinition match = null;
        for (StoryEventDefinition event : events) {
            if (event != null && "rusted_sovereign_c".equals(event.getTriggerId())) {
                match = event;
                break;
            }
        }

        assertNotNull(match);
        assertEquals("BOSS_DEFEAT", match.getTriggerType());
        assertEquals("frontier_annex", match.getSettlementUpgradeId());
        assertEquals("frontier.shadow_caves_secured", match.getOnceFlag());
    }

    @Test
    void warPhaseManagerBuildsStrategicSnapshotFromLateGameWorldState() {
        GameState state = new GameState("Tester");
        state.setForgeCoreLevel(4);
        state.setWorldStateFlag("settlement.watchtower_network", true);
        state.setWorldStateFlag("settlement.relay_expansion", true);
        state.setWorldStateFlag("settlement.command_hub", true);
        state.setWorldStateFlag("settlement.hangar_open", true);
        state.setWorldStateFlag("settlement.training_grounds_open", true);
        state.setDefeatedBossIds(List.of(
            "boss_1", "boss_2", "boss_3", "boss_4", "boss_5",
            "boss_6", "boss_7", "boss_8", "boss_9", "boss_10",
            "boss_11", "boss_12", "boss_13", "boss_14", "boss_15"
        ));

        BaseState verdant = new BaseState("verdant_fields");
        verdant.claimSite("frontier_base_site_1");
        verdant.claimSite("frontier_base_site_2");
        verdant.addPlacedStructure(new PlacedStructure(
            "sentry_post_001",
            "sentry_post",
            "verdant_fields",
            "frontier_base_site_1",
            new Rectangle(640f, 360f, 96f, 96f),
            220
        ));
        verdant.addPlacedStructure(new PlacedStructure(
            "supply_crate_001",
            "supply_crate",
            "verdant_fields",
            "frontier_base_site_1",
            new Rectangle(760f, 360f, 96f, 48f),
            160
        ));
        verdant.addPlacedStructure(new PlacedStructure(
            "power_pylon_001",
            "power_pylon",
            "verdant_fields",
            "frontier_base_site_2",
            new Rectangle(880f, 360f, 48f, 96f),
            180
        ));
        verdant.addDefenderAssignment(new DefenderAssignment("sentry_post_001", "scout_mk2", DefenderRole.GUARD));
        verdant.addDefenderAssignment(new DefenderAssignment("sentry_post_001", "guardian_mk2", DefenderRole.PATROL));
        verdant.getRaidState().setActive(true);
        verdant.getRaidState().setThreatLevel(1f);

        BaseState shadow = new BaseState("shadow_caves");
        shadow.claimSite("annex_site");
        shadow.addPlacedStructure(new PlacedStructure(
            "field_fabricator_001",
            "field_fabricator",
            "shadow_caves",
            "annex_site",
            new Rectangle(520f, 420f, 96f, 96f),
            240
        ));
        shadow.getRaidState().setThreatLevel(0.7f);

        GuildDefinition guild = GuildDefinition.createWithDefaultRanks("guild_iron", "Iron Vanguard", "Tester");
        guild.setHallZoneId("verdant_fields");
        guild.setHallClaimedSiteId("frontier_base_site_1");

        WarPhaseManager manager = new WarPhaseManager();
        WarPhaseSnapshot snapshot = manager.buildSnapshot(
            state,
            Map.of("verdant_fields", verdant, "shadow_caves", shadow),
            Map.of("guild_iron", guild),
            "Tester",
            manager.createDefaultFactionInfluence(),
            Map.of("shadow_caves", "rusted_sovereign_c")
        );

        assertTrue(snapshot.isUnlocked());
        assertEquals(3, snapshot.getOutpostsControlled());
        assertEquals(1, snapshot.getActiveRaidCount());
        assertEquals(2, snapshot.getThreatenedOutpostCount());
        assertEquals(2, snapshot.getControlledRegionCount());
        assertEquals(2, snapshot.getContestedRegionCount());
        assertEquals(2, snapshot.getDefenderBotCount());
        assertEquals(1, snapshot.getGuildCount());
        assertEquals(1, snapshot.getPlayerLedGuildCount());
        assertEquals(2, snapshot.getConvoyRouteCount());
        assertEquals(2, snapshot.getLargeExpeditionCount());
        assertEquals(1, snapshot.getPlayerQuestBoardCount());
        assertEquals(3, snapshot.getWorldBossFrontCount());
        assertTrue(snapshot.getTerritoryInfluence() > snapshot.getSettlementAttackRisk());
        assertEquals(3, snapshot.getFactionPressures().size());
        assertTrue(manager.isConvoyEscortRecommended(snapshot, verdant));
        assertTrue(manager.buildConvoyEscortObjective("Verdant Fields", verdant).contains("bank a live haul"));
        assertTrue(manager.isGuildStrikeRecommended(verdant, guild, true, false));
        assertTrue(manager.buildGuildStrikeObjective("Verdant Fields", guild.getDisplayName(), "Rusted Sovereign").contains("strike order"));
        assertTrue(manager.isPublicRecoveryRecommended(shadow, true, false));
        assertTrue(manager.buildPublicRecoveryObjective("Shadow Caves", "Settlement Plan Fragments").contains("recovery sweep"));
        assertTrue(snapshot.getCommandLines().stream().anyMatch(line -> line.contains("World influence")));
        assertTrue(snapshot.getCommandLines().stream().anyMatch(line -> line.contains("Major raids: 1 active")));
        assertTrue(snapshot.getCommandLines().stream().anyMatch(line -> line.contains("Guild fronts: 1 active")));
    }
}
