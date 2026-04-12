package com.skyblockexp.ezframework.listener;

/**
 * Extension interface for proxy messenger implementations that support
 * annotation-based packet listeners.
 *
 * <p>Implemented by {@code VelocityEzMessenger} and {@code BungeeEzMessenger}.
 * Consumers that want to remain platform-agnostic can depend on this interface
 * rather than a concrete messenger class.
 *
 * <h2>Usage</h2>
 * <p>Register a listener class whose methods are annotated with
 * {@link PacketListener}:
 *
 * <blockquote><pre>
 * messenger.registerListener(new MyPacketListener());
 * </pre></blockquote>
 *
 * <p>Any method in the listener class that:
 * <ul>
 *   <li>is annotated with {@code @PacketListener}</li>
 *   <li>accepts exactly one parameter whose type matches the incoming packet</li>
 * </ul>
 * will be invoked automatically when that packet is dispatched.
 *
 * <p>Multiple listeners — and the existing lambda-based
 * {@code registerHandler} API — may all be active on the same packet type
 * simultaneously.
 *
 * @see PacketListener
 * @see PacketListenerInterface
 * @see EventPriority
 */
public interface ListenerMessenger {

    /**
     * Register an annotation-based packet listener.
     *
     * <p>The dispatcher scans all methods of the provided listener for
     * {@link PacketListener} annotations and enqueues them, sorted by
     * {@link EventPriority}. Registration is additive — calling this method
     * multiple times with different listeners accumulates handlers.
     *
     * @param listener the listener instance whose annotated methods should
     *                 receive packet events; must not be {@code null}
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    void registerListener(PacketListenerInterface listener);
}
