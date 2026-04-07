package com.rogueforge.game.persistence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.data.MetaProgressionState;
import com.rogueforge.game.engine.ServiceLifecycle;

/**
 * Persists cross-run roguelite progression.
 */
public class MetaProgressionManager implements ServiceLifecycle {
    private final Json json = new Json();

    @Override
    public void initialize() {
        FileHandle file = Gdx.files.local(PersistencePaths.META_PROGRESSION);
        if (!file.parent().exists()) {
            file.parent().mkdirs();
        }
    }

    public MetaProgressionState load() {
        FileHandle file = Gdx.files.local(PersistencePaths.META_PROGRESSION);
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
        FileHandle file = Gdx.files.local(PersistencePaths.META_PROGRESSION);
        if (!file.parent().exists()) {
            file.parent().mkdirs();
        }
        file.writeString(json.prettyPrint(state != null ? state : new MetaProgressionState()), false);
    }
}
