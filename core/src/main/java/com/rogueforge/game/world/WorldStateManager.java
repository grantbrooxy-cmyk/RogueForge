package com.rogueforge.game.world;

import com.rogueforge.game.core.GameState;
import com.rogueforge.game.data.DefinitionRegistries;

/**
 * Manages global world-state flags that gate content and interactions.
 */
public class WorldStateManager {
    private WorldStateDefinition[] definitions;

    public WorldStateManager() {
        reloadDefinitions();
    }

    public void reloadDefinitions() {
        definitions = DefinitionRegistries.WORLD_STATE.getAll().toArray(new WorldStateDefinition[0]);
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
