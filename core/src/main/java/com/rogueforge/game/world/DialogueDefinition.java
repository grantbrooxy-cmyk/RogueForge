package com.rogueforge.game.world;

/**
 * A conditional dialogue entry with optional gameplay actions.
 */
public class DialogueDefinition {
    private String id;
    private String npcId;
    private int priority;
    private String zoneId;
    private String requiredQuestId;
    private String requiredQuestState;
    private boolean requiredQuestCompleted;
    private String requiredWorldFlag;
    private String blockedWorldFlag;
    private String requiredKeyItem;
    private DialogueLine[] lines;
    private long rewardGold;
    private int rewardExperience;
    private int rewardPotions;
    private String addKeyItem;
    private String setQuestId;
    private String setQuestStep;
    private String completeQuestId;
    private String setWorldFlag;
    private String recruitEventId;
    private String settlementUpgradeId;

    public String getId() { return id; }
    public String getNpcId() { return npcId; }
    public int getPriority() { return priority; }
    public String getZoneId() { return zoneId; }
    public String getRequiredQuestId() { return requiredQuestId; }
    public String getRequiredQuestState() { return requiredQuestState; }
    public boolean isRequiredQuestCompleted() { return requiredQuestCompleted; }
    public String getRequiredWorldFlag() { return requiredWorldFlag; }
    public String getBlockedWorldFlag() { return blockedWorldFlag; }
    public String getRequiredKeyItem() { return requiredKeyItem; }
    public DialogueLine[] getLines() { return lines != null ? lines : new DialogueLine[0]; }
    public long getRewardGold() { return rewardGold; }
    public int getRewardExperience() { return rewardExperience; }
    public int getRewardPotions() { return rewardPotions; }
    public String getAddKeyItem() { return addKeyItem; }
    public String getSetQuestId() { return setQuestId; }
    public String getSetQuestStep() { return setQuestStep; }
    public String getCompleteQuestId() { return completeQuestId; }
    public String getSetWorldFlag() { return setWorldFlag; }
    public String getRecruitEventId() { return recruitEventId; }
    public String getSettlementUpgradeId() { return settlementUpgradeId; }
}
