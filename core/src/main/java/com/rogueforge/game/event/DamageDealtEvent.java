package com.rogueforge.game.event;

import com.badlogic.gdx.utils.Pool;

/**
 * Event fired when damage is dealt from one entity to another.
 */
public class DamageDealtEvent implements Pool.Poolable {
    private Object source;
    private Object target;
    private float damage;

    public DamageDealtEvent() {
    }

    public DamageDealtEvent(Object source, Object target, float damage) {
        init(source, target, damage);
    }

    public DamageDealtEvent init(Object source, Object target, float damage) {
        this.source = source;
        this.target = target;
        this.damage = damage;
        return this;
    }

    public Object getSource() {
        return source;
    }

    public Object getTarget() {
        return target;
    }

    public float getDamage() {
        return damage;
    }

    @Override
    public void reset() {
        // Intentionally left populated so listeners/tests can inspect the event
        // safely after dispatch. obtain()/init(...) always overwrite the fields.
    }

    @Override
    public String toString() {
        return "DamageDealtEvent{" +
                "source=" + source +
                ", target=" + target +
                ", damage=" + damage +
                '}';
    }
}
