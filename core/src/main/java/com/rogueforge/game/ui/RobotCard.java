package com.rogueforge.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.rogueforge.game.robot.RobotDefinition;

/**
 * Scene2D widget displaying a robot card.
 * Shows robot name, role, HP bar, and equipment summary.
 */
public class RobotCard extends Table {

    private RobotDefinition robot;

    private Label nameLabel;
    private Label roleLabel;
    private ProgressBar hpBar;
    private Label equipmentLabel;

    private int currentHp;
    private int maxHp;

    /**
     * Constructs a new RobotCard with default styling.
     */
    public RobotCard() {
        super();
        initializeUI();
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        // Set up card styling
        this.background(createBackground());
        this.pad(8);
        this.defaults().expandX().fillX().space(4);

        // Name label
        nameLabel = new Label("", new Label.LabelStyle(
            null, // Font will be set by skin
            UITheme.TEXT
        ));
        nameLabel.setAlignment(Align.left);
        this.add(nameLabel).colspan(2).height(20);
        this.row();

        // Role badge
        roleLabel = new Label("", new Label.LabelStyle(
            null,
            UITheme.TEXT
        ));
        roleLabel.setAlignment(Align.center);
        this.add(roleLabel).width(80).height(20);

        // HP label
        Label hpLabelText = new Label("HP:", new Label.LabelStyle(
            null,
            UITheme.TEXT_DARK
        ));
        this.add(hpLabelText).width(30);
        this.row();

        // HP bar
        hpBar = new ProgressBar(0, 100, 1, false, new ProgressBar.ProgressBarStyle());
        hpBar.getStyle().background = createBarBackground();
        hpBar.getStyle().knobBefore = createBarFill();
        this.add(hpBar).colspan(2).height(16);
        this.row();

        // Equipment summary
        equipmentLabel = new Label("No equipment", new Label.LabelStyle(
            null,
            UITheme.SECONDARY
        ));
        equipmentLabel.setAlignment(Align.left);
        equipmentLabel.setWrap(true);
        this.add(equipmentLabel).colspan(2);
    }

    /**
     * Sets the robot to display and updates all UI elements.
     *
     * @param robot the RobotDefinition to display
     */
    public void setRobot(RobotDefinition robot) {
        if (robot == null) {
            clear();
            return;
        }

        this.robot = robot;
        this.currentHp = robot.getBaseHp();
        this.maxHp = robot.getBaseHp();

        // Update name
        nameLabel.setText(robot.getName());

        // Update role badge with color (use role name, not rank system)
        // Map role to a display color
        Color roleColor = getRoleColor(robot.getRole());
        roleLabel.setStyle(new Label.LabelStyle(
            null,
            roleColor
        ));
        roleLabel.setText(robot.getRole().name());

        // Update HP bar
        hpBar.setRange(0, maxHp);
        hpBar.setValue(currentHp);

        // Update equipment summary
        updateEquipmentLabel();
    }

    /**
     * Updates the HP bar and label display.
     *
     * @param newHp the current HP value
     */
    public void setHP(int newHp) {
        this.currentHp = Math.max(0, Math.min(newHp, maxHp));
        hpBar.setValue(currentHp);

        // Change bar color based on health percentage
        float healthPercent = (float) currentHp / maxHp;
        if (healthPercent < 0.25f) {
            hpBar.getStyle().knobBefore = createBarFillCritical();
        } else {
            hpBar.getStyle().knobBefore = createBarFill();
        }
    }

    /**
     * Updates the equipment summary label based on equipped items.
     */
    private void updateEquipmentLabel() {
        if (robot == null) {
            equipmentLabel.setText("No equipment");
            return;
        }

        // Build equipment string - this is a placeholder
        // In real implementation, you would fetch actual equipped items
        StringBuilder equipText = new StringBuilder();
        equipText.append("Equipment: ");

        // Example: assume robot has slots like HEAD, BODY, LEGS, FEET
        // In production, you'd iterate over actual equipped items
        equipText.append("(empty)");

        equipmentLabel.setText(equipText.toString());
    }

    /**
     * Creates a drawable for the card background.
     */
    private Drawable createBackground() {
        // This would typically use a 9-patch drawable from your skin
        // For now, returning null (you'll set via skin)
        return null;
    }

    /**
     * Creates a drawable for the health bar background.
     */
    private Drawable createBarBackground() {
        // Returns null - should be provided by Skin in real implementation
        return null;
    }

    /**
     * Creates a drawable for the health bar fill (normal).
     */
    private Drawable createBarFill() {
        // Returns null - should be provided by Skin in real implementation
        return null;
    }

    /**
     * Creates a drawable for the health bar fill (critical).
     */
    private Drawable createBarFillCritical() {
        // Returns null - should be provided by Skin in real implementation
        return null;
    }

    /**
     * Maps robot role to a color.
     */
    private Color getRoleColor(RobotDefinition.RoleEnum role) {
        if (role == null) {
            return UITheme.SECONDARY;
        }
        switch (role) {
            case TANK:    return UITheme.RANK_D;     // Blue
            case DPS:     return UITheme.RANK_S;     // Red
            case SUPPORT: return UITheme.RANK_E;     // Green
            case SCOUT:   return UITheme.RANK_C;     // Purple
            default:      return UITheme.SECONDARY;
        }
    }

    public RobotDefinition getRobot() {
        return robot;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaxHp() {
        return maxHp;
    }
}
