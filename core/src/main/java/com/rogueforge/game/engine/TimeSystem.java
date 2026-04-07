package com.rogueforge.game.engine;

import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.event.NpcScheduleEvent;
import com.rogueforge.game.world.SettlementTimeManager;

/**
 * Shared in-game clock that emits minute-level schedule events.
 */
public class TimeSystem {
    private final EventBus eventBus;
    private final SettlementTimeManager clock = new SettlementTimeManager();
    private int day = 1;
    private int lastMinuteOfDay = toMinuteOfDay(clock.getTimeOfDayHours());
    private String lastPhase = clock.describePhase();

    public TimeSystem(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void update(float delta) {
        if (delta <= 0f) {
            return;
        }
        float beforeHours = clock.getTimeOfDayHours();
        int beforeMinute = toMinuteOfDay(beforeHours);
        String beforePhase = clock.describePhase();

        clock.update(delta);

        float afterHours = clock.getTimeOfDayHours();
        int afterMinute = toMinuteOfDay(afterHours);
        if (afterHours < beforeHours) {
            day++;
        }

        int absoluteBefore = ((day - (afterHours < beforeHours ? 1 : 0)) * 1440) + beforeMinute;
        int absoluteAfter = (day * 1440) + afterMinute;
        for (int absoluteMinute = absoluteBefore + 1; absoluteMinute <= absoluteAfter; absoluteMinute++) {
            int eventDay = Math.max(1, absoluteMinute / 1440);
            int minuteOfDay = Math.floorMod(absoluteMinute, 1440);
            float eventHours = minuteOfDay / 60f;
            String phase = SettlementTimeManager.describePhase(eventHours);
            boolean phaseChanged = !phase.equalsIgnoreCase(beforePhase) || !phase.equalsIgnoreCase(lastPhase);
            if (eventBus != null) {
                eventBus.fire(new NpcScheduleEvent(
                    eventDay,
                    minuteOfDay / 60,
                    minuteOfDay % 60,
                    eventHours,
                    phase,
                    phaseChanged
                ));
            }
            beforePhase = phase;
            lastPhase = phase;
            lastMinuteOfDay = minuteOfDay;
        }
    }

    public SettlementTimeManager getClock() {
        return clock;
    }

    public float getTimeOfDayHours() {
        return clock.getTimeOfDayHours();
    }

    public void setTimeOfDayHours(float timeOfDayHours) {
        clock.setTimeOfDayHours(timeOfDayHours);
        lastMinuteOfDay = toMinuteOfDay(clock.getTimeOfDayHours());
        lastPhase = clock.describePhase();
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = Math.max(1, day);
    }

    private static int toMinuteOfDay(float hours) {
        int minute = (int) Math.floor(hours * 60f);
        return Math.floorMod(minute, 1440);
    }
}
