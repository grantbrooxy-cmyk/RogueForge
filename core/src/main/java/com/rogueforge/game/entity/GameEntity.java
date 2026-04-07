package com.rogueforge.game.entity;

import com.rogueforge.game.entity.component.Component;
import java.util.Collection;
import java.util.UUID;

/**
 * Lightweight base entity that owns a typed component map.
 *
 * <p>This is intentionally small: it gives us constructor-injected composition
 * without committing the whole project to a full ECS yet.</p>
 */
public abstract class GameEntity {
    private final UUID id = UUID.randomUUID();
    private final ComponentManager componentManager = new ComponentManager();
    private String entityId;

    protected GameEntity() {
    }

    protected GameEntity(String entityId) {
        this.entityId = entityId;
    }

    public UUID getEntityUuid() {
        return id;
    }

    public <T extends Component> T addComponent(T component) {
        return componentManager.add(component);
    }

    public <T extends Component> T addComponent(Class<T> type, T component) {
        return componentManager.add(type, component);
    }

    protected <T extends Component> T registerComponent(Class<T> type, T component) {
        return addComponent(type, component);
    }

    public <T extends Component> T getComponent(Class<T> type) {
        return componentManager.get(type);
    }

    public <T extends Component> T requireComponent(Class<T> type) {
        T component = getComponent(type);
        if (component == null) {
            throw new IllegalStateException("Missing component: " + type.getSimpleName());
        }
        return component;
    }

    public boolean hasComponent(Class<? extends Component> type) {
        return componentManager.has(type);
    }

    public Collection<Component> getComponents() {
        return componentManager.getAll();
    }

    public <T extends Component> T removeComponent(Class<T> type) {
        return componentManager.remove(type);
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
