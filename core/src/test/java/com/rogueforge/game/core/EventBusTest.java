package com.rogueforge.game.core;

import com.rogueforge.game.event.DamageDealtEvent;
import com.rogueforge.game.event.EntityKilledEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventBusTest {

    @Test
    void subscribeFireUnsubscribeAndClearWork() {
        EventBus bus = new EventBus();
        RecordingSubscriber subscriber = new RecordingSubscriber();

        bus.subscribe(subscriber);
        assertEquals(1, bus.getListenerCount(DamageDealtEvent.class));
        assertEquals(1, bus.getListenerCount(EntityKilledEvent.class));

        DamageDealtEvent damage = new DamageDealtEvent("attacker", "target", 42f);
        EntityKilledEvent killed = new EntityKilledEvent("target");
        bus.fire(damage);
        bus.fire(killed);

        assertEquals(damage, subscriber.lastDamage);
        assertEquals(killed, subscriber.lastKilled);

        bus.unsubscribe(subscriber);
        assertEquals(0, bus.getListenerCount(DamageDealtEvent.class));

        bus.clear();
        assertEquals(0, bus.getListenerCount(EntityKilledEvent.class));
    }

    private static class RecordingSubscriber {
        private DamageDealtEvent lastDamage;
        private EntityKilledEvent lastKilled;

        void onDamageDealtEvent(DamageDealtEvent event) {
            this.lastDamage = event;
        }

        void onEntityKilledEvent(EntityKilledEvent event) {
            this.lastKilled = event;
        }
    }
}
