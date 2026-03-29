package com.rogueforge.game.persistence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.data.MetaProgressionState;

/**
 * Persists cross-run roguelite progression.
 */
public class MetaProgressionManager {
    private static final String META_FILE = "saves/meta_progression.json";

    private final Json json = new Json();

    public MetaProgressionState load() {
        FileHandle file = Gdx.files.local(META_FILE);
        if (!file.exists()) {
            return new MetaProgressionState();
        }
        try {
            MetaProgressionState state = json.fromJson(MetaProgressionState.class, file.readString());
            return state != null ? state : new MetaProgressionState();
        } catch (Exception ex) {
            return new MetaProgressionState();
        }
    }

    public void save(MetaProgressionState state) {
        FileHandle file = Gdx.files.local(META_FILE);
        if (!file.parent().exists()) {
            file.parent().mkdirs();
        }
        file.writeString(json.prettyPrint(state != null ? state : new MetaProgressionState()), false);
    }
}
