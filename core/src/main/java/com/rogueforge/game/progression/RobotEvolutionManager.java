package com.rogueforge.game.progression;

/**
 * Handles robot level progression and forge-gated evolution tiers.
 */
public final class RobotEvolutionManager {
    private RobotEvolutionManager() {
    }

    public static int addExperience(RobotProgressionState state, int amount) {
        if (state == null) {
            return 0;
        }
        int levelsGained = 0;
        state.setExperience(state.getExperience() + Math.max(0, amount));
        while (state.getExperience() >= xpForNextLevel(state.getLevel())) {
            state.setExperience(state.getExperience() - xpForNextLevel(state.getLevel()));
            state.setLevel(state.getLevel() + 1);
            levelsGained++;
        }
        return levelsGained;
    }

    public static boolean applyEvolution(RobotProgressionState state, String unlockedGrade) {
        if (state == null) {
            return false;
        }
        int originalTier = state.getEvolutionTier();
        if (state.getLevel() >= 10 && gradeAtLeast(unlockedGrade, "C")) {
            state.setEvolutionTier(3);
        } else if (state.getLevel() >= 5 && gradeAtLeast(unlockedGrade, "E")) {
            state.setEvolutionTier(2);
        }
        return state.getEvolutionTier() != originalTier;
    }

    public static String getEvolvedRobotId(String robotId, int evolutionTier) {
        if (robotId == null || robotId.isEmpty()) {
            return robotId;
        }
        String baseId = robotId.replace("_mk1", "").replace("_mk2", "").replace("_mk3", "");
        int clampedTier = Math.max(1, Math.min(3, evolutionTier));
        return baseId + "_mk" + clampedTier;
    }

    public static float statMultiplier(int evolutionTier) {
        switch (Math.max(1, evolutionTier)) {
            case 3:
                return 1.22f;
            case 2:
                return 1.10f;
            default:
                return 1f;
        }
    }

    public static float levelBonusPerLevel() {
        return 1.5f;
    }

    private static int xpForNextLevel(int level) {
        return 35 + (level * 18);
    }

    private static boolean gradeAtLeast(String grade, String requirement) {
        return gradeIndex(grade) >= gradeIndex(requirement);
    }

    private static int gradeIndex(String grade) {
        String[] order = {"G", "F", "E", "D", "C", "B", "A", "S", "S+", "S++", "S+++"};
        if (grade == null) {
            return 0;
        }
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(grade)) {
                return i;
            }
        }
        return 0;
    }
}
