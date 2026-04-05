package com.rogueforge.game.ui;

import com.badlogic.gdx.graphics.Color;

/**
 * Centralized UI theme with colors, ranks, and font sizes.
 * Provides consistent styling across the game UI.
 */
public class UITheme {

    // Base colors — warm pixel-art RPG palette (Stardew / Pokémon inspired)
    public static final Color PRIMARY = new Color(0.357f, 0.549f, 0.353f, 1.0f);   // Muted sage green
    public static final Color SECONDARY = new Color(0.741f, 0.714f, 0.667f, 1.0f); // Warm stone gray
    public static final Color ACCENT = new Color(0.910f, 0.663f, 0.188f, 1.0f);    // Warm amber gold
    public static final Color BACKGROUND = new Color(0.110f, 0.086f, 0.063f, 1.0f); // Warm dark brown-black
    public static final Color TEXT = new Color(0.961f, 0.933f, 0.847f, 1.0f);      // Warm cream (not pure white)
    public static final Color TEXT_DARK = new Color(0.165f, 0.133f, 0.102f, 1.0f); // Warm dark brown

    // Status bar colors — muted, natural tones
    public static final Color HEALTH_BAR = new Color(0.322f, 0.698f, 0.337f, 1.0f);         // Warm forest green
    public static final Color HEALTH_BAR_CRITICAL = new Color(0.839f, 0.243f, 0.196f, 1.0f); // Warm red-orange
    public static final Color MANA_BAR = new Color(0.353f, 0.616f, 0.784f, 1.0f);            // Muted sky blue
    public static final Color EXPERIENCE_BAR = new Color(0.910f, 0.663f, 0.188f, 1.0f);      // Amber (matches ACCENT)

    // Rank colors
    public static final Color RANK_G = new Color(0.6f, 0.6f, 0.6f, 1.0f);       // Gray
    public static final Color RANK_F = new Color(1.0f, 1.0f, 1.0f, 1.0f);       // White
    public static final Color RANK_E = new Color(0.2f, 1.0f, 0.2f, 1.0f);       // Green
    public static final Color RANK_D = new Color(0.2f, 0.8f, 1.0f, 1.0f);       // Blue
    public static final Color RANK_C = new Color(1.0f, 0.2f, 1.0f, 1.0f);       // Purple
    public static final Color RANK_A = new Color(1.0f, 0.85f, 0.0f, 1.0f);      // Gold
    public static final Color RANK_S = new Color(1.0f, 0.2f, 0.2f, 1.0f);       // Red
    public static final Color RANK_S_PLUS = new Color(0.8f, 0.0f, 0.0f, 1.0f);  // Dark red/Crimson
    public static final Color RANK_S_PLUSPLUS = new Color(1.0f, 0.5f, 0.0f, 1.0f); // Orange
    public static final Color RANK_S_PLUSPLUSPLUS = new Color(1.0f, 0.2f, 0.8f, 1.0f); // Magenta (special)

    // Font size constants
    public static final int FONT_SIZE_SMALL = 12;
    public static final int FONT_SIZE_MEDIUM = 16;
    public static final int FONT_SIZE_LARGE = 24;
    public static final int FONT_SIZE_TITLE = 32;

    /**
     * Returns the color associated with a rank string.
     *
     * @param rank the rank identifier (e.g., "G", "F", "E", "D", "C", "A", "S", "S+", "S++", "S+++")
     * @return the Color for the rank, or SECONDARY if rank is unknown
     */
    public static Color getColorForRank(String rank) {
        if (rank == null) {
            return SECONDARY;
        }

        switch (rank.toUpperCase()) {
            case "G":
                return RANK_G;
            case "F":
                return RANK_F;
            case "E":
                return RANK_E;
            case "D":
                return RANK_D;
            case "C":
                return RANK_C;
            case "A":
                return RANK_A;
            case "S":
                return RANK_S;
            case "S+":
                return RANK_S_PLUS;
            case "S++":
                return RANK_S_PLUSPLUS;
            case "S+++":
                return RANK_S_PLUSPLUSPLUS;
            default:
                return SECONDARY;
        }
    }

    /**
     * Gets a slightly darker version of a color for shadows or borders.
     *
     * @param color the base color
     * @param factor the darkening factor (0.5f = 50% darker)
     * @return a new Color that is darker
     */
    public static Color darken(Color color, float factor) {
        return new Color(
            color.r * (1 - factor),
            color.g * (1 - factor),
            color.b * (1 - factor),
            color.a
        );
    }

    /**
     * Gets a slightly lighter version of a color for highlights.
     *
     * @param color the base color
     * @param factor the lightening factor (0.5f = 50% lighter)
     * @return a new Color that is lighter
     */
    public static Color lighten(Color color, float factor) {
        return new Color(
            Math.min(1.0f, color.r + (1 - color.r) * factor),
            Math.min(1.0f, color.g + (1 - color.g) * factor),
            Math.min(1.0f, color.b + (1 - color.b) * factor),
            color.a
        );
    }
}
