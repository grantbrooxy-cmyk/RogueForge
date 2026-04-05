package com.rogueforge.game.world;

import com.rogueforge.game.core.GameState;
import com.rogueforge.game.data.StoryEventDefinition;
import com.rogueforge.game.support.GdxTestSupport;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import java.util.ArrayList;
import java.util.List;
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
}
