package com.rogueforge.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Scene2D summary panel for actor state and recent battle log lines.
 */
public class BattleInspectorPanel extends Table {
    private final Label headerLabel;
    private final List<Label> detailLabels = new ArrayList<>();
    private final List<Label> logLabels = new ArrayList<>();

    public BattleInspectorPanel(BitmapFont titleFont, BitmapFont bodyFont, BitmapFont smallFont) {
        top().left();
        pad(12f);

        headerLabel = new Label("Battle Feed", new Label.LabelStyle(titleFont, Color.WHITE));
        add(headerLabel).left().padBottom(8f);
        row();

        for (int i = 0; i < 5; i++) {
            Label detail = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
            detailLabels.add(detail);
            add(detail).left().padBottom(3f);
            row();
        }

        add(new Label("Recent Log", new Label.LabelStyle(bodyFont, new Color(0.8f, 0.88f, 0.97f, 1f))))
            .left().padTop(8f).padBottom(6f);
        row();

        for (int i = 0; i < 6; i++) {
            Label logLine = new Label("", new Label.LabelStyle(smallFont, Color.LIGHT_GRAY));
            logLabels.add(logLine);
            add(logLine).left().padBottom(3f);
            row();
        }

        pack();
    }

    public void updateContent(String header, List<String> details, List<String> battleLog) {
        headerLabel.setText(header != null ? header : "Battle Feed");
        for (int i = 0; i < detailLabels.size(); i++) {
            Label label = detailLabels.get(i);
            if (details != null && i < details.size()) {
                label.setText(details.get(i));
            } else {
                label.setText("");
            }
        }
        for (int i = 0; i < logLabels.size(); i++) {
            Label label = logLabels.get(i);
            if (battleLog != null && i < battleLog.size()) {
                label.setText(battleLog.get(i));
            } else {
                label.setText("");
            }
        }
        pack();
    }
}
