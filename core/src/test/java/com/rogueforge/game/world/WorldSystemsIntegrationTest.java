package com.rogueforge.game.world;

import com.rogueforge.game.core.GameState;
import com.rogueforge.game.support.GdxTestSupport;
import java.util.ArrayList;
import java.util.List;
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

        state.setQuestFlag("quest_shard_started", true);
        quests.initialize(state);
        assertEquals("search_shard", quests.getQuestState(state, "shard_hunt"));

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
    void dialogueSystemResolvesStatefulNpcDialogueFromRealData() {
        GameState state = new GameState("Tester");
        QuestManager quests = new QuestManager();
        WorldStateManager world = new WorldStateManager();
        DialogueSystem dialogue = new DialogueSystem();

        DialogueSystem.DialogueResult start = dialogue.resolve("mira", "verdant_fields", state, quests, world);
        assertNotNull(start);
        assertTrue(start.text.contains("Workshop Pass"));

        quests.startQuest(state, "shard_hunt");
        state.setQuestState("shard_hunt", "search_shard");
        DialogueSystem.DialogueResult mid = dialogue.resolve("mira", "verdant_fields", state, quests, world);
        assertNotNull(mid);
        assertTrue(mid.text.contains("cave gate"));

        state.setQuestState("shard_hunt", "return_shard");
        DialogueSystem.DialogueResult turnIn = dialogue.resolve("mira", "verdant_fields", state, quests, world);
        assertEquals("peak_sigil", turnIn.addKeyItem);
        assertEquals("shard_hunt", turnIn.completeQuestId);
        assertEquals("frontier.peak_lift_unlocked", turnIn.setWorldFlag);
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
        assertTrue(active.contains("artificer_mk1"));

        assertNotNull(settlements.get("workshop_tools"));
        assertTrue(settlements.getAll().size() >= 4);
    }
}
