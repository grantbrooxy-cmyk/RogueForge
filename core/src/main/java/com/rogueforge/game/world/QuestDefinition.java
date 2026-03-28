package com.rogueforge.game.world;

/**
 * Data-driven quest definition.
 */
public class QuestDefinition {
    private String id;
    private String title;
    private int act;
    private String category;
    private boolean mainQuest;
    private boolean autoStart;
    private String startStepId;
    private QuestStep[] steps;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getAct() {
        return act;
    }

    public String getCategory() {
        return category != null ? category : "";
    }

    public boolean isMainQuest() {
        return mainQuest;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public String getStartStepId() {
        return startStepId;
    }

    public QuestStep[] getSteps() {
        return steps != null ? steps : new QuestStep[0];
    }
}
