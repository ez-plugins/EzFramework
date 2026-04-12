package com.skyblockexp.ezframework.proxy.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a proxy packet event handler to be discovered and
 * registered by {@link ProxyEventManager#registerListener(Object)}.
 *
 * <p>Annotated methods must be {@code public}, non-static, and accept exactly
 * one parameter whose type extends {@link ProxyPacketEvent}. Any other
 * signature causes {@link ProxyEventManager#registerListener(Object)} to throw
 * an {@link IllegalArgumentException}.
 *
 * <p>Example:
 * <pre>{@code
 * public class MyProxyListener {
 *
 *     @PacketListener
 *     public void onLogin(PlayerLoginPacketEvent event) {
 *         System.out.println("Login: " + event.getContext().getPlayerName());
 *     }
 *
 *     @PacketListener(priority = EventPriority.MONITOR, ignoreCancelled = false)
 *     public void onLoginMonitor(PlayerLoginPacketEvent event) {
 *         // always runs — even if cancelled
 *     }
 * }
 * }</pre>
 *
 * @see ProxyEventManager
 * @see EventPriority
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PacketListener {

    /**
     * The priority tier at which this handler is invoked.
     *
     * <p>Handlers are called in ascending {@link EventPriority} ordinal order
     * ({@link EventPriority#LOWEST} first, {@link EventPriority#MONITOR} last).
     *
     * @return the priority; defaults to {@link EventPriority#NORMAL}
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * When {@code true}, this handler is skipped if the event has already been
     * cancelled by a prior listener.
     *
     * <p>Handlers at {@link EventPriority#MONITOR} always execute regardless of
     * this flag.
     *
     * @return {@code true} to skip cancelled events; defaults to {@code false}
     */
    boolean ignoreCancelled() default false;
}
