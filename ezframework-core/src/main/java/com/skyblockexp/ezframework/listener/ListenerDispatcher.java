package com.skyblockexp.ezframework.listener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Core dispatcher for {@link PacketListenerInterface} instances.
 *
 * <p>{@code ListenerDispatcher} is composed inside the proxy messenger
 * implementations ({@code VelocityEzMessenger}, {@code BungeeEzMessenger}).
 * It handles two responsibilities:
 *
 * <ol>
 *   <li><b>Registration</b> — scans a listener class for methods annotated
 *       with {@link PacketListener}, extracts their parameter type as the
 *       event key, and stores them sorted by {@link EventPriority}.</li>
 *   <li><b>Dispatch</b> — given an event object, invokes every registered
 *       handler whose parameter type matches, in priority order, honouring
 *       {@link PacketListener#ignoreCancelled()} for {@link Cancellable} events.</li>
 * </ol>
 *
 * <h2>Thread safety</h2>
 * <p>Registration ({@link #registerListener}) is <em>not</em> thread-safe and
 * should be called during plugin initialisation on the main thread. Dispatch
 * ({@link #fire}) is safe to call from any thread once registration is complete,
 * as the handler map is not mutated during dispatch.
 *
 * @see ListenerMessenger
 * @see PacketListener
 */
public final class ListenerDispatcher {

    /** Maps event class to the ordered list of handlers registered for it. */
    private final Map<Class<?>, List<RegisteredHandler>> handlerMap = new HashMap<>();

    /**
     * Scans {@code listener} for methods annotated with {@link PacketListener}
     * and registers each qualifying method as a handler for its parameter type.
     *
     * <p>A method qualifies if it:
     * <ul>
     *   <li>carries a {@code @PacketListener} annotation</li>
     *   <li>declares exactly one parameter (the event type)</li>
     * </ul>
     *
     * <p>After registration all handler lists are re-sorted by
     * {@link EventPriority} ordinal so that handlers from different listener
     * classes interleave correctly.
     *
     * @param listener the listener whose methods should be registered;
     *                 must not be {@code null}
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    public void registerListener(PacketListenerInterface listener) {
        Objects.requireNonNull(listener, "listener");

        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(PacketListener.class)) continue;
            if (method.getParameterCount() != 1) continue;

            Class<?> eventType = method.getParameterTypes()[0];
            PacketListener annotation = method.getAnnotation(PacketListener.class);

            handlerMap
                .computeIfAbsent(eventType, k -> new ArrayList<>())
                .add(new RegisteredHandler(
                    listener,
                    method,
                    annotation.priority(),
                    annotation.ignoreCancelled()
                ));
        }

        handlerMap.values().forEach(list ->
            list.sort(Comparator.comparingInt(h -> h.priority().ordinal()))
        );
    }

    /**
     * Dispatches {@code event} to all handlers registered for its type,
     * in {@link EventPriority} order.
     *
     * <p>If a handler is annotated with
     * {@link PacketListener#ignoreCancelled() ignoreCancelled = true} and
     * {@code event} implements {@link Cancellable} and is currently cancelled,
     * that handler is skipped.
     *
     * <p>Any exception thrown by a handler is printed to stderr and execution
     * continues with the next handler — one failing handler never prevents the
     * rest from running.
     *
     * <p>If no handlers are registered for the event type, this method returns
     * immediately without allocating.
     *
     * @param <T>   the event type
     * @param event the event to dispatch; must not be {@code null}
     * @return {@code event}, unchanged, after all handlers have been invoked
     * @throws NullPointerException if {@code event} is {@code null}
     */
    public <T> T fire(T event) {
        Objects.requireNonNull(event, "event");

        List<RegisteredHandler> handlers = handlerMap.get(event.getClass());
        if (handlers == null) return event;

        for (RegisteredHandler handler : handlers) {
            if (handler.ignoreCancelled() && event instanceof Cancellable c && c.isCancelled()) {
                continue;
            }
            try {
                handler.method().invoke(handler.listener(), event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return event;
    }

    /**
     * Returns the number of handler methods registered for a given event type.
     * Primarily intended for testing.
     *
     * @param eventType the event class to query
     * @return the number of registered handlers, or {@code 0} if none
     */
    public int handlerCount(Class<?> eventType) {
        List<RegisteredHandler> handlers = handlerMap.get(eventType);
        return handlers == null ? 0 : handlers.size();
    }

    // -----------------------------------------------------------------------
    // Internal record
    // -----------------------------------------------------------------------

    /**
     * Immutable snapshot of a single registered handler method.
     *
     * @param listener         the listener instance that owns the method
     * @param method           the handler method to invoke
     * @param priority         the execution priority
     * @param ignoreCancelled  whether to skip invocation for cancelled events
     */
    private record RegisteredHandler(
        PacketListenerInterface listener,
        Method method,
        EventPriority priority,
        boolean ignoreCancelled
    ) {}
}
