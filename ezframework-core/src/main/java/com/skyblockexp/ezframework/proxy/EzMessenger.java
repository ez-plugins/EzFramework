package com.skyblockexp.ezframework.proxy;

/**
 * High-level, platform-agnostic messaging API for cross-server packet exchange.
 *
 * <p>{@code EzMessenger} extends {@link ServerConnection} with handler
 * registration and convenience overloads that accept raw {@link EzPacket}s
 * (automatically wrapped in a {@link ServerMessage} on the default channel).
 *
 * <p>Platform modules (Velocity, BungeeCord, …) provide implementations by
 * implementing the two {@link ServerConnection} primitives:
 * {@link #send(String, ServerMessage)} and {@link #broadcast(ServerMessage)}.
 *
 * <p>Typical usage:
 * <pre>{@code
 * // Register a handler
 * messenger.registerHandler(BalanceResponsePacket.class, (packet, ctx) ->
 *     logger.info("Balance for " + packet.getPlayerId() + ": " + packet.getBalance()));
 *
 * // Send using the convenience overload (uses default channel)
 * messenger.send("survival", new BalanceRequestPacket(playerName, UUID.randomUUID().toString()));
 *
 * // Or send with an explicit channel
 * messenger.send("survival", ServerMessage.of(packet, new EzChannel("myplugin:data")));
 * }</pre>
 */
public interface EzMessenger extends ServerConnection {

    /**
     * Register a handler for packets of the given type. Only one handler per
     * packet type is supported; registering again replaces the previous handler.
     *
     * @param packetType the class of the packet to handle
     * @param handler    the handler to invoke on receipt
     * @param <T>        the packet type
     */
    <T extends EzPacket> void registerHandler(Class<T> packetType, EzPacketHandler<T> handler);

    // -------------------------------------------------------------------------
    // Convenience overloads — wrap a raw EzPacket in a ServerMessage using the
    // default channel, then delegate to the ServerConnection primitives.
    // -------------------------------------------------------------------------

    /**
     * Send a packet to a named backend server on {@link EzChannel#DEFAULT}.
     *
     * @param server the server name as registered with the proxy
     * @param packet the packet to send
     */
    default void send(String server, EzPacket packet) {
        send(server, ServerMessage.of(packet));
    }

    /**
     * Broadcast a packet to all backend servers on {@link EzChannel#DEFAULT}.
     *
     * @param packet the packet to broadcast
     */
    default void broadcast(EzPacket packet) {
        broadcast(ServerMessage.of(packet));
    }
}
