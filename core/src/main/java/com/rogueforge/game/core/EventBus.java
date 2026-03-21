package com.rogueforge.game.core;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Simple event bus for decoupled communication between game systems.
 * Subscribers can register listeners and receive events based on method annotations.
 */
public class EventBus {
    private Map<Class<?>, List<EventListener>> listeners = new HashMap<>();

    /**
     * Subscribes an object to listen for events.
     * The object's methods will be scanned for EventHandler annotations.
     *
     * @param subscriber The object that wants to listen for events
     */
    public void subscribe(Object subscriber) {
        Class<?> clazz = subscriber.getClass();

        // Scan all methods for @EventHandler annotation
        for (Method method : clazz.getDeclaredMethods()) {
            // Check if method has EventHandler annotation (if we were using annotations)
            // For now, use naming convention: onEventName(EventType event)
            if (method.getName().startsWith("on") && method.getParameterCount() == 1) {
                Class<?> eventType = method.getParameterTypes()[0];

                // Make method accessible
                method.setAccessible(true);

                // Add listener
                listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                    .add(new EventListener(subscriber, method));
            }
        }
    }

    /**
     * Unsubscribes an object from the event bus.
     *
     * @param subscriber The object to unsubscribe
     */
    public void unsubscribe(Object subscriber) {
        for (List<EventListener> eventListeners : listeners.values()) {
            eventListeners.removeIf(listener -> listener.subscriber == subscriber);
        }
    }

    /**
     * Fires an event to all subscribed listeners.
     *
     * @param event The event to fire
     */
    public void fire(Object event) {
        if (event == null) {
            return;
        }

        Class<?> eventType = event.getClass();
        List<EventListener> eventListeners = listeners.get(eventType);

        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                try {
                    listener.method.invoke(listener.subscriber, event);
                } catch (Exception e) {
                    System.err.println("Error firing event: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets the number of listeners for a specific event type.
     *
     * @param eventType The event class
     * @return The number of listeners
     */
    public int getListenerCount(Class<?> eventType) {
        List<EventListener> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }

    /**
     * Clears all listeners from the bus.
     */
    public void clear() {
        listeners.clear();
    }

    /**
     * Internal class to hold listener information.
     */
    private static class EventListener {
        Object subscriber;
        Method method;

        EventListener(Object subscriber, Method method) {
            this.subscriber = subscriber;
            this.method = method;
        }
    }
}
