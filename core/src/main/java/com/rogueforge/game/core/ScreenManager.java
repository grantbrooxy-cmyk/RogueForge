package com.rogueforge.game.core;

import com.badlogic.gdx.Screen;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Manages a stack of screens. Defers disposal of old screens to the end of the
 * current frame to avoid destroying GL resources mid-draw.
 */
public class ScreenManager {
    private final Stack<Screen> screenStack;
    private final List<Screen> pendingDispose;
    private Screen pendingPush;
    private boolean pendingPop;
    private boolean pendingReplace;

    public ScreenManager() {
        this.screenStack = new Stack<>();
        this.pendingDispose = new ArrayList<>();
    }

    /** Push a new screen onto the stack. Safe to call during render(). */
    public void push(Screen screen) {
        if (screen == null) throw new IllegalArgumentException("Cannot push null screen");

        if (!screenStack.isEmpty()) {
            screenStack.peek().hide();
        }
        screenStack.push(screen);
        screen.show();
    }

    /** Pop the current screen. Disposal is deferred. */
    public void pop() {
        if (screenStack.isEmpty()) return;
        Screen old = screenStack.pop();
        old.hide();
        pendingDispose.add(old);

        if (!screenStack.isEmpty()) {
            screenStack.peek().show();
        }
    }

    /** Replace the current screen. Old screen disposal is deferred. */
    public void replace(Screen screen) {
        if (screen == null) throw new IllegalArgumentException("Cannot replace with null screen");

        if (!screenStack.isEmpty()) {
            Screen old = screenStack.pop();
            old.hide();
            pendingDispose.add(old);
        }
        screenStack.push(screen);
        screen.show();
    }

    /** Call this once per frame from the main game's render() AFTER rendering. */
    public void flushDispose() {
        for (Screen s : pendingDispose) {
            s.dispose();
        }
        pendingDispose.clear();
    }

    public Screen current() {
        return screenStack.isEmpty() ? null : screenStack.peek();
    }

    public boolean isEmpty() { return screenStack.isEmpty(); }
    public int size() { return screenStack.size(); }

    public void resize(int width, int height) {
        if (!screenStack.isEmpty()) {
            screenStack.peek().resize(width, height);
        }
    }

    public void dispose() {
        // Flush any pending first
        flushDispose();
        while (!screenStack.isEmpty()) {
            screenStack.pop().dispose();
        }
    }

    public void pause() {
        if (!screenStack.isEmpty()) screenStack.peek().pause();
    }

    public void resume() {
        if (!screenStack.isEmpty()) screenStack.peek().resume();
    }

    public void render(float delta) {
        if (!screenStack.isEmpty()) {
            screenStack.peek().render(delta);
        }
    }
}
