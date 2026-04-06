package com.rogueforge.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.rogueforge.game.combat.BattleCombatant;
import java.util.ArrayList;
import java.util.List;

/**
 * Scene2D timeline preview for upcoming CTB turns.
 */
public class BattleTimelinePanel extends Table {
    private final Label titleLabel;
    private final List<Label> entryLabels = new ArrayList<>();

    public BattleTimelinePanel(BitmapFont titleFont, BitmapFont bodyFont) {
        setFillParent(false);
        top().left();

        titleLabel = new Label("Timeline", new Label.LabelStyle(titleFont, Color.WHITE));
        add(titleLabel).left().padBottom(8f);
        row();

        for (int i = 0; i < 8; i++) {
            Label entry = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
            entry.setWrap(false);
            entryLabels.add(entry);
            add(entry).left().padBottom(3f);
            row();
        }
    }

    public void updateTimeline(List<BattleCombatant> turns) {
        for (int i = 0; i < entryLabels.size(); i++) {
            Label entry = entryLabels.get(i);
            if (turns != null && i < turns.size()) {
                BattleCombatant combatant = turns.get(i);
                entry.setText((i + 1) + ". " + combatant.getName());
                entry.setColor(combatant.isAlly()
                    ? new Color(0.71f, 0.9f, 1f, 1f)
                    : new Color(1f, 0.78f, 0.74f, 1f));
            } else {
                entry.setText("");
            }
        }
        pack();
    }
}
