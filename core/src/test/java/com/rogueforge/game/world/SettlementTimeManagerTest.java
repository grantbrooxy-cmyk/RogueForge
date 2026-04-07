package com.rogueforge.game.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementTimeManagerTest {

    @Test
    void phaseChecksMatchDescribedTimeOfDay() {
        SettlementTimeManager manager = new SettlementTimeManager();
        manager.setTimeOfDayHours(8.5f);

        assertTrue(manager.isWithinPhase("Morning"));
        assertFalse(manager.isWithinPhase("Night"));

        manager.setTimeOfDayHours(21f);
        assertTrue(manager.isWithinPhase("Night"));
    }
}
