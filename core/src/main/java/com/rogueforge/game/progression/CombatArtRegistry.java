package com.rogueforge.game.progression;

import com.rogueforge.game.combat.WeaponType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Maps each weapon type to its three Combat Art ability IDs, unlocked at
 * weapon proficiency levels 3, 6, and 10 respectively.
 *
 * Tier mapping:
 *   Tier I  (level  3) — basic combat art, moderate power
 *   Tier II (level  6) — advanced art, often with status effects or AoE
 *   Tier III (level 10) — mastery skill, high damage / unique mechanic
 *
 * The IDs here must match the entries in {@code assets/data/abilities.json}.
 */
public final class CombatArtRegistry {

    /** Proficiency level at which Tier I art is unlocked. */
    public static final int TIER_I_LEVEL   = 3;
    /** Proficiency level at which Tier II art is unlocked. */
    public static final int TIER_II_LEVEL  = 6;
    /** Proficiency level at which Tier III art (Weapon Mastery) is unlocked. */
    public static final int TIER_III_LEVEL = 10;

    private CombatArtRegistry() {
    }

    // ── Art ID tables ──────────────────────────────────────────────────────

    private static final String[] SWORD      = { "sword_cleave",       "sword_guard_break",      "sword_last_rites"     };
    private static final String[] AXE        = { "axe_skull_crush",    "axe_maim",               "axe_devastate"        };
    private static final String[] LANCE      = { "lance_skewer",       "lance_sweep",            "lance_meteor_thrust"  };
    private static final String[] STAFF      = { "staff_ether_bolt",   "staff_mana_surge",       "staff_genesis_ray"    };
    private static final String[] BOW        = { "bow_snipe",          "bow_rain_of_arrows",     "bow_void_shot"        };
    private static final String[] FIST       = { "fist_iron_fist",     "fist_concussive_blow",   "fist_berserker_rush"  };
    private static final String[] GUN        = { "gun_burst_shot",     "gun_suppressive_fire",   "gun_railgun"          };
    private static final String[] DUAL_BLADE = { "dual_dancing_blades","dual_vortex_slash",      "dual_phantom_cross"   };

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Returns the ability ID for the given weapon type and tier (1, 2, or 3).
     * Returns an empty string for {@link WeaponType#NONE} or an invalid tier.
     */
    public static String getArtId(WeaponType weaponType, int tier) {
        String[] arts = tableFor(weaponType);
        if (arts == null || tier < 1 || tier > 3) {
            return "";
        }
        return arts[tier - 1];
    }

    /**
     * Returns all three Combat Art ability IDs for the given weapon type,
     * ordered Tier I → Tier II → Tier III. Returns an empty list for
     * {@link WeaponType#NONE}.
     */
    public static List<String> getAllArtIds(WeaponType weaponType) {
        String[] arts = tableFor(weaponType);
        if (arts == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(arts);
    }

    /**
     * Returns the ability IDs that should be unlocked at {@code level},
     * i.e. the arts for tiers where the proficiency threshold was just reached.
     * Returns an empty list if {@code level} is not a milestone level (3, 6, 10)
     * or the weapon type is NONE.
     */
    public static List<String> getUnlockedAtLevel(WeaponType weaponType, int level) {
        String artId = getArtIdForLevel(weaponType, level);
        if (artId.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(artId);
    }

    /**
     * Returns the ability ID unlocked at a specific proficiency level milestone,
     * or an empty string if this level is not a milestone.
     */
    public static String getArtIdForLevel(WeaponType weaponType, int level) {
        if (level == TIER_I_LEVEL)   return getArtId(weaponType, 1);
        if (level == TIER_II_LEVEL)  return getArtId(weaponType, 2);
        if (level == TIER_III_LEVEL) return getArtId(weaponType, 3);
        return "";
    }

    /**
     * Returns the unlock level for a given Combat Art tier (1→3, 2→6, 3→10),
     * or -1 for an invalid tier.
     */
    public static int levelForTier(int tier) {
        switch (tier) {
            case 1: return TIER_I_LEVEL;
            case 2: return TIER_II_LEVEL;
            case 3: return TIER_III_LEVEL;
            default: return -1;
        }
    }

    /**
     * Returns the tier (1, 2, or 3) of the given ability ID within the
     * specified weapon type's art table, or -1 if not found.
     */
    public static int tierOf(WeaponType weaponType, String abilityId) {
        String[] arts = tableFor(weaponType);
        if (arts == null || abilityId == null) {
            return -1;
        }
        for (int i = 0; i < arts.length; i++) {
            if (arts[i].equals(abilityId)) {
                return i + 1;
            }
        }
        return -1;
    }

    // ── Internal helpers ───────────────────────────────────────────────────

    private static String[] tableFor(WeaponType weaponType) {
        if (weaponType == null || weaponType == WeaponType.NONE) {
            return null;
        }
        switch (weaponType) {
            case SWORD:      return SWORD;
            case AXE:        return AXE;
            case LANCE:      return LANCE;
            case STAFF:      return STAFF;
            case BOW:        return BOW;
            case FIST:       return FIST;
            case GUN:        return GUN;
            case DUAL_BLADE: return DUAL_BLADE;
            default:         return null;
        }
    }
}
