package com.rogueforge.game.core;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple event bus for decoupled communication between game systems.
 * This implementation is intended for single-threaded LibGDX gameplay code.
 */
public class EventBus {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Class<?>, List<HandlerDefinition>> HANDLER_CACHE = new HashMap<>();
    private final Map<Class<?>, ListenerBucket> listeners = new HashMap<>();

    /**
     * Subscribes an object to listen for events.
     * Methods marked with {@link EventHandler} are preferred; legacy {@code onXxx(Event)}
     * single-argument methods are also supported as a fallback.
     *
     * @param subscriber The object that wants to listen for events
     */
    public synchronized void subscribe(Object subscriber) {
        if (subscriber == null) {
            return;
        }
        for (HandlerDefinition definition : getHandlerDefinitions(subscriber.getClass())) {
            Consumer<Object> handler = createHandler(subscriber, definition.method, definition.eventType);
            listeners.computeIfAbsent(definition.eventType, ignored -> new ListenerBucket())
                .add(new EventListener(subscriber, handler));
        }
    }

    /**
     * Subscribes a direct listener for one event type without reflective method scanning.
     *
     * @param eventType The event class to listen for
     * @param listener The event consumer
     * @param <T> The event type
     */
    public synchronized <T> void subscribe(Class<T> eventType, Consumer<? super T> listener) {
        if (eventType == null || listener == null) {
            return;
        }
        listeners.computeIfAbsent(eventType, ignored -> new ListenerBucket())
            .add(new EventListener(listener, event -> listener.accept(eventType.cast(event))));
    }

    /**
     * Unsubscribes an object from the event bus.
     *
     * @param subscriber The object to unsubscribe
     */
    public synchronized void unsubscribe(Object subscriber) {
        if (subscriber == null) {
            return;
        }
        listeners.values().removeIf(bucket -> {
            bucket.removeSubscriber(subscriber);
            return bucket.isEmpty();
        });
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

        EventListener[] snapshot;
        synchronized (this) {
            ListenerBucket bucket = listeners.get(event.getClass());
            snapshot = bucket != null ? bucket.snapshot : null;
        }
        if (snapshot == null || snapshot.length == 0) {
            return;
        }
        for (EventListener listener : snapshot) {
            try {
                listener.handler.accept(event);
            } catch (Exception e) {
                System.err.println("Error firing event: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the number of listeners for a specific event type.
     *
     * @param eventType The event class
     * @return The number of listeners
     */
    public synchronized int getListenerCount(Class<?> eventType) {
        ListenerBucket bucket = listeners.get(eventType);
        return bucket != null ? bucket.listeners.size() : 0;
    }

    /**
     * Clears all listeners from the bus.
     */
    public synchronized void clear() {
        listeners.clear();
    }

    /**
     * Internal class to hold listener information.
     */
    private static class EventListener {
        final Object subscriber;
        final Consumer<Object> handler;

        EventListener(Object subscriber, Consumer<Object> handler) {
            this.subscriber = subscriber;
            this.handler = handler;
        }
    }

    private static class HandlerDefinition {
        final Method method;
        final Class<?> eventType;

        HandlerDefinition(Method method, Class<?> eventType) {
            this.method = method;
            this.eventType = eventType;
        }
    }

    private static class ListenerBucket {
        final List<EventListener> listeners = new ArrayList<>();
        EventListener[] snapshot = new EventListener[0];

        void add(EventListener listener) {
            listeners.add(listener);
            refreshSnapshot();
        }

        void removeSubscriber(Object subscriber) {
            listeners.removeIf(listener -> listener.subscriber == subscriber);
            refreshSnapshot();
        }

        boolean isEmpty() {
            return listeners.isEmpty();
        }

        private void refreshSnapshot() {
            snapshot = listeners.toArray(new EventListener[0]);
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

    private static synchronized List<HandlerDefinition> getHandlerDefinitions(Class<?> subscriberClass) {
        List<HandlerDefinition> cached = HANDLER_CACHE.get(subscriberClass);
        if (cached != null) {
            return cached;
        }

        List<HandlerDefinition> definitions = new ArrayList<>();
        for (Method method : subscriberClass.getDeclaredMethods()) {
            if (!isEventHandlerMethod(method)) {
                continue;
            }
            definitions.add(new HandlerDefinition(method, method.getParameterTypes()[0]));
        }

        List<HandlerDefinition> immutable = Collections.unmodifiableList(definitions);
        HANDLER_CACHE.put(subscriberClass, immutable);
        return immutable;
    }

    private static boolean isEventHandlerMethod(Method method) {
        if (method.getParameterCount() != 1) {
            return false;
        }
        return method.isAnnotationPresent(EventHandler.class)
            || method.getName().startsWith("on");
    }
}
