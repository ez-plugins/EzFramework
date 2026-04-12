package com.skyblockexp.ezframework.listener;

/**
 * Optional interface for packet events that can be cancelled by a listener.
 *
 * <p>When a packet event implements this interface, any {@link PacketListener}
 * handler may call {@link #setCancelled(boolean) setCancelled(true)} to signal
 * that subsequent processing should be suppressed.
 *
 * <p>Handlers annotated with
 * {@link PacketListener#ignoreCancelled() ignoreCancelled = true} will be
 * skipped automatically by the {@link ListenerDispatcher} once the event is
 * cancelled.
 *
 * <h2>Implementing a cancellable packet event</h2>
 * <blockquote><pre>
 * public class PlayerLoginPacketEvent implements Cancellable {
 *     private boolean cancelled;
 *
 *     {@literal @}Override
 *     public boolean isCancelled() { return cancelled; }
 *
 *     {@literal @}Override
 *     public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
 * }
 * </pre></blockquote>
 *
 * @see PacketListener#ignoreCancelled()
 * @see ListenerDispatcher
 */
public interface Cancellable {

    /**
     * Returns whether the packet event has been cancelled.
     *
     * @return {@code true} if the event is cancelled
     */
    boolean isCancelled();

    /**
     * Sets the cancellation state of the packet event.
     *
     * @param cancelled {@code true} to cancel the event, {@code false} to un-cancel it
     */
    void setCancelled(boolean cancelled);
}
