package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.InputMultiplexer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Small router that rebuilds an InputMultiplexer from the active input context.
 */
public class InputContextRouter {
    private final Map<InputContext, List<InputProcessor>> processorsByContext = new EnumMap<>(InputContext.class);
    private final List<InputProcessor> sharedProcessors = new ArrayList<>();
    private final InputMultiplexer multiplexer = new InputMultiplexer();
    private InputContext currentContext;

    public void addSharedProcessor(InputProcessor processor) {
        if (processor != null) {
            sharedProcessors.add(processor);
        }
    }

    public void setProcessors(InputContext context, InputProcessor... processors) {
        List<InputProcessor> values = processorsByContext.computeIfAbsent(context, ignored -> new ArrayList<>());
        values.clear();
        if (processors != null) {
            for (InputProcessor processor : processors) {
                if (processor != null) {
                    values.add(processor);
                }
            }
        }
    }

    public void activate(InputContext context) {
        currentContext = context;
        multiplexer.clear();
        for (InputProcessor processor : sharedProcessors) {
            multiplexer.addProcessor(processor);
        }
        List<InputProcessor> processors = processorsByContext.get(context);
        if (processors != null) {
            for (InputProcessor processor : processors) {
                multiplexer.addProcessor(processor);
            }
        }
        Gdx.input.setInputProcessor(multiplexer);
    }

    public InputContext getCurrentContext() {
        return currentContext;
    }
}
