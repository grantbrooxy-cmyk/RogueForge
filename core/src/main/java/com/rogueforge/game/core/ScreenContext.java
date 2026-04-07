package com.rogueforge.game.core;

/**
 * Narrow navigation/game host view for screens.
 */
public interface ScreenContext {
    RogueForgeGame getGame();
    ScreenManager getScreenManager();
}
