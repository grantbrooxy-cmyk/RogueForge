package com.rogueforge.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.support.GdxTestSupport;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestDialogueIntegrityTest {

    @BeforeAll
    static void bootGdx() {
        GdxTestSupport.init();
    }

    @Test
    void questDataProvidesAMinimumNetworkDepthAndDialogueLinksStayValid() {
        QuestDefinition[] quests = new Json().fromJson(
            QuestDefinition[].class,
            Gdx.files.internal("data/quests.json").readString()
        );
        DialogueDefinition[] dialogue = new Json().fromJson(
            DialogueDefinition[].class,
            Gdx.files.internal("data/dialogue.json").readString()
        );

        assertNotNull(quests);
        assertNotNull(dialogue);
        assertTrue(quests.length >= 20, "Expected at least 20 quests for a meaningful quest network.");

        Set<String> questIds = Arrays.stream(quests).map(QuestDefinition::getId).collect(Collectors.toSet());
        int threeStepOrMore = 0;
        for (QuestDefinition quest : quests) {
            assertTrue(quest.getSteps().length >= 2, "Quest " + quest.getId() + " should have at least two steps.");
            if (quest.getSteps().length >= 3) {
                threeStepOrMore++;
            }

            Set<String> stepIds = new HashSet<>();
            for (QuestStep step : quest.getSteps()) {
                assertTrue(stepIds.add(step.getId()), "Duplicate step id " + step.getId() + " in quest " + quest.getId());
            }
            assertTrue(stepIds.contains(quest.getStartStepId()), "Quest " + quest.getId() + " start step must exist.");
        }
        assertTrue(threeStepOrMore >= 12, "Expected at least 12 quests with three or more steps.");

        Set<String> allStepIds = Arrays.stream(quests)
            .flatMap(quest -> Arrays.stream(quest.getSteps()))
            .map(QuestStep::getId)
            .collect(Collectors.toSet());

        for (DialogueDefinition entry : dialogue) {
            if (entry.getRequiredQuestId() != null && !entry.getRequiredQuestId().isEmpty()) {
                assertTrue(questIds.contains(entry.getRequiredQuestId()), "Dialogue references missing quest " + entry.getRequiredQuestId());
            }
            if (entry.getSetQuestId() != null && !entry.getSetQuestId().isEmpty()) {
                assertTrue(questIds.contains(entry.getSetQuestId()), "Dialogue sets missing quest " + entry.getSetQuestId());
            }
            if (entry.getCompleteQuestId() != null && !entry.getCompleteQuestId().isEmpty()) {
                assertTrue(questIds.contains(entry.getCompleteQuestId()), "Dialogue completes missing quest " + entry.getCompleteQuestId());
            }
            if (entry.getRequiredQuestState() != null
                && !entry.getRequiredQuestState().isEmpty()
                && !QuestManager.NOT_STARTED.equals(entry.getRequiredQuestState())) {
                assertTrue(allStepIds.contains(entry.getRequiredQuestState()), "Dialogue references missing quest step " + entry.getRequiredQuestState());
            }
            if (entry.getSetQuestStep() != null && !entry.getSetQuestStep().isEmpty()) {
                assertTrue(allStepIds.contains(entry.getSetQuestStep()), "Dialogue sets missing quest step " + entry.getSetQuestStep());
            }
        }
    }

    @Test
    void actAndWorldStateReferencesResolveAcrossQuestDialogueAndSettlementData() {
        QuestDefinition[] quests = new Json().fromJson(
            QuestDefinition[].class,
            Gdx.files.internal("data/quests.json").readString()
        );
        DialogueDefinition[] dialogue = new Json().fromJson(
            DialogueDefinition[].class,
            Gdx.files.internal("data/dialogue.json").readString()
        );
        WorldStateDefinition[] worldState = new Json().fromJson(
            WorldStateDefinition[].class,
            Gdx.files.internal("data/world_state.json").readString()
        );
        SettlementUpgradeDefinition[] upgrades = new Json().fromJson(
            SettlementUpgradeDefinition[].class,
            Gdx.files.internal("data/settlement_upgrades.json").readString()
        );

        assertNotNull(quests);
        assertNotNull(dialogue);
        assertNotNull(worldState);
        assertNotNull(upgrades);

        Set<String> worldFlags = Arrays.stream(worldState)
            .map(WorldStateDefinition::getId)
            .collect(Collectors.toSet());
        Set<String> uniqueWorldFlags = new HashSet<>();
        for (WorldStateDefinition definition : worldState) {
            assertTrue(uniqueWorldFlags.add(definition.getId()), "Duplicate world state flag " + definition.getId());
        }

        Map<String, Integer> tiersByUpgradeId = Arrays.stream(upgrades)
            .collect(Collectors.toMap(SettlementUpgradeDefinition::getId, SettlementUpgradeDefinition::getTier));

        List<String> actTwoMainQuestIds = Arrays.stream(quests)
            .filter(QuestDefinition::isMainQuest)
            .filter(quest -> quest.getAct() == 2)
            .map(QuestDefinition::getId)
            .collect(Collectors.toList());
        assertTrue(actTwoMainQuestIds.contains("frontier_push"));
        assertTrue(actTwoMainQuestIds.contains("deep_signal"));

        List<String> actThreeMainQuestIds = Arrays.stream(quests)
            .filter(QuestDefinition::isMainQuest)
            .filter(quest -> quest.getAct() == 3)
            .map(QuestDefinition::getId)
            .collect(Collectors.toList());
        assertTrue(actThreeMainQuestIds.contains("abyss_archive"));
        assertTrue(actThreeMainQuestIds.contains("fortress_command"));
        assertTrue(actThreeMainQuestIds.contains("void_key"));

        for (QuestDefinition quest : quests) {
            for (QuestStep step : quest.getSteps()) {
                if (step.getCompletionWorldFlag() != null && !step.getCompletionWorldFlag().isEmpty()) {
                    assertTrue(
                        worldFlags.contains(step.getCompletionWorldFlag()),
                        "Quest step references missing world flag " + step.getCompletionWorldFlag()
                    );
                }
            }
        }

        int advancedSettlementQuestHooks = 0;
        for (DialogueDefinition entry : dialogue) {
            if (entry.getRequiredWorldFlag() != null && !entry.getRequiredWorldFlag().isEmpty()) {
                assertTrue(worldFlags.contains(entry.getRequiredWorldFlag()), "Dialogue requires missing world flag " + entry.getRequiredWorldFlag());
            }
            if (entry.getBlockedWorldFlag() != null && !entry.getBlockedWorldFlag().isEmpty()) {
                assertTrue(worldFlags.contains(entry.getBlockedWorldFlag()), "Dialogue blocks on missing world flag " + entry.getBlockedWorldFlag());
            }
            if (entry.getSetWorldFlag() != null && !entry.getSetWorldFlag().isEmpty()) {
                assertTrue(worldFlags.contains(entry.getSetWorldFlag()), "Dialogue sets missing world flag " + entry.getSetWorldFlag());
            }

            if (entry.getSettlementUpgradeId() == null || entry.getSettlementUpgradeId().isEmpty()) {
                continue;
            }
            Integer tier = tiersByUpgradeId.get(entry.getSettlementUpgradeId());
            assertTrue(tier != null, "Dialogue references missing settlement upgrade " + entry.getSettlementUpgradeId());
            if (tier >= 2) {
                assertTrue(
                    entry.getSetQuestId() != null && !entry.getSetQuestId().isEmpty(),
                    "Advanced settlement dialogue should set a quest: " + entry.getId()
                );
                assertTrue(
                    entry.getSetQuestStep() != null && !entry.getSetQuestStep().isEmpty(),
                    "Advanced settlement dialogue should set a quest step: " + entry.getId()
                );
                advancedSettlementQuestHooks++;
            }
        }

        assertTrue(advancedSettlementQuestHooks >= 6, "Expected multiple Act 2 settlement quest hooks.");
    }
}
