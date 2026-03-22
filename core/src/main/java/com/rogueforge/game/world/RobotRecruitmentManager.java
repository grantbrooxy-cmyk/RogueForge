package com.rogueforge.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles robot recruitment events in the world.
 */
public class RobotRecruitmentManager {
    private final Map<String, RecruitmentDefinition> definitions = new HashMap<>();

    public RobotRecruitmentManager() {
        RecruitmentDefinition[] loaded = new Json().fromJson(
            RecruitmentDefinition[].class,
            Gdx.files.internal("data/recruitment.json").readString()
        );
        if (loaded != null) {
            for (RecruitmentDefinition definition : loaded) {
                if (definition != null && definition.getEventId() != null) {
                    definitions.put(definition.getEventId(), definition);
                }
            }
        }
    }

    public RecruitmentResult apply(String eventId, List<String> collectedRobotIds, List<String> activeRobotIds) {
        RecruitmentDefinition definition = definitions.get(eventId);
        if (definition == null || definition.getRobotId() == null) {
            return null;
        }
        RecruitmentResult result = new RecruitmentResult();
        result.robotId = definition.getRobotId();
        result.message = definition.getMessage();
        result.joinedWorldFlag = definition.getJoinedWorldFlag();
        if (!collectedRobotIds.contains(definition.getRobotId())) {
            collectedRobotIds.add(definition.getRobotId());
            result.newlyCollected = true;
        }
        int slot = definition.getAutoDeploySlot();
        if (slot >= 0 && slot < activeRobotIds.size()) {
            result.replacedRobotId = activeRobotIds.set(slot, definition.getRobotId());
            result.deployed = true;
        } else if (!activeRobotIds.contains(definition.getRobotId())) {
            activeRobotIds.add(definition.getRobotId());
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
    }
}
