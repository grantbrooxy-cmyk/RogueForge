package com.rogueforge.game.engine;

import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.event.NpcScheduleEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeSystemTest {

    @Test
    void updateFiresNpcScheduleEventsAtMinuteResolution() {
        EventBus eventBus = new EventBus();
        TimeSystem timeSystem = new TimeSystem(eventBus);
        List<NpcScheduleEvent> events = new ArrayList<>();
        eventBus.subscribe(NpcScheduleEvent.class, events::add);

        timeSystem.setTimeOfDayHours(5.98f);
        timeSystem.update(1f);

        assertFalse(events.isEmpty());
        assertTrue(events.stream().anyMatch(NpcScheduleEvent::isPhaseChanged));
    }
}
