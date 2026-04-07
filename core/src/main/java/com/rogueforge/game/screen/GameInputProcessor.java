package com.rogueforge.game.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

/**
 * Input processor for the GameScreen.
 * Handles game-specific input like pause and movement.
 */
public class GameInputProcessor implements InputProcessor {
    private final GameScreen gameScreen;

    public GameInputProcessor(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    @Override
    public boolean keyDown(int keycode) {
        InputContext context = gameScreen.getCurrentInputContext();
        if (keycode == Input.Keys.ESCAPE) {
            gameScreen.handlePauseInput();
            return true;
        }
        if (keycode == Input.Keys.I && context == InputContext.EXPLORATION) {
            gameScreen.openWorkshop();
            return true;
        }
        if (keycode == Input.Keys.ENTER
            && (context == InputContext.DIALOG || context == InputContext.SETTLEMENT || context == InputContext.BUILD)) {
            gameScreen.dismissMessages();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }
}
