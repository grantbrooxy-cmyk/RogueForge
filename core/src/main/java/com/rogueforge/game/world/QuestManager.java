package com.rogueforge.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.core.GameState;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages structured quest progress and current objectives.
 */
public class QuestManager {
    public static final String NOT_STARTED = "NOT_STARTED";
    public static final String COMPLETED = "__COMPLETED__";
    private static final String IRONHAVEN_ARRIVAL_QUEST = "ironhaven_arrival";
    private static final String IRONHAVEN_SURVEY_STEP = "survey_town";
    private static final String IRONHAVEN_SURVEY_COMPLETE_FLAG = "arrival.town_surveyed";

    private final Map<String, QuestDefinition> definitions = new LinkedHashMap<>();

    public QuestManager() {
        loadDefinitions();
    }

    private void loadDefinitions() {
        QuestDefinition[] loaded = new Json().fromJson(
            QuestDefinition[].class,
            Gdx.files.internal("data/quests.json").readString()
        );
        if (loaded == null) {
            return;
        }
        for (QuestDefinition definition : loaded) {
            if (definition != null && definition.getId() != null) {
                definitions.put(definition.getId(), definition);
            }
        }
    }

    public void initialize(GameState state) {
        if (state == null) {
            return;
        }
        for (QuestDefinition definition : definitions.values()) {
            if (definition.isAutoStart() && NOT_STARTED.equals(getQuestState(state, definition.getId()))) {
                startQuest(state, definition.getId());
            }
        }
    }

    public String getQuestState(GameState state, String questId) {
        if (state == null || questId == null || questId.isEmpty()) {
            return NOT_STARTED;
        }
        return state.getQuestState(questId);
    }

    public boolean isQuestCompleted(GameState state, String questId) {
        return COMPLETED.equals(getQuestState(state, questId));
    }

    public void startQuest(GameState state, String questId) {
        QuestDefinition definition = definitions.get(questId);
        if (state == null || definition == null) {
            return;
        }
        String current = getQuestState(state, questId);
        if (!NOT_STARTED.equals(current)) {
            return;
        }
        state.setQuestState(questId, definition.getStartStepId());
    }

    public void setQuestStep(GameState state, String questId, String stepId) {
        if (state == null || questId == null || stepId == null || stepId.isEmpty()) {
            return;
        }
        state.setQuestState(questId, stepId);
    }

    public void completeQuest(GameState state, String questId) {
        if (state != null && questId != null && !questId.isEmpty()) {
            state.setQuestState(questId, COMPLETED);
        }
    }

    public void recordNpcConversation(GameState state, WorldStateManager worldStateManager, String zoneId, String npcId) {
        if (state == null || worldStateManager == null || npcId == null || npcId.isEmpty()) {
            return;
        }
        if (!IRONHAVEN_SURVEY_STEP.equals(getQuestState(state, IRONHAVEN_ARRIVAL_QUEST))) {
            return;
        }
        if (!"town".equals(zoneId) && !"verdant_fields".equals(zoneId)) {
            return;
        }

        String surveyFlag = getIronhavenSurveyFlag(npcId);
        if (surveyFlag == null) {
            return;
        }

        worldStateManager.setFlag(state, surveyFlag, true);
        if (worldStateManager.isFlagActive(state, "arrival.spoke_mira")
            && worldStateManager.isFlagActive(state, "arrival.spoke_tor")
            && worldStateManager.isFlagActive(state, "arrival.spoke_edda")) {
            worldStateManager.setFlag(state, IRONHAVEN_SURVEY_COMPLETE_FLAG, true);
        }
    }

    private String getIronhavenSurveyFlag(String npcId) {
        if ("mira".equals(npcId)) {
            return "arrival.spoke_mira";
        }
        if ("tor".equals(npcId)) {
            return "arrival.spoke_tor";
        }
        if ("edda_town".equals(npcId)) {
            return "arrival.spoke_edda";
        }
        return null;
    }

    public void syncProgress(GameState state, WorldStateManager worldStateManager) {
        if (state == null) {
            return;
        }
        for (QuestDefinition definition : definitions.values()) {
            String current = getQuestState(state, definition.getId());
            if (NOT_STARTED.equals(current) || COMPLETED.equals(current)) {
                continue;
            }
            QuestStep[] steps = definition.getSteps();
            for (int i = 0; i < steps.length; i++) {
                QuestStep step = steps[i];
                if (!current.equals(step.getId())) {
                    continue;
                }
                boolean complete = false;
                if (step.getCompletionKeyItem() != null && !step.getCompletionKeyItem().isEmpty()
                    && state.hasKeyItem(step.getCompletionKeyItem())) {
                    complete = true;
                }
                if (step.getCompletionWorldFlag() != null && !step.getCompletionWorldFlag().isEmpty()
                    && worldStateManager != null && worldStateManager.isFlagActive(state, step.getCompletionWorldFlag())) {
                    complete = true;
                }
                if (complete) {
                    if (i + 1 < steps.length) {
                        state.setQuestState(definition.getId(), steps[i + 1].getId());
                    } else {
                        completeQuest(state, definition.getId());
                    }
                }
                break;
            }
        }
    }

    public String getCurrentObjective(GameState state) {
        for (QuestDefinition definition : definitions.values()) {
            if (!definition.isMainQuest()) {
                continue;
            }
            String current = getQuestState(state, definition.getId());
            if (NOT_STARTED.equals(current) || COMPLETED.equals(current)) {
                continue;
            }
            for (QuestStep step : definition.getSteps()) {
                if (current.equals(step.getId())) {
                    return step.getObjective();
                }
            }
        }
        for (QuestDefinition definition : definitions.values()) {
            if (definition.isMainQuest()) {
                continue;
            }
            String current = getQuestState(state, definition.getId());
            if (NOT_STARTED.equals(current) || COMPLETED.equals(current)) {
                continue;
            }
            for (QuestStep step : definition.getSteps()) {
                if (current.equals(step.getId())) {
                    return step.getObjective();
                }
            }
        }
        return "Explore the frontier and strengthen your crew.";
    }

    public List<String> getActiveQuestLines(GameState state, boolean mainQuestsOnly) {
        List<String> lines = new ArrayList<>();
        for (QuestDefinition definition : definitions.values()) {
            if (mainQuestsOnly != definition.isMainQuest()) {
                continue;
            }
            String current = getQuestState(state, definition.getId());
            if (NOT_STARTED.equals(current) || COMPLETED.equals(current)) {
                continue;
            }
            for (QuestStep step : definition.getSteps()) {
                if (current.equals(step.getId())) {
                    lines.add(definition.getTitle() + ": " + step.getObjective());
                    break;
                }
            }
        }
        return lines;
    }
}
