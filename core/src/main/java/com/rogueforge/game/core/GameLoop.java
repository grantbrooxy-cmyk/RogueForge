package com.rogueforge.game.core;

/**
 * Fixed timestep game loop with accumulator pattern.
 * Separates variable-rate rendering from fixed-rate physics updates.
 * Uses a fixed timestep of 1/60th of a second (60 FPS) for physics.
 * Rendering happens at variable framerate but physics updates are consistent.
 */
public class GameLoop {
    private static final String TAG = "GameLoop";

    /**
     * Fixed physics timestep: 1/60th second (60 updates per second).
     */
    public static final float FIXED_STEP = 1.0f / 60.0f;

    /**
     * Maximum accumulator time to prevent "spiral of death".
     * If delta is larger than this, the accumulator is capped.
     */
    public static final float MAX_ACCUMULATOR = 0.25f;

    /**
     * Functional interface for fixed timestep physics callbacks.
     */
    @FunctionalInterface
    public interface FixedStepCallback {
        /**
         * Called at fixed timesteps for physics and logic updates.
         * This is called potentially multiple times per frame if
         * the frame delta is large.
         *
         * @param deltaTime the fixed timestep (1/60th second)
         */
        void onFixedStep(float deltaTime);
    }

    /**
     * Functional interface for variable-rate rendering callbacks.
     */
    @FunctionalInterface
    public interface RenderCallback {
        /**
         * Called once per rendered frame.
         * This is called at the actual framerate (variable).
         * Use this for interpolation and visual updates.
         *
         * @param deltaTime the time since the last frame (variable)
         * @param accumulator the accumulated physics time (0.0 to FIXED_STEP)
         */
        void onRender(float deltaTime, float accumulator);
    }

    private float accumulator;
    private FixedStepCallback fixedStepCallback;
    private RenderCallback renderCallback;

    /**
     * Create a new GameLoop with optional callbacks.
     *
     * @param fixedStepCallback callback for fixed timestep updates, or null
     * @param renderCallback callback for variable-rate rendering, or null
     */
    public GameLoop(FixedStepCallback fixedStepCallback, RenderCallback renderCallback) {
        this.accumulator = 0.0f;
        this.fixedStepCallback = fixedStepCallback;
        this.renderCallback = renderCallback;
    }

    /**
     * Create a new GameLoop without callbacks.
     * Use setFixedStepCallback() and setRenderCallback() to set them later.
     */
    public GameLoop() {
        this(null, null);
    }

    /**
     * Update the game loop with delta time from the rendering frame.
     * Accumulates delta time and calls fixed step callbacks as needed.
     * Then calls render callback with interpolation factor.
     *
     * @param deltaTime time elapsed since last frame, in seconds
     */
    public void update(float deltaTime) {
        // Cap delta time to prevent spiral of death
        if (deltaTime > MAX_ACCUMULATOR) {
            deltaTime = MAX_ACCUMULATOR;
        }

        // Accumulate time
        accumulator += deltaTime;

        // Process fixed timesteps
        while (accumulator >= FIXED_STEP) {
            if (fixedStepCallback != null) {
                fixedStepCallback.onFixedStep(FIXED_STEP);
            }
            accumulator -= FIXED_STEP;
        }

        // Render with interpolation support
        if (renderCallback != null) {
            renderCallback.onRender(deltaTime, accumulator);
        }
    }

    /**
     * Reset the accumulator.
     * Useful after pausing or loading to prevent large jumps.
     */
    public void reset() {
        accumulator = 0.0f;
    }

    /**
     * Set the fixed timestep callback.
     *
     * @param callback the callback to invoke at each fixed step, or null
     */
    public void setFixedStepCallback(FixedStepCallback callback) {
        this.fixedStepCallback = callback;
    }

    /**
     * Set the render callback.
     *
     * @param callback the callback to invoke for rendering, or null
     */
    public void setRenderCallback(RenderCallback callback) {
        this.renderCallback = callback;
    }

    /**
     * Get the current accumulator value.
     * Useful for interpolation calculations.
     *
     * @return the current accumulator (0.0 to FIXED_STEP)
     */
    public float getAccumulator() {
        return accumulator;
    }

    /**
     * Get the interpolation factor (0.0 to 1.0).
     * Represents how far we are between the last and next fixed step.
     * Useful for smooth interpolation in rendering.
     *
     * @return interpolation factor (accumulator / FIXED_STEP)
     */
    public float getInterpolationFactor() {
        if (FIXED_STEP == 0.0f) {
            return 0.0f;
        }
        return accumulator / FIXED_STEP;
    }
}
