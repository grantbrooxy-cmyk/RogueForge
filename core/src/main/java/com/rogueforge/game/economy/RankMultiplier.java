package com.rogueforge.game.economy;

/**
 * Enum representing monster/loot rank tiers with corresponding currency multipliers.
 * Ranks progress from G (lowest) to S+++ (highest).
 */
public enum RankMultiplier {
    G(1, "G"),
    F(2, "F"),
    E(4, "E"),
    D(8, "D"),
    C(16, "C"),
    B(32, "B"),
    A(64, "A"),
    S(128, "S"),
    S_PLUS(256, "S+"),
    S_PLUS_PLUS(512, "S++"),
    S_PLUS_PLUS_PLUS(1024, "S+++");

    private final int multiplier;
    private final String displayName;

    RankMultiplier(int multiplier, String displayName) {
        this.multiplier = multiplier;
        this.displayName = displayName;
    }

    /**
     * Gets the currency multiplier for this rank.
     *
     * @return The multiplier value
     */
    public int getMultiplier() {
        return multiplier;
    }

    /**
     * Gets the display name for this rank.
     *
     * @return The display string (e.g., "S+++")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses a rank from a string name.
     *
     * @param name The rank name (e.g., "S+", "S++", "S+++", or enum name like "S_PLUS")
     * @return The RankMultiplier enum value, or null if not found
     */
    public static RankMultiplier fromString(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        // Try to match by display name
        for (RankMultiplier rank : RankMultiplier.values()) {
            if (rank.displayName.equalsIgnoreCase(name)) {
                return rank;
            }
        }

        // Try to match by enum name
        try {
            return RankMultiplier.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Gets the next rank in progression.
     *
     * @param current The current rank
     * @return The next rank, or null if already at maximum (S+++)
     */
    public static RankMultiplier next(RankMultiplier current) {
        if (current == null) {
            return G;
        }

        switch (current) {
            case G: return F;
            case F: return E;
            case E: return D;
            case D: return C;
            case C: return B;
            case B: return A;
            case A: return S;
            case S: return S_PLUS;
            case S_PLUS: return S_PLUS_PLUS;
            case S_PLUS_PLUS: return S_PLUS_PLUS_PLUS;
            case S_PLUS_PLUS_PLUS: return null; // Max rank
            default: return null;
        }
    }

    /**
     * Gets the previous rank in progression.
     *
     * @param current The current rank
     * @return The previous rank, or null if already at minimum (G)
     */
    public static RankMultiplier previous(RankMultiplier current) {
        if (current == null) {
            return S_PLUS_PLUS_PLUS;
        }

        switch (current) {
            case G: return null; // Min rank
            case F: return G;
            case E: return F;
            case D: return E;
            case C: return D;
            case B: return C;
            case A: return B;
            case S: return A;
            case S_PLUS: return S;
            case S_PLUS_PLUS: return S_PLUS;
            case S_PLUS_PLUS_PLUS: return S_PLUS_PLUS;
            default: return null;
        }
    }

    /**
     * Gets the rank index (0-based, where G=0, S+++= 10).
     * Useful for comparisons and sorting.
     *
     * @return The rank index
     */
    public int getIndex() {
        return this.ordinal();
    }

    /**
     * Checks if this rank is higher than another.
     *
     * @param other The rank to compare against
     * @return true if this rank is higher
     */
    public boolean isHigherThan(RankMultiplier other) {
        return this.getIndex() > other.getIndex();
    }

    /**
     * Checks if this rank is lower than another.
     *
     * @param other The rank to compare against
     * @return true if this rank is lower
     */
    public boolean isLowerThan(RankMultiplier other) {
        return this.getIndex() < other.getIndex();
    }
}
