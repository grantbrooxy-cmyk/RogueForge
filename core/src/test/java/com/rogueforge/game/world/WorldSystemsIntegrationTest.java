package com.rogueforge.game.world;

import com.rogueforge.game.core.GameState;
import com.rogueforge.game.support.GdxTestSupport;
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

        DialogueSystem.DialogueResult start = dialogue.resolve("mira", "verdant_fields", state, quests, world);
        assertNotNull(start);
        assertEquals("workshop_pass", start.addKeyItem);
        String startPages = start.pages.stream().map(page -> page.text).collect(Collectors.joining(" "));
        assertTrue(startPages.contains("Workshop Pass"));

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
        assertTrue(settlements.getAll().size() >= 4);
    }
}
