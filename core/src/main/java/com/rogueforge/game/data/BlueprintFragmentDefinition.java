package com.rogueforge.game.data;

public class BlueprintFragmentDefinition {
    private String id;
    private String name;
    private String description;
    private String sourceHint;

    public BlueprintFragmentDefinition() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name != null ? name : id;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public String getSourceHint() {
        return sourceHint != null ? sourceHint : "";
    }
}
