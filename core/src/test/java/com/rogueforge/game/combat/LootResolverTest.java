package com.rogueforge.game.combat;

import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.data.MonsterDefinition;
import com.rogueforge.game.event.CurrencyEarnedEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LootResolverTest {

    @Test
    void monsterKillQueuesCurrencyUsingRankMultiplier() {
        EventBus bus = new EventBus();
        RecordingSubscriber subscriber = new RecordingSubscriber();
        bus.subscribe(CurrencyEarnedEvent.class, event -> subscriber.currencyEarnedEvent = event);
        LootResolver resolver = new LootResolver(bus);
        resolver.setEquipmentPool(java.util.List.of());

        MonsterDefinition monster = new MonsterDefinition(
            "drake",
            "Drake",
            "C",
            100,
            20,
            10,
            12,
            5,
            "AGGRO",
            new String[0]
        );

        bus.fire(new com.rogueforge.game.event.EntityKilledEvent(monster));
        bus.processQueuedEvents();

        assertNotNull(subscriber.currencyEarnedEvent);
        assertEquals(80L, subscriber.currencyEarnedEvent.getAmount());
        assertEquals("Drake", subscriber.currencyEarnedEvent.getSource());
    }

    private static class RecordingSubscriber {
        private CurrencyEarnedEvent currencyEarnedEvent;
    }
}
