package com.rogueforge.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

/**
 * Scene2D widget displaying a rank badge.
 * Shows rank text with a colored background matching the rank color scheme.
 */
public class RankBadge extends Table {

    private String rank;
    private Label rankLabel;

    /**
     * Constructs a new RankBadge.
     */
    public RankBadge() {
        super();
        initializeUI();
    }

    /**
     * Constructs a new RankBadge with an initial rank.
     *
     * @param initialRank the rank to display (e.g., "S", "A", "G")
     */
    public RankBadge(String initialRank) {
        super();
        initializeUI();
        setRank(initialRank);
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        // Set up badge styling - compact table
        this.pad(4);
        this.defaults().expandX().fillX();

        // Create label with default rank text
        rankLabel = new Label("?", new Label.LabelStyle(
            null, // Font will be set by skin
            UITheme.TEXT
        ));
        rankLabel.setAlignment(Align.center);

        // Add label to table
        this.add(rankLabel).width(24).height(24);

        // Set initial background
        updateBackground();
    }

    /**
     * Sets the rank to display and updates styling.
     *
     * @param rank the rank string (e.g., "G", "F", "E", "D", "C", "A", "S", "S+", "S++", "S+++")
     */
    public void setRank(String rank) {
        this.rank = rank != null ? rank : "?";
        rankLabel.setText(this.rank);
        updateBackground();
    }

    /**
     * Updates the background color based on the current rank.
     */
    private void updateBackground() {
        Color rankColor = UITheme.getColorForRank(rank);
        // Darken the color slightly for better contrast with white text
        Color bgColor = UITheme.darken(rankColor, 0.3f);

        // In a real implementation, you would set a background drawable
        // with the appropriate color. For now, this is a placeholder.
        // this.background(new Image(new Texture(...)));
    }

    /**
     * Gets the current rank displayed.
     *
     * @return the rank string
     */
    public String getRank() {
        return rank;
    }

    /**
     * Gets the label widget for direct manipulation if needed.
     *
     * @return the Label displaying the rank
     */
    public Label getRankLabel() {
        return rankLabel;
    }
}
