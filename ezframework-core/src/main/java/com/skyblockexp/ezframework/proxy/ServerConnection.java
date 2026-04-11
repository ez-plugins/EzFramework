package com.skyblockexp.ezframework.proxy;

/**
 * Platform-agnostic transport contract for sending {@link ServerMessage}s
 * between proxy and backend servers.
 *
 * <p>This interface defines only the raw sending primitives. Higher-level
 * concerns such as handler registration live in {@link EzMessenger}, which
 * extends this interface.
 *
 * <p>Platform modules (Velocity, BungeeCord, …) provide concrete
 * implementations of {@code ServerConnection} that translate
 * {@link ServerMessage}s into their respective plugin-messaging APIs.
 *
 * <p>Example (Velocity implementation):
 * <pre>{@code
 * public class VelocityEzMessenger implements EzMessenger {   // EzMessenger extends ServerConnection
 *     @Override
 *     public void send(String server, ServerMessage message) {
 *         byte[] data = serializer.serialize(message.getPacket());
 *         proxyServer.getServer(server)
 *                    .ifPresent(s -> s.sendPluginMessage(channelId, data));
 *     }
 * }
 * }</pre>
 */
public interface ServerConnection {

    /**
     * Send a {@link ServerMessage} to a single named backend server.
     *
     * @param server  the server name as registered with the proxy
     * @param message the message to send; must not be null
     */
    void send(String server, ServerMessage message);

    /**
     * Broadcast a {@link ServerMessage} to all known backend servers.
     *
     * @param message the message to broadcast; must not be null
     */
    void broadcast(ServerMessage message);
}
