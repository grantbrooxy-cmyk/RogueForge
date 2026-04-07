package com.rogueforge.game.entity;

import com.rogueforge.game.entity.component.Component;
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
    private final Map<Class<? extends Component>, Component> components = new LinkedHashMap<>();
    private String entityId;

    protected GameEntity() {
    }

    protected GameEntity(String entityId) {
        this.entityId = entityId;
    }

    protected <T extends Component> T registerComponent(Class<T> type, T component) {
        if (type == null || component == null) {
            throw new IllegalArgumentException("Component type and instance are required.");
        }
        components.put(type, component);
        return component;
    }

    public <T extends Component> T getComponent(Class<T> type) {
        Component component = components.get(type);
        return component != null ? type.cast(component) : null;
    }

    public <T extends Component> T requireComponent(Class<T> type) {
        T component = getComponent(type);
        if (component == null) {
            throw new IllegalStateException("Missing component: " + type.getSimpleName());
        }
        return component;
    }

    public boolean hasComponent(Class<? extends Component> type) {
        return components.containsKey(type);
    }

    public Collection<Component> getComponents() {
        return Collections.unmodifiableCollection(components.values());
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
