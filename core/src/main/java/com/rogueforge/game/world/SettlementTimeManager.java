package com.rogueforge.game.world;

/**
 * Simple day/night clock for settlements and NPC schedules.
 */
public class SettlementTimeManager {
    private static final float HOURS_PER_DAY = 24f;
    private static final float DEFAULT_SECONDS_PER_DAY = 360f;

    private float timeOfDayHours = 8f;
    private float secondsPerDay = DEFAULT_SECONDS_PER_DAY;

    public void update(float delta) {
        if (delta <= 0f) {
            return;
        }
        timeOfDayHours += (HOURS_PER_DAY / secondsPerDay) * delta;
        while (timeOfDayHours >= HOURS_PER_DAY) {
            timeOfDayHours -= HOURS_PER_DAY;
        }
    }

    public float getTimeOfDayHours() {
        return timeOfDayHours;
    }

    public void setTimeOfDayHours(float timeOfDayHours) {
        float normalized = timeOfDayHours % HOURS_PER_DAY;
        this.timeOfDayHours = normalized < 0f ? normalized + HOURS_PER_DAY : normalized;
    }

    public boolean isNight() {
        return timeOfDayHours < 6f || timeOfDayHours >= 19f;
    }

    public float getDaylightStrength() {
        if (timeOfDayHours < 5f || timeOfDayHours >= 21f) {
            return 0.2f;
        }
        if (timeOfDayHours < 7f) {
            return 0.2f + ((timeOfDayHours - 5f) / 2f) * 0.8f;
        }
        if (timeOfDayHours >= 18f) {
            return 1f - ((timeOfDayHours - 18f) / 3f) * 0.8f;
        }
        return 1f;
    }

    public String describePhase() {
        return describePhase(timeOfDayHours);
    }

    public static String describePhase(float timeOfDayHours) {
        if (timeOfDayHours < 6f) {
            return "Night";
        }
        if (timeOfDayHours < 9f) {
            return "Morning";
        }
        if (timeOfDayHours < 17f) {
            return "Day";
        }
        if (timeOfDayHours < 20f) {
            return "Evening";
        }
        return "Night";
    }

    public boolean isWithinPhase(String requiredPhase) {
        if (requiredPhase == null || requiredPhase.isEmpty()) {
            return true;
        }
        return describePhase().equalsIgnoreCase(requiredPhase);
    }
}
