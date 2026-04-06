package com.rogueforge.game.entity.component;

/**
 * Robot-specific identity and deployment data.
 */
public class RobotCoreComponent {
    public String robotId;
    public String robotName;
    public String role;
    public String grade;
    public boolean deployed = true;
    public int slotIndex = -1;
}
