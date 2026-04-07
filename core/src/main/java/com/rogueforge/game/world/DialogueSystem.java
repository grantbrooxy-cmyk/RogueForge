package com.rogueforge.game.world;

import com.rogueforge.game.core.GameState;
import com.rogueforge.game.data.DefinitionRegistries;
import com.rogueforge.game.engine.ServiceLifecycle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Resolves dialogue content from JSON based on current game state.
 */
public class DialogueSystem implements ServiceLifecycle {
    private final List<DialogueDefinition> definitions = new ArrayList<>();

    public DialogueSystem() {
        reloadDefinitions();
    }

    @Override
    public void initialize() {
        reloadDefinitions();
    }

    public void reloadDefinitions() {
        definitions.clear();
        for (DialogueDefinition definition : DefinitionRegistries.DIALOGUE.getAll()) {
            if (definition != null) {
                definitions.add(definition);
            }
        }
        definitions.sort(Comparator.comparingInt(DialogueDefinition::getPriority).reversed());
    }

    public DialogueResult resolve(String npcId, String zoneId, GameState state,
                                  QuestManager questManager, WorldStateManager worldStateManager) {
        for (DialogueDefinition definition : definitions) {
            if (!matches(definition, npcId, zoneId, state, questManager, worldStateManager)) {
                continue;
            }
            return toResult(definition);
        }
        return null;
    }

    private boolean matches(DialogueDefinition definition, String npcId, String zoneId, GameState state,
                            QuestManager questManager, WorldStateManager worldStateManager) {
        if (definition.getNpcId() == null || !definition.getNpcId().equals(npcId)) {
            return false;
        }
        if (definition.getZoneId() != null && !definition.getZoneId().isEmpty()
            && (zoneId == null || !definition.getZoneId().equals(zoneId))) {
            return false;
        }
        if (definition.getRequiredQuestId() != null && !definition.getRequiredQuestId().isEmpty()) {
            String current = questManager != null ? questManager.getQuestState(state, definition.getRequiredQuestId()) : QuestManager.NOT_STARTED;
            if (definition.isRequiredQuestCompleted()) {
                if (!QuestManager.COMPLETED.equals(current)) {
                    return false;
                }
            } else if (definition.getRequiredQuestState() != null && !definition.getRequiredQuestState().isEmpty()) {
                if (!definition.getRequiredQuestState().equals(current)) {
                    return false;
                }
            }
        }
        if (definition.getRequiredWorldFlag() != null && !definition.getRequiredWorldFlag().isEmpty()
            && (worldStateManager == null || !worldStateManager.isFlagActive(state, definition.getRequiredWorldFlag()))) {
            return false;
        }
        if (definition.getBlockedWorldFlag() != null && !definition.getBlockedWorldFlag().isEmpty()
            && worldStateManager != null && worldStateManager.isFlagActive(state, definition.getBlockedWorldFlag())) {
            return false;
        }
        if (definition.getRequiredKeyItem() != null && !definition.getRequiredKeyItem().isEmpty()
            && (state == null || !state.hasKeyItem(definition.getRequiredKeyItem()))) {
            return false;
        }
        return true;
    }

    private DialogueResult toResult(DialogueDefinition definition) {
        DialogueResult result = new DialogueResult();
        result.dialogueId = definition.getId();
        DialogueLine[] lines = definition.getLines();
        for (DialogueLine line : lines) {
            if (line == null || line.getText() == null || line.getText().isEmpty()) {
                continue;
            }
            String speaker = line.getSpeaker() != null && !line.getSpeaker().isEmpty()
                ? line.getSpeaker()
                : definition.getNpcId();
            result.pages.add(new DialoguePage(speaker, line.getText()));
        }
        if (!result.pages.isEmpty()) {
            result.speaker = result.pages.get(0).speaker;
            result.text = result.pages.get(0).text;
        } else {
            result.speaker = definition.getNpcId();
            result.text = "";
        }
        result.rewardGold = definition.getRewardGold();
        result.rewardExperience = definition.getRewardExperience();
        result.rewardPotions = definition.getRewardPotions();
        result.addKeyItem = definition.getAddKeyItem();
        result.setQuestId = definition.getSetQuestId();
        result.setQuestStep = definition.getSetQuestStep();
        result.completeQuestId = definition.getCompleteQuestId();
        result.setWorldFlag = definition.getSetWorldFlag();
        result.recruitEventId = definition.getRecruitEventId();
        result.settlementUpgradeId = definition.getSettlementUpgradeId();
        return result;
    }

    public static class DialogueResult {
        public String dialogueId;
        public String speaker;
        public String text;
        public final List<DialoguePage> pages = new ArrayList<>();
        public long rewardGold;
        public int rewardExperience;
        public int rewardPotions;
        public String addKeyItem;
        public String setQuestId;
        public String setQuestStep;
        public String completeQuestId;
        public String setWorldFlag;
        public String recruitEventId;
        public String settlementUpgradeId;
    }

    public static class DialoguePage {
        public final String speaker;
        public final String text;

        public DialoguePage(String speaker, String text) {
            this.speaker = speaker != null ? speaker : "";
            this.text = text != null ? text : "";
        }
    }
}
