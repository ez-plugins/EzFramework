package com.skyblockexp.ezframework.proxy.event;

import com.skyblockexp.ezframework.proxy.EzContext;

/**
 * Base type for all proxy packet events fired through a {@link ProxyEventManager}.
 *
 * <p>Each event carries the {@link EzContext} that was attached to the original
 * inbound packet (containing the player name and source server), as well as a
 * cancellation flag that listeners can set to signal that subsequent processing
 * should be suppressed.
 *
 * <p>Implement this interface to define a custom proxy packet event:
 * <pre>{@code
 * public class PlayerLoginPacketEvent implements ProxyPacketEvent {
 *
 *     private final EzContext context;
 *     private final String playerName;
 *     private boolean cancelled;
 *
 *     public PlayerLoginPacketEvent(EzContext context, String playerName) {
 *         this.context = context;
 *         this.playerName = playerName;
 *     }
 *
 *     public String getPlayerName() { return playerName; }
 *
 *     @Override public EzContext getContext()          { return context; }
 *     @Override public boolean   isCancelled()         { return cancelled; }
 *     @Override public void      setCancelled(boolean c) { this.cancelled = c; }
 * }
 * }</pre>
 *
 * @see PacketListener
 * @see ProxyEventManager
 */
public interface ProxyPacketEvent {

    /**
     * The context that accompanied the original inbound packet.
     *
     * <p>The context contains the triggering player name and the source server
     * name as known by the proxy transport.
     *
     * @return the {@link EzContext}; never {@code null}
     */
    EzContext getContext();

    /**
     * Returns whether this event has been cancelled by a prior listener.
     *
     * <p>Handlers registered with {@link PacketListener#ignoreCancelled()
     * ignoreCancelled = true} are skipped when this returns {@code true}.
     * Handlers at priority {@link EventPriority#MONITOR} always execute
     * regardless of cancellation state.
     *
     * @return {@code true} if the event is cancelled
     */
    boolean isCancelled();

    /**
     * Sets the cancellation state of this event.
     *
     * @param cancel {@code true} to cancel the event
     */
    void setCancelled(boolean cancel);
}
