package com.rogueforge.game.world;

/**
 * One step in a quest chain.
 */
public class QuestStep {
    private String id;
    private String objective;
    private String completionWorldFlag;
    private String completionKeyItem;

    public String getId() {
        return id;
    }

    public String getObjective() {
        return objective;
    }

    public String getCompletionWorldFlag() {
        return completionWorldFlag;
    }

    public String getCompletionKeyItem() {
        return completionKeyItem;
    }
}
