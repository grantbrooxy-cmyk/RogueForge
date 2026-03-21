package com.rogueforge.game.robot;

/**
 * Static utility class for applying role-based stat bonuses to robots.
 * Each role has specific advantages and disadvantages to create distinct playstyles.
 */
public final class RoleBonus {
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private RoleBonus() {
    }

    /**
     * Applies role-specific bonuses to a robot's base stats.
     * Returns modified stats based on the robot's assigned role.
     *
     * @param robot The robot to apply bonuses for
     * @return A Stats object with role bonuses applied to base values
     */
    public static RobotDefinition.Stats applyRoleBonus(RobotDefinition robot) {
        if (robot == null) {
            return new RobotDefinition.Stats(0, 0, 0, 0);
        }

        int baseHp = robot.getBaseHp();
        int baseAttack = robot.getBaseAttack();
        int baseDefense = robot.getBaseDefense();
        int baseSpeed = robot.getBaseSpeed();

        switch (robot.getRole()) {
            case TANK:    return applyTankBonus(baseHp, baseAttack, baseDefense, baseSpeed);
            case DPS:     return applyDpsBonus(baseHp, baseAttack, baseDefense, baseSpeed);
            case SUPPORT: return applySupportBonus(baseHp, baseAttack, baseDefense, baseSpeed);
            case SCOUT:   return applyScoutBonus(baseHp, baseAttack, baseDefense, baseSpeed);
            default:      return new RobotDefinition.Stats(baseHp, baseAttack, baseDefense, baseSpeed);
        }
    }

    /**
     * TANK: +30% HP, +20% defense, -10% speed
     * Designed to absorb damage and protect allies.
     */
    private static RobotDefinition.Stats applyTankBonus(int hp, int attack, int defense, int speed) {
        int bonusHp = Math.round(hp * 0.30f);
        int bonusDefense = Math.round(defense * 0.20f);
        int speedPenalty = Math.round(speed * 0.10f);

        return new RobotDefinition.Stats(
                hp + bonusHp,
                attack,
                defense + bonusDefense,
                Math.max(1, speed - speedPenalty) // Ensure speed stays positive
        );
    }

    /**
     * DPS: +25% attack, +10% speed, -15% defense
     * Designed for high damage output at the cost of durability.
     */
    private static RobotDefinition.Stats applyDpsBonus(int hp, int attack, int defense, int speed) {
        int bonusAttack = Math.round(attack * 0.25f);
        int bonusSpeed = Math.round(speed * 0.10f);
        int defensePenalty = Math.round(defense * 0.15f);

        return new RobotDefinition.Stats(
                hp,
                attack + bonusAttack,
                Math.max(1, defense - defensePenalty), // Ensure defense stays positive
                speed + bonusSpeed
        );
    }

    /**
     * SUPPORT: +20% HP, heal aura flag (metadata), -10% attack
     * Designed to sustain allies and provide utility.
     * Note: Heal aura is a conceptual flag; actual healing mechanics would be in ability system.
     */
    private static RobotDefinition.Stats applySupportBonus(int hp, int attack, int defense, int speed) {
        int bonusHp = Math.round(hp * 0.20f);
        int attackPenalty = Math.round(attack * 0.10f);

        return new RobotDefinition.Stats(
                hp + bonusHp,
                Math.max(1, attack - attackPenalty), // Ensure attack stays positive
                defense,
                speed
        );
    }

    /**
     * SCOUT: +40% speed, +15% attack, -20% HP
     * Designed for mobility and burst damage.
     */
    private static RobotDefinition.Stats applyScoutBonus(int hp, int attack, int defense, int speed) {
        int speedBonus = Math.round(speed * 0.40f);
        int bonusAttack = Math.round(attack * 0.15f);
        int hpPenalty = Math.round(hp * 0.20f);

        return new RobotDefinition.Stats(
                Math.max(1, hp - hpPenalty), // Ensure HP stays positive
                attack + bonusAttack,
                defense,
                speed + speedBonus
        );
    }

    /**
     * Utility method to get a descriptive string for a role's bonuses.
     *
     * @param role The role enum value
     * @return Human-readable description of role bonuses
     */
    public static String getDescription(RobotDefinition.RoleEnum role) {
        switch (role) {
            case TANK:    return "Tank: +30% HP, +20% Defense, -10% Speed";
            case DPS:     return "DPS: +25% Attack, +10% Speed, -15% Defense";
            case SUPPORT: return "Support: +20% HP, Heal Aura, -10% Attack";
            case SCOUT:   return "Scout: +40% Speed, +15% Attack, -20% HP";
            default:      return "Unknown Role";
        }
    }
}
