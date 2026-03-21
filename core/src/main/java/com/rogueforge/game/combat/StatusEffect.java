package com.rogueforge.game.combat;

/**
 * Base class for all status effects that can affect entities.
 * Status effects have a duration and tick at regular intervals.
 */
public abstract class StatusEffect {
    protected String name;
    protected float duration;       // Total duration in seconds
    protected float elapsed;        // Time elapsed in seconds
    protected float tickInterval;   // How often to tick in seconds
    protected float tickTimer;      // Accumulated time since last tick

    public StatusEffect(String name, float duration, float tickInterval) {
        this.name = name;
        this.duration = duration;
        this.tickInterval = tickInterval;
        this.elapsed = 0f;
        this.tickTimer = 0f;
    }

    /**
     * Abstract method to be implemented by subclasses.
     * Called every tick interval to apply the effect's behavior.
     *
     * @param target The entity affected by this status effect
     */
    protected abstract void tick(Object target);

    /**
     * Updates the status effect's internal timers.
     * Automatically calls tick() at the specified tick interval.
     *
     * @param delta Time elapsed since last update in seconds
     */
    public void update(float delta) {
        elapsed += delta;
        tickTimer += delta;

        // Execute tick if timer exceeds interval
        if (tickTimer >= tickInterval) {
            tick(null); // TODO: Pass actual target
            tickTimer -= tickInterval;
        }
    }

    /**
     * Checks if this status effect has expired.
     *
     * @return true if elapsed time >= duration
     */
    public boolean isExpired() {
        return elapsed >= duration;
    }

    public String getName() {
        return name;
    }

    public float getDuration() {
        return duration;
    }

    public float getElapsed() {
        return elapsed;
    }

    public float getTickInterval() {
        return tickInterval;
    }

    public float getRemainingDuration() {
        return Math.max(0, duration - elapsed);
    }

    /**
     * Burn effect - deals damage over time.
     * 5 damage per tick, ticks every 0.5 seconds.
     */
    public static class BurnEffect extends StatusEffect {
        private static final float BURN_DAMAGE = 5f;
        private static final float BURN_TICK_INTERVAL = 0.5f;

        public BurnEffect(float duration) {
            super("Burn", duration, BURN_TICK_INTERVAL);
        }

        @Override
        protected void tick(Object target) {
            // TODO: Apply 5 damage to target
            // target.takeDamage(BURN_DAMAGE);
        }
    }

    /**
     * Slow effect - reduces movement speed by 50%.
     * Applied passively, no per-tick logic needed.
     */
    public static class SlowEffect extends StatusEffect {
        public static final float SLOW_MULTIPLIER = 0.5f;

        public SlowEffect(float duration) {
            super("Slow", duration, Float.MAX_VALUE);
        }

        @Override
        protected void tick(Object target) {
            // No per-tick logic for slow effect
            // Speed reduction is applied when queried
        }

        public float getSpeedMultiplier() {
            return SLOW_MULTIPLIER;
        }
    }

    /**
     * Stun effect - prevents entity actions entirely.
     * Duration-based, no ticking needed.
     */
    public static class StunEffect extends StatusEffect {
        public StunEffect(float duration) {
            super("Stun", duration, Float.MAX_VALUE);
        }

        @Override
        protected void tick(Object target) {
            // No per-tick logic for stun effect
            // Stun is evaluated when entity tries to act
        }

        public boolean isStunned() {
            return !isExpired();
        }
    }
}
