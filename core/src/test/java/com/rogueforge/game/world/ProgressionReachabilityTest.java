package com.rogueforge.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.data.StoryEventDefinition;
import com.rogueforge.game.data.ZoneDefinition;
import com.rogueforge.game.engine.world.TmxWorldLoader;
import com.rogueforge.game.support.GdxTestSupport;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionReachabilityTest {
    private static final Set<String> CODE_STARTED_QUESTS = Set.of("workshop_tools");

    @BeforeAll
    static void bootGdx() {
        GdxTestSupport.init();
    }

    @Test
    void everyImplementedZoneIsReachableFromTownAndCanRouteBack() {
        ZoneDefinition[] zones = new Json().fromJson(
            ZoneDefinition[].class,
            Gdx.files.internal("data/zones.json").readString()
        );

        assertNotNull(zones);
        TmxWorldLoader loader = new TmxWorldLoader();
        Map<String, Set<String>> outgoing = new HashMap<>();
        Map<String, Set<String>> incoming = new HashMap<>();

        for (ZoneDefinition zone : zones) {
            TmxWorldLoader.LoadedZone loaded = loader.load(zone);
            outgoing.computeIfAbsent(zone.getId(), ignored -> new HashSet<>());
            incoming.computeIfAbsent(zone.getId(), ignored -> new HashSet<>());

            for (TmxWorldLoader.Door door : loaded.doors) {
                if (door.targetZoneId == null || door.targetZoneId.isEmpty()) {
                    continue;
                }
                outgoing.computeIfAbsent(zone.getId(), ignored -> new HashSet<>()).add(door.targetZoneId);
                incoming.computeIfAbsent(door.targetZoneId, ignored -> new HashSet<>()).add(zone.getId());
            }
        }

        Set<String> expectedZones = new HashSet<>();
        for (ZoneDefinition zone : zones) {
            expectedZones.add(zone.getId());
        }

        assertEquals(expectedZones, traverse("town", outgoing), "Every zone should be reachable from Ironhaven.");
        assertEquals(expectedZones, traverse("town", incoming), "Every zone should have a route back to Ironhaven.");
    }

    @Test
    void everyNonAutoStartQuestHasARealStartSource() {
        QuestDefinition[] quests = new Json().fromJson(
            QuestDefinition[].class,
            Gdx.files.internal("data/quests.json").readString()
        );
        DialogueDefinition[] dialogue = new Json().fromJson(
            DialogueDefinition[].class,
            Gdx.files.internal("data/dialogue.json").readString()
        );
        StoryEventDefinition[] storyEvents = new Json().fromJson(
            StoryEventDefinition[].class,
            Gdx.files.internal("data/story_events.json").readString()
        );

        assertNotNull(quests);
        assertNotNull(dialogue);
        assertNotNull(storyEvents);

        Set<String> dialogueStarted = new HashSet<>();
        for (DialogueDefinition entry : dialogue) {
            if (entry.getSetQuestId() != null && !entry.getSetQuestId().isEmpty()) {
                dialogueStarted.add(entry.getSetQuestId());
            }
        }

        Set<String> storyStarted = new HashSet<>();
        for (StoryEventDefinition event : storyEvents) {
            if (event.getSetQuestId() != null && !event.getSetQuestId().isEmpty()) {
                storyStarted.add(event.getSetQuestId());
            }
        }

        List<String> missingStarts = new ArrayList<>();
        for (QuestDefinition quest : quests) {
            if (quest.isAutoStart()) {
                continue;
            }
            if (dialogueStarted.contains(quest.getId())
                || storyStarted.contains(quest.getId())
                || CODE_STARTED_QUESTS.contains(quest.getId())) {
                continue;
            }
            missingStarts.add(quest.getId());
        }

        assertTrue(
            missingStarts.isEmpty(),
            "These quests have no start source in dialogue, story events, or known code hooks: " + missingStarts
        );
    }

    @Test
    void everyManualQuestStepHasADialogueResolver() {
        QuestDefinition[] quests = new Json().fromJson(
            QuestDefinition[].class,
            Gdx.files.internal("data/quests.json").readString()
        );
        DialogueDefinition[] dialogue = new Json().fromJson(
            DialogueDefinition[].class,
            Gdx.files.internal("data/dialogue.json").readString()
        );
        StoryEventDefinition[] storyEvents = new Json().fromJson(
            StoryEventDefinition[].class,
            Gdx.files.internal("data/story_events.json").readString()
        );

        assertNotNull(quests);
        assertNotNull(dialogue);
        assertNotNull(storyEvents);

        List<String> unresolvedSteps = new ArrayList<>();
        for (QuestDefinition quest : quests) {
            QuestStep[] steps = quest.getSteps();
            for (int i = 0; i < steps.length; i++) {
                QuestStep step = steps[i];
                boolean autoResolved = step.getCompletionWorldFlag() != null && !step.getCompletionWorldFlag().isEmpty()
                    || step.getCompletionKeyItem() != null && !step.getCompletionKeyItem().isEmpty();
                if (autoResolved) {
                    continue;
                }

                boolean startStep = step.getId().equals(quest.getStartStepId());
                boolean hasStartSource = quest.isAutoStart()
                    || CODE_STARTED_QUESTS.contains(quest.getId())
                    || hasQuestStartDialogue(dialogue, quest.getId());

                if (startStep && hasStartSource) {
                    continue;
                }

                if (!hasDialogueResolver(dialogue, quest.getId(), step.getId())
                    && !hasStoryEventResolver(storyEvents, quest.getId())) {
                    unresolvedSteps.add(quest.getId() + ":" + step.getId());
                }
            }
        }

        assertTrue(
            unresolvedSteps.isEmpty(),
            "These manual quest steps have no dialogue resolver and can strand progression: " + unresolvedSteps
        );
    }

    @Test
    void everyZoneHasNarrativeOrMechanicalCoverageInCurrentContent() {
        ZoneDefinition[] zones = new Json().fromJson(
            ZoneDefinition[].class,
            Gdx.files.internal("data/zones.json").readString()
        );
        StoryEventDefinition[] storyEvents = new Json().fromJson(
            StoryEventDefinition[].class,
            Gdx.files.internal("data/story_events.json").readString()
        );
        DialogueDefinition[] dialogue = new Json().fromJson(
            DialogueDefinition[].class,
            Gdx.files.internal("data/dialogue.json").readString()
        );

        assertNotNull(zones);
        assertNotNull(storyEvents);
        assertNotNull(dialogue);

        Set<String> zoneIntroCoverage = new HashSet<>();
        for (StoryEventDefinition event : storyEvents) {
            if ("ZONE_ENTER".equals(event.getTriggerType()) && event.getTriggerId() != null && !event.getTriggerId().isEmpty()) {
                zoneIntroCoverage.add(event.getTriggerId());
            }
        }

        Set<String> dialogueZoneCoverage = new HashSet<>();
        for (DialogueDefinition entry : dialogue) {
            if (entry.getZoneId() != null && !entry.getZoneId().isEmpty()) {
                dialogueZoneCoverage.add(entry.getZoneId());
            }
        }

        List<String> uncovered = new ArrayList<>();
        for (ZoneDefinition zone : zones) {
            if ("town".equals(zone.getId())) {
                continue;
            }
            if (zoneIntroCoverage.contains(zone.getId())
                || dialogueZoneCoverage.contains(zone.getId())
                || (zone.getBossId() != null && !zone.getBossId().isEmpty())) {
                continue;
            }
            uncovered.add(zone.getId());
        }

        assertTrue(
            uncovered.isEmpty(),
            "These zones lack narrative/mechanical coverage hooks in the current data: " + uncovered
        );
    }

    private boolean hasQuestStartDialogue(DialogueDefinition[] dialogue, String questId) {
        for (DialogueDefinition entry : dialogue) {
            if (questId.equals(entry.getSetQuestId())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDialogueResolver(DialogueDefinition[] dialogue, String questId, String stepId) {
        for (DialogueDefinition entry : dialogue) {
            if (!questId.equals(entry.getRequiredQuestId()) || !stepId.equals(entry.getRequiredQuestState())) {
                continue;
            }
            if (entry.getSetQuestStep() != null && !entry.getSetQuestStep().isEmpty() && !stepId.equals(entry.getSetQuestStep())) {
                return true;
            }
            if (questId.equals(entry.getCompleteQuestId())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasStoryEventResolver(StoryEventDefinition[] storyEvents, String questId) {
        for (StoryEventDefinition event : storyEvents) {
            if (questId.equals(event.getCompleteQuestId())) {
                return true;
            }
        }
        return false;
    }

    private Set<String> traverse(String start, Map<String, Set<String>> graph) {
        Set<String> visited = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            if (!visited.add(current)) {
                continue;
            }
            for (String next : graph.getOrDefault(current, Set.of())) {
                if (next != null && !next.isEmpty() && !visited.contains(next)) {
                    queue.addLast(next);
                }
            }
        }

        return visited;
    }
}
