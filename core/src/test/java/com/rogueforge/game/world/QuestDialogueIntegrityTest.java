package com.rogueforge.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.support.GdxTestSupport;
import java.util.Arrays;
import java.util.HashSet;
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
}
