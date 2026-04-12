package com.skyblockexp.ezframework.proxy.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Annotation-driven event bus for {@link ProxyPacketEvent} instances.
 *
 * <p>{@code ProxyEventManager} discovers handlers by reflecting over
 * {@link PacketListener}-annotated methods on registered listener objects,
 * then dispatches events to those handlers in {@link EventPriority} order.
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>Construct a manager, typically once per plugin bootstrap.</li>
 *   <li>Call {@link #registerListener(Object)} for each listener object.</li>
 *   <li>Call {@link #fireEvent(ProxyPacketEvent)} whenever a packet event
 *       should be dispatched.</li>
 * </ol>
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * ProxyEventManager manager = new ProxyEventManager(logger);
 * manager.registerListener(new MyProxyListener());
 *
 * PlayerLoginPacketEvent event = new PlayerLoginPacketEvent(context, playerName);
 * manager.fireEvent(event);
 * }</pre>
 *
 * <h2>Thread safety</h2>
 * <p>The internal handler map is a {@link ConcurrentHashMap}; individual handler
 * lists are rebuilt (copy-on-write) during registration and unregistration.
 * {@link #fireEvent(ProxyPacketEvent)} iterates a stable snapshot and is safe to
 * call from multiple threads concurrently.
 *
 * @see PacketListener
 * @see EventPriority
 * @see ProxyPacketEvent
 */
public final class ProxyEventManager {

    private static final Comparator<RegisteredHandler> BY_PRIORITY =
            Comparator.comparingInt(h -> h.priority().ordinal());

    private final Logger logger;

    /**
     * Handlers keyed by the concrete {@link ProxyPacketEvent} subtype.
     * Values are <em>immutable</em> snapshots sorted by {@link EventPriority}.
     */
    private final Map<Class<? extends ProxyPacketEvent>, List<RegisteredHandler>> handlerMap =
            new ConcurrentHashMap<>();

    /**
     * Construct a manager that logs handler exceptions to the given logger.
     *
     * @param logger the logger to use; must not be {@code null}
     */
    public ProxyEventManager(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Scan {@code listener} for {@link PacketListener}-annotated methods and
     * register each as a handler.
     *
     * <p>Each annotated method must be {@code public} and accept exactly one
     * parameter whose type extends {@link ProxyPacketEvent}. Violations throw
     * {@link IllegalArgumentException} immediately, before any handler from the
     * same listener object is registered; all-or-nothing per call.
     *
     * @param listener the listener object to scan; must not be {@code null}
     * @throws IllegalArgumentException if any annotated method has an invalid signature
     */
    public void registerListener(Object listener) {
        Objects.requireNonNull(listener, "listener");

        List<RegisteredHandler> toAdd = new ArrayList<>();
        for (Method method : listener.getClass().getMethods()) {
            PacketListener annotation = method.getAnnotation(PacketListener.class);
            if (annotation == null) {
                continue;
            }
            validateMethod(method);
            @SuppressWarnings("unchecked")
            Class<? extends ProxyPacketEvent> eventType =
                    (Class<? extends ProxyPacketEvent>) method.getParameterTypes()[0];
            toAdd.add(new RegisteredHandler(listener, method, eventType,
                    annotation.priority(), annotation.ignoreCancelled()));
        }

        // Merge into the handler map using copy-on-write per event type
        for (RegisteredHandler handler : toAdd) {
            handlerMap.compute(handler.eventType(), (type, existing) -> {
                List<RegisteredHandler> next = new ArrayList<>(
                        existing == null ? Collections.emptyList() : existing);
                next.add(handler);
                next.sort(BY_PRIORITY);
                return Collections.unmodifiableList(next);
            });
        }
    }

    /**
     * Remove all handlers that were registered by the given listener object.
     *
     * <p>If the listener was never registered this method is a no-op.
     *
     * @param listener the listener object to remove; must not be {@code null}
     */
    public void unregisterAll(Object listener) {
        Objects.requireNonNull(listener, "listener");

        handlerMap.replaceAll((type, existing) -> {
            List<RegisteredHandler> next = new ArrayList<>(existing);
            next.removeIf(h -> h.listenerInstance() == listener);
            if (next.isEmpty()) {
                return Collections.emptyList();
            }
            next.sort(BY_PRIORITY);
            return Collections.unmodifiableList(next);
        });
    }

    // -------------------------------------------------------------------------
    // Dispatch
    // -------------------------------------------------------------------------

    /**
     * Fire the given event, invoking all matching handlers in
     * {@link EventPriority} order.
     *
     * <p>Cancellation behaviour:
     * <ul>
     *   <li>Handlers with {@link PacketListener#ignoreCancelled() ignoreCancelled = true}
     *       are skipped if the event is cancelled at the moment they would be called.</li>
     *   <li>Handlers at {@link EventPriority#MONITOR} always execute regardless of
     *       cancellation state or the {@code ignoreCancelled} flag.</li>
     * </ul>
     *
     * <p>If a handler method throws any exception the manager logs a warning and
     * continues dispatching to remaining handlers.
     *
     * @param event the event to fire; must not be {@code null}
     * @param <E>   the concrete event type
     */
    public <E extends ProxyPacketEvent> void fireEvent(E event) {
        Objects.requireNonNull(event, "event");

        List<RegisteredHandler> handlers = handlerMap.get(event.getClass());
        if (handlers == null || handlers.isEmpty()) {
            return;
        }

        for (RegisteredHandler handler : handlers) {
            boolean isMonitor = handler.priority() == EventPriority.MONITOR;
            if (!isMonitor && handler.ignoreCancelled() && event.isCancelled()) {
                continue;
            }
            try {
                handler.method().invoke(handler.listenerInstance(), event);
            } catch (Exception e) {
                Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                logger.warning("[ProxyEventManager] Handler "
                        + handler.listenerInstance().getClass().getSimpleName()
                        + "#" + handler.method().getName()
                        + " threw an exception: " + cause.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static void validateMethod(Method method) {
        Class<?>[] params = method.getParameterTypes();
        if (params.length != 1) {
            throw new IllegalArgumentException(
                    "@PacketListener method " + method.getDeclaringClass().getSimpleName()
                            + "#" + method.getName()
                            + " must have exactly one parameter, found " + params.length);
        }
        if (!ProxyPacketEvent.class.isAssignableFrom(params[0])) {
            throw new IllegalArgumentException(
                    "@PacketListener method " + method.getDeclaringClass().getSimpleName()
                            + "#" + method.getName()
                            + " parameter must extend ProxyPacketEvent, found "
                            + params[0].getName());
        }
    }

    // -------------------------------------------------------------------------
    // Internal record
    // -------------------------------------------------------------------------

    private record RegisteredHandler(
            Object listenerInstance,
            Method method,
            Class<? extends ProxyPacketEvent> eventType,
            EventPriority priority,
            boolean ignoreCancelled) {
    }
}
