package com.rogueforge.game.engine.base;

/**
 * Reserve-bot assignment to a placed base structure.
 */
public class DefenderAssignment {
    private final String structureInstanceId;
    private final String robotId;
    private final DefenderRole role;

    public DefenderAssignment(String structureInstanceId, String robotId, DefenderRole role) {
        this.structureInstanceId = structureInstanceId;
        this.robotId = robotId;
        this.role = role != null ? role : DefenderRole.GUARD;
    }

    public String getStructureInstanceId() {
        return structureInstanceId;
    }

    public String getRobotId() {
        return robotId;
    }

    public DefenderRole getRole() {
        return role;
    }
}
