package com.rogueforge.game.entity;

import com.rogueforge.game.entity.component.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tiny typed component store for the lightweight entity model.
 */
public final class ComponentManager {
    private final Map<Class<? extends Component>, Component> components = new LinkedHashMap<>();

    public <T extends Component> T add(T component) {
        if (component == null) {
            throw new IllegalArgumentException("Component instance is required.");
        }
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) component.getClass();
        return add(type, component);
    }

    public <T extends Component> T add(Class<T> type, T component) {
        if (type == null || component == null) {
            throw new IllegalArgumentException("Component type and instance are required.");
        }
        components.put(type, component);
        return component;
    }

    public <T extends Component> T get(Class<T> type) {
        Component component = components.get(type);
        return component != null ? type.cast(component) : null;
    }

    public boolean has(Class<? extends Component> type) {
        return components.containsKey(type);
    }

    public <T extends Component> T remove(Class<T> type) {
        Component removed = components.remove(type);
        return removed != null ? type.cast(removed) : null;
    }

    public Collection<Component> getAll() {
        return Collections.unmodifiableCollection(components.values());
    }
}
