package com.rogueforge.game.data;

/**
 * Data definition for lightweight story events triggered by zones or boss defeats.
 */
public class StoryEventDefinition {
    private String id;
    private String triggerType;
    private String triggerId;
    private String onceFlag;
    private String speaker;
    private String text;
    private String setWorldFlag;
    private String addKeyItem;
    private String setQuestId;
    private String setQuestStep;
    private String completeQuestId;
    private String settlementUpgradeId;
    private int rewardExperience;

    public String getId() {
        return id;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getOnceFlag() {
        return onceFlag;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getText() {
        return text;
    }

    public String getSetWorldFlag() {
        return setWorldFlag;
    }

    public String getAddKeyItem() {
        return addKeyItem;
    }

    public String getSetQuestId() {
        return setQuestId;
    }

    public String getSetQuestStep() {
        return setQuestStep;
    }

    public String getCompleteQuestId() {
        return completeQuestId;
    }

    public String getSettlementUpgradeId() {
        return settlementUpgradeId;
    }

    public int getRewardExperience() {
        return rewardExperience;
    }
}
