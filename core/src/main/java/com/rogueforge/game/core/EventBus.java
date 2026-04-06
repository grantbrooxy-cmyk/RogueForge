package com.rogueforge.game.core;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * Simple event bus for decoupled communication between game systems.
 * Subscribers can register listeners and receive events based on method annotations.
 */
public class EventBus {
    private final Map<Class<?>, List<EventListener>> listeners = new HashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

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
                Consumer<Object> handler = createHandler(subscriber, method, eventType);
                listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                    .add(new EventListener(subscriber, handler));
            }
        }
    }

    /**
     * Subscribes a direct listener for one event type without reflective method scanning.
     *
     * @param eventType The event class to listen for
     * @param listener The event consumer
     * @param <T> The event type
     */
    public <T> void subscribe(Class<T> eventType, Consumer<? super T> listener) {
        if (eventType == null || listener == null) {
            return;
        }
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
            .add(new EventListener(listener, event -> listener.accept(eventType.cast(event))));
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
            for (EventListener listener : List.copyOf(eventListeners)) {
                try {
                    listener.handler.accept(event);
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
        Consumer<Object> handler;

        EventListener(Object subscriber, Consumer<Object> handler) {
            this.subscriber = subscriber;
            this.handler = handler;
        }
    }

    private Consumer<Object> createHandler(Object subscriber, Method method, Class<?> eventType) {
        try {
            Class<?> subscriberClass = subscriber.getClass();
            MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(
                subscriberClass,
                LOOKUP
            );
            MethodHandle methodHandle = privateLookup.unreflect(method);
            MethodType samMethodType = MethodType.methodType(void.class, Object.class);
            MethodType instantiatedMethodType = MethodType.methodType(void.class, eventType);
            CallSite callSite = LambdaMetafactory.metafactory(
                privateLookup,
                "accept",
                MethodType.methodType(Consumer.class, subscriberClass),
                samMethodType,
                methodHandle,
                instantiatedMethodType
            );
            @SuppressWarnings("unchecked")
            Consumer<Object> handler = (Consumer<Object>) callSite.getTarget().invoke(subscriber);
            return handler;
        } catch (Throwable throwable) {
            throw new IllegalStateException(
                "Failed to register event handler " + method.getName()
                    + " on " + subscriber.getClass().getName(),
                throwable
            );
        }
    }
}
