package com.skyblockexp.ezframework.proxy.bungee;

import com.skyblockexp.ezframework.listener.ListenerDispatcher;
import com.skyblockexp.ezframework.listener.ListenerMessenger;
import com.skyblockexp.ezframework.listener.PacketListenerInterface;
import com.skyblockexp.ezframework.proxy.EzContext;
import com.skyblockexp.ezframework.proxy.EzMessenger;
import com.skyblockexp.ezframework.proxy.EzPacket;
import com.skyblockexp.ezframework.proxy.EzPacketHandler;
import com.skyblockexp.ezframework.proxy.EzPacketRegistry;
import com.skyblockexp.ezframework.proxy.EzSerializer;
import com.skyblockexp.ezframework.proxy.ServerMessage;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * BungeeCord-backed implementation of {@link EzMessenger} (which extends
 * {@link com.skyblockexp.ezframework.proxy.ServerConnection}).
 *
 * <p>Implements the two {@code ServerConnection} transport primitives
 * ({@link #send(String, ServerMessage)} and {@link #broadcast(ServerMessage)}):
 * each {@link ServerMessage} is serialized by {@link EzSerializer} into a UTF-8
 * JSON envelope and delivered to backend servers via BungeeCord plugin messaging.
 *
 * <p>Incoming messages from backend servers are received by
 * {@link BungeePluginMessageListener} and forwarded to
 * {@link #dispatch(byte[], String, String)} for deserialization and handler dispatch.
 *
 * <p>Instances are created and owned by {@link BungeeBootstrap}.
 *
 * <p><b>BungeeCord limitation:</b> plugin messages can only be sent to a backend
 * server while at least one player is connected to it. If no player is connected,
 * {@link #send} and {@link #broadcast} log a warning and skip that server.
 */
public final class BungeeEzMessenger implements EzMessenger, ListenerMessenger {

    private final ProxyServer proxy;
    private final Logger logger;
    private final EzPacketRegistry registry;
    private final EzSerializer serializer;

    /** Handlers keyed by the packet ID string returned by {@link EzPacket#packetId()}. */
    private final Map<String, EzPacketHandler<?>> handlers = new ConcurrentHashMap<>();

    private final ListenerDispatcher listenerDispatcher = new ListenerDispatcher();

    /**
     * Construct a messenger. Called by {@link BungeeBootstrap}.
     *
     * @param proxy      the BungeeCord proxy server
     * @param logger     plugin logger for warnings and errors
     * @param registry   packet registry for deserialization
     * @param serializer serializer instance
     */
    BungeeEzMessenger(
            ProxyServer proxy,
            Logger logger,
            EzPacketRegistry registry,
            EzSerializer serializer) {
        this.proxy = Objects.requireNonNull(proxy, "proxy");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
    }

    /**
     * Return the packet registry used by this messenger.
     *
     * @return packet registry
     */
    public EzPacketRegistry getRegistry() {
        return registry;
    }

    /**
     * Return the serializer used by this messenger.
     *
     * @return serializer
     */
    public EzSerializer getSerializer() {
        return serializer;
    }

    // -------------------------------------------------------------------------
    // ServerConnection — transport primitives
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Serializes the packet from the {@link ServerMessage} and sends it to the
     * named backend server over the message's channel. Logs a warning if the server
     * is unknown or has no connected players.
     */
    @Override
    public void send(String server, ServerMessage message) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(message, "message");
        ServerInfo serverInfo = proxy.getServerInfo(server);
        if (serverInfo == null) {
            logger.warning("[EzMessenger] Cannot send packet '" + message.getPacket().packetId()
                    + "' — server '" + server + "' not found");
            return;
        }
        byte[] data = serializer.serialize(message.getPacket());
        try {
            serverInfo.sendData(message.getChannel().getName(), data);
        } catch (Exception e) {
            logger.warning("[EzMessenger] Plugin message for '" + message.getPacket().packetId()
                    + "' on '" + server + "' failed — " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Serializes the packet and delivers it to every backend server registered
     * with the proxy that has at least one connected player.
     */
    @Override
    public void broadcast(ServerMessage message) {
        Objects.requireNonNull(message, "message");
        byte[] data = serializer.serialize(message.getPacket());
        for (ServerInfo serverInfo : proxy.getServers().values()) {
            try {
                serverInfo.sendData(message.getChannel().getName(), data);
            } catch (Exception e) {
                logger.fine("[EzMessenger] Plugin message to '" + serverInfo.getName()
                        + "' failed — " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // EzMessenger — handler registration
    // -------------------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public <T extends EzPacket> void registerHandler(Class<T> packetType, EzPacketHandler<T> handler) {
        Objects.requireNonNull(packetType, "packetType");
        Objects.requireNonNull(handler, "handler");
        try {
            T instance = packetType.getDeclaredConstructor().newInstance();
            handlers.put(instance.packetId(), handler);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Cannot register handler for " + packetType.getName()
                            + ": requires a public no-arg constructor", e);
        }
    }

    /**
     * Register a Bukkit-style annotation-based packet listener.
     *
     * <p>Methods annotated with {@link com.skyblockexp.ezframework.listener.PacketListener}
     * will be invoked when a matching packet is dispatched, in
     * {@link com.skyblockexp.ezframework.listener.EventPriority} order.
     * This is independent of {@link #registerHandler} — both may be used simultaneously.
     *
     * @param listener the listener to register; must not be {@code null}
     */
    @Override
    public void registerListener(PacketListenerInterface listener) {
        Objects.requireNonNull(listener, "listener");
        listenerDispatcher.registerListener(listener);
    }

    // -------------------------------------------------------------------------
    // Internal — inbound dispatch (called by BungeePluginMessageListener)
    // -------------------------------------------------------------------------

    /**
     * Deserialize and dispatch a raw inbound plugin message to the appropriate
     * {@link EzPacketHandler}. Called by {@link BungeePluginMessageListener}.
     *
     * @param data         raw bytes from the plugin channel
     * @param sourceServer name of the backend server that sent the message
     * @param playerName   player whose connection carried the message, or {@code null}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    void dispatch(byte[] data, String sourceServer, String playerName) {
        EzPacket packet;
        try {
            packet = serializer.deserialize(data, registry);
        } catch (EzSerializer.EzSerializerException e) {
            logger.warning("[EzMessenger] Failed to deserialize incoming packet from '"
                    + sourceServer + "': " + e.getMessage());
            return;
        }
        EzPacketHandler handler = handlers.get(packet.packetId());
        if (handler != null) {
            EzContext context = new EzContext(playerName, sourceServer, null);
            try {
                handler.handle(packet, context);
            } catch (Exception e) {
                logger.severe("[EzMessenger] Handler for '" + packet.packetId()
                        + "' threw an exception: " + e.getMessage());
            }
        } else {
            logger.fine("[EzMessenger] No handler for packet '" + packet.packetId()
                    + "' from '" + sourceServer + "'");
        }
        listenerDispatcher.fire(packet);
    }
}
