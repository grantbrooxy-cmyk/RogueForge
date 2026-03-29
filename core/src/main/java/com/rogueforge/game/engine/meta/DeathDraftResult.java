package com.rogueforge.game.engine.meta;

import java.util.ArrayList;
import java.util.List;

public class DeathDraftResult {
    private final DeathDraftTier tier;
    private final int progressScore;
    private final String summary;
    private final List<DeathDraftChoice> choices;

    public DeathDraftResult(DeathDraftTier tier, int progressScore, String summary, List<DeathDraftChoice> choices) {
        this.tier = tier != null ? tier : DeathDraftTier.CATASTROPHIC;
        this.progressScore = Math.max(0, progressScore);
        this.summary = summary;
        this.choices = choices != null ? new ArrayList<>(choices) : new ArrayList<>();
    }

    public DeathDraftTier getTier() {
        return tier;
    }

    public int getProgressScore() {
        return progressScore;
    }

    public String getSummary() {
        return summary;
    }

    public List<DeathDraftChoice> getChoices() {
        return new ArrayList<>(choices);
    }
}
