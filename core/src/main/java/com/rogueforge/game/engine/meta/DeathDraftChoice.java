package com.rogueforge.game.engine.meta;

public class DeathDraftChoice {
    public enum Kind {
        ENHANCEMENT,
        CURSE,
        CLEAR_CURSE
    }

    private final Kind kind;
    private final String id;
    private final String name;
    private final String description;

    public DeathDraftChoice(Kind kind, String id, String name, String description) {
        this.kind = kind;
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Kind getKind() {
        return kind;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
