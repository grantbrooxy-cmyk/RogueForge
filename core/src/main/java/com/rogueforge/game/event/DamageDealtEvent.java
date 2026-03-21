package com.rogueforge.game.event;

/**
 * Event fired when damage is dealt from one entity to another.
 */
public class DamageDealtEvent {
    private Object source;
    private Object target;
    private float damage;

    public DamageDealtEvent(Object source, Object target, float damage) {
        this.source = source;
        this.target = target;
        this.damage = damage;
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
    public String toString() {
        return "DamageDealtEvent{" +
                "source=" + source +
                ", target=" + target +
                ", damage=" + damage +
                '}';
    }
}
