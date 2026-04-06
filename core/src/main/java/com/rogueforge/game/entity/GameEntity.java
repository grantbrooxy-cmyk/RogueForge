package com.rogueforge.game.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight base entity that owns a typed component map.
 *
 * <p>This is intentionally small: it gives us constructor-injected composition
 * without committing the whole project to a full ECS yet.</p>
 */
public abstract class GameEntity {
    private final Map<Class<?>, Object> components = new LinkedHashMap<>();
    private String entityId;

    protected GameEntity() {
    }

    protected GameEntity(String entityId) {
        this.entityId = entityId;
    }

    protected <T> T registerComponent(Class<T> type, T component) {
        if (type == null || component == null) {
            throw new IllegalArgumentException("Component type and instance are required.");
        }
        components.put(type, component);
        return component;
    }

    public <T> T getComponent(Class<T> type) {
        Object component = components.get(type);
        return component != null ? type.cast(component) : null;
    }

    public <T> T requireComponent(Class<T> type) {
        T component = getComponent(type);
        if (component == null) {
            throw new IllegalStateException("Missing component: " + type.getSimpleName());
        }
        return component;
    }

    public boolean hasComponent(Class<?> type) {
        return components.containsKey(type);
    }

    public Collection<Object> getComponents() {
        return Collections.unmodifiableCollection(components.values());
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
