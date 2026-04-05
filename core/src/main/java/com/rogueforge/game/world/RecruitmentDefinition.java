package com.rogueforge.game.world;

/**
 * Defines a recruitable robot event.
 */
public class RecruitmentDefinition {
    private String eventId;
    private String robotId;
    private int autoDeploySlot = -1;
    private String message;
    private String joinedWorldFlag;
    private String requiredBlueprintFragmentId;
    private int requiredBlueprintFragmentCount;

    public String getEventId() { return eventId; }
    public String getRobotId() { return robotId; }
    public int getAutoDeploySlot() { return autoDeploySlot; }
    public String getMessage() { return message; }
    public String getJoinedWorldFlag() { return joinedWorldFlag; }
    public String getRequiredBlueprintFragmentId() { return requiredBlueprintFragmentId != null ? requiredBlueprintFragmentId : ""; }
    public int getRequiredBlueprintFragmentCount() { return Math.max(0, requiredBlueprintFragmentCount); }
}
