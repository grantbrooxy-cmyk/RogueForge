package com.rogueforge.game.progression;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles robot level progression and Forge-Core-gated evolution tiers.
 *
 * <h3>Evolution gate summary</h3>
 * <pre>
 *  Tier 1 (mk1) — starting form, always available
 *  Tier 2 (mk2) — robot level ≥ 5  AND player grade ≥ E AND Forge Core ≥ Lv2
 *                 Cost: {@link #TIER2_COST} ({@value #TIER2_BONE_FIBER_AMOUNT}× bone_fiber)
 *  Tier 3 (mk3) — robot level ≥ 10 AND player grade ≥ C AND Forge Core ≥ Lv3
 *                 Cost: {@link #TIER3_COST} ({@value #TIER3_DRAKE_HEART_AMOUNT}× drake_heart)
 * </pre>
 *
 * <p>Material costs are consumed by the caller (GameScreen) via
 * {@link com.rogueforge.game.core.GameState#consumeForgeComponents}.
 * {@link #applyEvolution} only mutates the evolution tier; the caller is
 * responsible for reverting the tier if it cannot pay the cost.
 */
public final class RobotEvolutionManager {

    // ── Tier 2 material cost ───────────────────────────────────────────────
    /** Component ID required for Tier 2 evolution. */
    public static final String TIER2_COMPONENT_ID   = "bone_fiber";
    /** Amount of {@link #TIER2_COMPONENT_ID} required for Tier 2 evolution. */
    public static final int    TIER2_BONE_FIBER_AMOUNT = 3;

    // ── Tier 3 material cost ───────────────────────────────────────────────
    /** Component ID required for Tier 3 evolution. */
    public static final String TIER3_COMPONENT_ID   = "drake_heart";
    /** Amount of {@link #TIER3_COMPONENT_ID} required for Tier 3 evolution. */
    public static final int    TIER3_DRAKE_HEART_AMOUNT = 2;

    /** Pre-built cost map for Tier 2. Safe to share (immutable contents). */
    public static final Map<String, Integer> TIER2_COST;
    /** Pre-built cost map for Tier 3. Safe to share (immutable contents). */
    public static final Map<String, Integer> TIER3_COST;

    static {
        Map<String, Integer> t2 = new HashMap<>();
        t2.put(TIER2_COMPONENT_ID, TIER2_BONE_FIBER_AMOUNT);
        TIER2_COST = Collections.unmodifiableMap(t2);

        Map<String, Integer> t3 = new HashMap<>();
        t3.put(TIER3_COMPONENT_ID, TIER3_DRAKE_HEART_AMOUNT);
        TIER3_COST = Collections.unmodifiableMap(t3);
    }

    private RobotEvolutionManager() {
    }

    // ── Experience ─────────────────────────────────────────────────────────

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

    // ── Evolution ──────────────────────────────────────────────────────────

    /**
     * Checks whether the robot meets all non-material conditions for evolution
     * and advances {@code state.evolutionTier} if so.
     *
     * <p><b>Caller responsibility:</b> if the tier was raised, the caller must
     * attempt {@link com.rogueforge.game.core.GameState#consumeForgeComponents}
     * with {@link #evolutionMaterialCost} and revert the tier via
     * {@link RobotProgressionState#setEvolutionTier} if materials are
     * insufficient.
     *
     * @param state          the robot's progression state (mutated on tier change)
     * @param unlockedGrade  the player's current unlocked grade string (e.g. "E", "C")
     * @param forgeCoreLevel the current Forge Core level (1–4)
     * @return {@code true} if the evolution tier was raised
     */
    public static boolean applyEvolution(RobotProgressionState state,
                                         String unlockedGrade,
                                         int forgeCoreLevel) {
        if (state == null) {
            return false;
        }
        int originalTier = state.getEvolutionTier();
        if (state.getLevel() >= 10
                && gradeAtLeast(unlockedGrade, "C")
                && forgeCoreLevel >= 3) {
            state.setEvolutionTier(3);
        } else if (state.getLevel() >= 5
                && gradeAtLeast(unlockedGrade, "E")
                && forgeCoreLevel >= 2) {
            state.setEvolutionTier(2);
        }
        return state.getEvolutionTier() != originalTier;
    }

    /**
     * Returns the forge-component cost map for the given evolution tier.
     * Returns an empty map for Tier 1 (no cost) or any unknown tier.
     */
    public static Map<String, Integer> evolutionMaterialCost(int targetTier) {
        switch (targetTier) {
            case 2: return TIER2_COST;
            case 3: return TIER3_COST;
            default: return Collections.emptyMap();
        }
    }

    // ── ID / stat helpers ──────────────────────────────────────────────────

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
            case 3:  return 1.22f;
            case 2:  return 1.10f;
            default: return 1f;
        }
    }

    public static float levelBonusPerLevel() {
        return 1.5f;
    }

    // ── Private helpers ────────────────────────────────────────────────────

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
