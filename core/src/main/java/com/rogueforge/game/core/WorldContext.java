package com.rogueforge.game.core;

import com.rogueforge.game.engine.GameEngineServices;

/**
 * Narrow world/services view for gameplay screens.
 */
public interface WorldContext {
    GameEngineServices getEngineServices();
    <T> T getService(Class<T> type);
}
