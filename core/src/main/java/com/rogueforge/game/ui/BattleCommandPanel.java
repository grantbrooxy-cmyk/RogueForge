package com.rogueforge.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Scene2D command list for the active battle menu.
 */
public class BattleCommandPanel extends Table {
    private final Label titleLabel;
    private final Label hintLabel;
    private final List<Label> optionLabels = new ArrayList<>();

    public BattleCommandPanel(BitmapFont titleFont, BitmapFont bodyFont, BitmapFont hintFont) {
        top().left();
        pad(12f);

        titleLabel = new Label("Commands", new Label.LabelStyle(titleFont, Color.WHITE));
        add(titleLabel).left().padBottom(8f);
        row();

        for (int i = 0; i < 8; i++) {
            Label optionLabel = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
            optionLabels.add(optionLabel);
            add(optionLabel).left().padBottom(4f);
            row();
        }

        hintLabel = new Label("", new Label.LabelStyle(hintFont, Color.LIGHT_GRAY));
        add(hintLabel).left().padTop(6f);
        pack();
    }

    public void updateOptions(String title, String[] options, int selectedIndex, String hintText) {
        titleLabel.setText(title != null ? title : "Commands");
        for (int i = 0; i < optionLabels.size(); i++) {
            Label label = optionLabels.get(i);
            if (options != null && i < options.length) {
                label.setText((i + 1) + ". " + options[i]);
                label.setColor(i == selectedIndex
                    ? new Color(1f, 0.9f, 0.55f, 1f)
                    : Color.WHITE);
            } else {
                label.setText("");
            }
        }
        hintLabel.setText(hintText != null ? hintText : "");
        pack();
    }
}
