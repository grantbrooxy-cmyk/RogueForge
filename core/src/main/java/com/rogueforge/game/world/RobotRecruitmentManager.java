package com.rogueforge.game.world;

import com.rogueforge.game.core.GameState;
import com.rogueforge.game.data.DefinitionRegistries;
import com.rogueforge.game.engine.ServiceLifecycle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles robot recruitment events in the world.
 */
public class RobotRecruitmentManager implements ServiceLifecycle {
    private final Map<String, RecruitmentDefinition> definitions = new HashMap<>();

    public RobotRecruitmentManager() {
        reloadDefinitions();
    }

    @Override
    public void initialize() {
        reloadDefinitions();
    }

    public void reloadDefinitions() {
        definitions.clear();
        for (RecruitmentDefinition definition : DefinitionRegistries.RECRUITMENT.getAll()) {
            if (definition != null && definition.getEventId() != null) {
                definitions.put(definition.getEventId(), definition);
            }
        }
    }

    public RecruitmentResult apply(String eventId, List<String> collectedRobotIds, List<String> activeRobotIds) {
        return apply(eventId, collectedRobotIds, activeRobotIds, null);
    }

    public RecruitmentResult apply(String eventId, List<String> collectedRobotIds, List<String> activeRobotIds, GameState gameState) {
        RecruitmentDefinition definition = definitions.get(eventId);
        if (definition == null || definition.getRobotId() == null) {
            return null;
        }
        RecruitmentResult result = new RecruitmentResult();
        result.robotId = definition.getRobotId();
        result.message = definition.getMessage();
        result.joinedWorldFlag = definition.getJoinedWorldFlag();
        result.requiredBlueprintFragmentId = definition.getRequiredBlueprintFragmentId();
        result.requiredBlueprintFragmentCount = definition.getRequiredBlueprintFragmentCount();
        if (gameState != null
            && result.requiredBlueprintFragmentCount > 0
            && !result.requiredBlueprintFragmentId.isEmpty()
            && !gameState.consumeBlueprintFragments(result.requiredBlueprintFragmentId, result.requiredBlueprintFragmentCount)) {
            result.blocked = true;
            return result;
        }
        if (!collectedRobotIds.contains(definition.getRobotId())) {
            collectedRobotIds.add(definition.getRobotId());
            result.newlyCollected = true;
        }
        int slot = definition.getAutoDeploySlot();
        if (slot >= 0 && slot < activeRobotIds.size()) {
            result.replacedRobotId = activeRobotIds.set(slot, definition.getRobotId());
            result.deployed = true;
        }
        return result;
    }

    public static class RecruitmentResult {
        public String robotId;
        public String replacedRobotId;
        public boolean newlyCollected;
        public boolean deployed;
        public String message;
        public String joinedWorldFlag;
        public boolean blocked;
        public String requiredBlueprintFragmentId;
        public int requiredBlueprintFragmentCount;
    }
}
