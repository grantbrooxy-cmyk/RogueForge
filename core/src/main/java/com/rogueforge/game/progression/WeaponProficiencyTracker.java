package com.rogueforge.game.progression;

import com.rogueforge.game.combat.WeaponType;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared formulas and mutation helpers for weapon proficiency growth and
 * combat scaling.
 *
 * <p>Unlock milestones (levels 3, 6, 10) correspond to Combat Art tiers I–III
 * as defined in {@link CombatArtRegistry}. {@link #gainXp} handles XP addition,
 * level-up, and automatic milestone unlock in a single call so callers don't
 * need to replicate the logic.
 */
public final class WeaponProficiencyTracker {
    private WeaponProficiencyTracker() {
    }

    // ── State helpers ──────────────────────────────────────────────────────

    public static WeaponProficiencyState getOrCreate(RobotProgressionState robotState, WeaponType weaponType) {
        if (robotState == null || weaponType == null || weaponType == WeaponType.NONE) {
            return null;
        }
        WeaponProficiencyState state = robotState.getWeaponProficiencies().get(weaponType.name());
        if (state == null) {
            state = new WeaponProficiencyState(weaponType.name());
            robotState.getWeaponProficiencies().put(weaponType.name(), state);
        }
        return state;
    }

    // ── XP / level-up ─────────────────────────────────────────────────────

    /** Base XP awarded for a physical attack with this weapon. */
    public static int xpForAttack() {
        return 10;
    }

    /**
     * Adds XP to the state, processes any level-ups, and automatically unlocks
     * Combat Art milestones at levels 3, 6, and 10.
     *
     * @param state      the weapon proficiency state to mutate
     * @param weaponType the weapon family (used to look up art IDs in {@link CombatArtRegistry})
     * @param amount     XP to add (negative values are ignored by {@link WeaponProficiencyState#addXp})
     * @return a list of ability IDs that were newly unlocked this call (empty when nothing unlocked)
     */
    public static List<String> gainXp(WeaponProficiencyState state, WeaponType weaponType, int amount) {
        List<String> newlyUnlocked = new ArrayList<>();
        if (state == null || weaponType == null || weaponType == WeaponType.NONE) {
            return newlyUnlocked;
        }

        int levelBefore = state.getLevel();
        int levelsGained = state.addXp(amount);
        if (levelsGained <= 0) {
            return newlyUnlocked;
        }

        // Check every level crossed for a milestone.
        for (int lvl = levelBefore + 1; lvl <= state.getLevel(); lvl++) {
            String artId = CombatArtRegistry.getArtIdForLevel(weaponType, lvl);
            if (!artId.isEmpty()) {
                boolean added = state.unlockMilestone(artId);
                if (added) {
                    newlyUnlocked.add(artId);
                }
            }
        }
        return newlyUnlocked;
    }

    // ── Scaling formulas ───────────────────────────────────────────────────

    /** Returns the physical damage multiplier for the given proficiency level (1–10). */
    public static float damageMultiplier(int level) {
        int clamped = Math.max(1, Math.min(10, level));
        return 1f + ((clamped - 1) * 0.03f);
    }

    // ── UI helpers ─────────────────────────────────────────────────────────

    /**
     * Returns the ability ID that is unlocked at the given proficiency level,
     * or an empty string when the level is not a milestone.
     *
     * <p>Prefer this over the old {@code unlockLabel()} in UI code so the label
     * can be looked up from the ability definition name rather than being
     * hard-coded here.
     */
    public static String unlockArtId(WeaponType weaponType, int level) {
        return CombatArtRegistry.getArtIdForLevel(weaponType, level);
    }

    /**
     * Returns a human-readable milestone label for display in the UI, e.g. on
     * a level-up banner. The label is generic (not weapon-specific) so it can
     * be shown before the ability name is resolved from the registry.
     *
     * @deprecated Use {@link CombatArtRegistry#getArtIdForLevel(WeaponType, int)}
     *             and resolve the ability name from {@code AbilityRegistry} instead.
     */
    @Deprecated
    public static String unlockLabel(int level) {
        switch (level) {
            case CombatArtRegistry.TIER_I_LEVEL:   return "Combat Art I";
            case CombatArtRegistry.TIER_II_LEVEL:  return "Combat Art II";
            case CombatArtRegistry.TIER_III_LEVEL: return "Weapon Mastery";
            default: return "";
        }
    }
}
