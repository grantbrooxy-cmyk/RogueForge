package com.rogueforge.game.world;

import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.core.GameState;
import com.rogueforge.game.data.ZoneDefinition;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicWorldEventSystemTest {

    @Test
    void bossClearSpawnsFollowUpWorldEventsOnlyOnce() {
        DynamicWorldEventSystem system = new DynamicWorldEventSystem(new EventBus());
        WorldStateManager worldStateManager = new WorldStateManager();
        GameState state = new GameState("Tester");
        worldStateManager.initialize(state);

        ZoneDefinition clearedZone = new ZoneDefinition("shadow_caves", "Shadow Caves", "maps/shadow.tmx", "D", "C", "boss", new String[]{"enemy"});
        ZoneDefinition alternateZone = new ZoneDefinition("rusty_quarry", "Rusty Quarry", "maps/quarry.tmx", "D", "C", "boss2", new String[]{"enemy"});

        List<DynamicWorldEventSystem.DynamicWorldEvent> firstPass = system.handleBossClear(
            "shadow_boss",
            "shadow_caves",
            List.of(clearedZone, alternateZone),
            state,
            worldStateManager
        );
        List<DynamicWorldEventSystem.DynamicWorldEvent> secondPass = system.handleBossClear(
            "shadow_boss",
            "shadow_caves",
            List.of(clearedZone, alternateZone),
            state,
            worldStateManager
        );

        assertFalse(firstPass.isEmpty());
        assertTrue(firstPass.stream().anyMatch(event -> event.getType() == DynamicWorldEventSystem.DynamicWorldEvent.Type.CONTRACT));
        assertEquals(0, secondPass.size());
    }
}
