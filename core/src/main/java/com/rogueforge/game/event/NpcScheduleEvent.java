package com.rogueforge.game.event;

/**
 * Fired once per in-game minute so schedule-driven systems can react to time.
 */
public class NpcScheduleEvent {
    private final int day;
    private final int hour;
    private final int minute;
    private final float timeOfDayHours;
    private final String phase;
    private final boolean phaseChanged;

    public NpcScheduleEvent(int day, int hour, int minute, float timeOfDayHours, String phase, boolean phaseChanged) {
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.timeOfDayHours = timeOfDayHours;
        this.phase = phase;
        this.phaseChanged = phaseChanged;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public float getTimeOfDayHours() {
        return timeOfDayHours;
    }

    public String getPhase() {
        return phase;
    }

    public boolean isPhaseChanged() {
        return phaseChanged;
    }
}
