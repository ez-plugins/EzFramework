package com.skyblockexp.ezframework.proxy.event;

/**
 * Determines the order in which {@link PacketListener}-annotated methods are
 * invoked during a {@link ProxyEventManager#fireEvent(ProxyPacketEvent) fireEvent} call.
 *
 * <p>Handlers are called in ascending ordinal order: {@link #LOWEST} fires
 * first, {@link #MONITOR} fires last. This mirrors the Bukkit event priority
 * model so that developers familiar with that platform can apply the same
 * mental model.
 *
 * <p>Recommended usage:
 * <ul>
 *   <li>{@link #LOWEST} / {@link #LOW} — first opportunity to read or modify an event</li>
 *   <li>{@link #NORMAL} — default; most business logic lives here</li>
 *   <li>{@link #HIGH} / {@link #HIGHEST} — final say on mutations and cancellations</li>
 *   <li>{@link #MONITOR} — read-only observation <em>after</em> all decisions have been
 *       made; always fires even when the event is cancelled</li>
 * </ul>
 */
public enum EventPriority {

    /** Lowest priority — fires first. */
    LOWEST,

    /** Low priority. */
    LOW,

    /** Normal priority (default). */
    NORMAL,

    /** High priority. */
    HIGH,

    /** Highest priority — fires just before {@link #MONITOR}. */
    HIGHEST,

    /**
     * Monitor priority — fires last; always invoked even when the event has been
     * cancelled. Use this tier for logging or metrics, never to mutate event state.
     */
    MONITOR
}
