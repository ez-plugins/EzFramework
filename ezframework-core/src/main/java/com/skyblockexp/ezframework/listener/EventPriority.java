package com.skyblockexp.ezframework.listener;

/**
 * Determines the order in which {@link PacketListener}-annotated methods are
 * invoked when a packet is dispatched.
 *
 * <p>Methods with a lower priority ordinal execute <em>first</em>. When multiple
 * methods share the same priority they execute in registration order.
 *
 * <h2>Recommended usage</h2>
 * <ul>
 *   <li><b>{@link #LOWEST} / {@link #LOW}</b> — early guards, e.g. permission
 *       or rate-limit checks that may cancel the packet before other handlers run.</li>
 *   <li><b>{@link #NORMAL}</b> — default for most business logic.</li>
 *   <li><b>{@link #HIGH} / {@link #HIGHEST}</b> — logic that must see modifications
 *       made by lower-priority handlers before acting.</li>
 *   <li><b>{@link #MONITOR}</b> — read-only observation and logging <em>only</em>.
 *       Handlers at this level should never modify or cancel the packet because
 *       all processing decisions have already been made.</li>
 * </ul>
 *
 * @see PacketListener#priority()
 */
public enum EventPriority {

    /**
     * Executed first. Intended for early-exit guards (e.g. ban or rate-limit checks)
     * that cancel the packet before any other handler sees it.
     */
    LOWEST,

    /**
     * Executed before {@link #NORMAL}. Suitable for pre-processing or soft
     * validation that other handlers may depend on.
     */
    LOW,

    /**
     * Default priority. Used for the majority of packet-handling logic.
     */
    NORMAL,

    /**
     * Executed after {@link #NORMAL}. Suitable for handlers that react to
     * changes made by normal-priority handlers.
     */
    HIGH,

    /**
     * Executed after {@link #HIGH}. Reserved for handlers that must have the
     * final say on packet mutations before monitoring begins.
     */
    HIGHEST,

    /**
     * Executed last. Intended for read-only observation such as logging or
     * metrics. Handlers at this level <strong>must not</strong> modify or
     * cancel the packet.
     */
    MONITOR
}
