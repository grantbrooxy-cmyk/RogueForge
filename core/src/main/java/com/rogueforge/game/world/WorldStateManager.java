package com.rogueforge.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.core.GameState;

/**
 * Manages global world-state flags that gate content and interactions.
 */
public class WorldStateManager {
    private WorldStateDefinition[] definitions;

    public WorldStateManager() {
        definitions = new Json().fromJson(
            WorldStateDefinition[].class,
            Gdx.files.internal("data/world_state.json").readString()
        );
        if (definitions == null) {
            definitions = new WorldStateDefinition[0];
        }
    }

    public void initialize(GameState state) {
        if (state == null) {
            return;
        }
        for (WorldStateDefinition definition : definitions) {
            if (definition != null && definition.getId() != null && !state.getWorldStateFlags().containsKey(definition.getId())) {
                state.setWorldStateFlag(definition.getId(), definition.isDefaultValue());
            }
        }
    }

    public boolean isFlagActive(GameState state, String flag) {
        return state != null && flag != null && !flag.isEmpty() && state.isWorldStateFlagActive(flag);
    }

    public void setFlag(GameState state, String flag, boolean value) {
        if (state != null && flag != null && !flag.isEmpty()) {
            state.setWorldStateFlag(flag, value);
        }
    }
}
