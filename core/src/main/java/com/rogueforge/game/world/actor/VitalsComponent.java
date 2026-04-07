package com.rogueforge.game.world.actor;

/**
 * Shared health/alive state for an overworld actor.
 */
public class VitalsComponent implements com.rogueforge.game.entity.component.Component {
    public float health;
    public float maxHealth;
    public boolean alive = true;
}
