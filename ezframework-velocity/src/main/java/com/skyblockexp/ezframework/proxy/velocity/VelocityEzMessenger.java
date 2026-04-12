package com.skyblockexp.ezframework.proxy.velocity;

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
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Velocity-backed implementation of {@link EzMessenger} (which extends
 * {@link com.skyblockexp.ezframework.proxy.ServerConnection}).
 *
 * <p>Implements the two {@code ServerConnection} transport primitives
 * ({@link #send(String, ServerMessage)} and {@link #broadcast(ServerMessage)}):
 * each {@link ServerMessage} is serialized by {@link EzSerializer} into a UTF-8
 * JSON envelope and delivered to backend servers via Velocity plugin messaging.
 *
 * <p>Incoming messages from backend servers are received by
 * {@link VelocityPluginMessageListener} and forwarded to
 * {@link #dispatch(byte[], String, String)} for deserialization and handler dispatch.
 *
 * <p>Instances are created and owned by {@link VelocityBootstrap}.
 */
public final class VelocityEzMessenger implements EzMessenger, ListenerMessenger {

    private final ProxyServer proxy;
    private final Logger logger;
    private final EzPacketRegistry registry;
    private final EzSerializer serializer;
    private final MinecraftChannelIdentifier defaultChannelId;

    /** Handlers keyed by the packet ID string returned by {@link EzPacket#packetId()}. */
    private final Map<String, EzPacketHandler<?>> handlers = new ConcurrentHashMap<>();

    private final ListenerDispatcher listenerDispatcher = new ListenerDispatcher();

    /**
     * Construct a messenger. Called by {@link VelocityBootstrap}.
     *
     * @param proxy      the Velocity proxy server
     * @param logger     plugin logger for warnings and errors
     * @param registry   packet registry for deserialization
     * @param serializer serializer instance
     */
    VelocityEzMessenger(
            ProxyServer proxy,
            Logger logger,
            EzPacketRegistry registry,
            EzSerializer serializer) {
        this.proxy = Objects.requireNonNull(proxy, "proxy");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
        String[] parts = com.skyblockexp.ezframework.proxy.EzChannel.DEFAULT.parts();
        this.defaultChannelId = parts.length == 2
            ? MinecraftChannelIdentifier.create(parts[0], parts[1])
            : MinecraftChannelIdentifier.from(com.skyblockexp.ezframework.proxy.EzChannel.DEFAULT.getName());
    }

    /**
     * Return the Velocity channel identifier derived from {@link com.skyblockexp.ezframework.proxy.EzChannel#DEFAULT}.
     *
     * @return the default channel identifier
     */
    public MinecraftChannelIdentifier getDefaultChannelId() {
        return defaultChannelId;
    }

    /**
     * Resolve a Velocity channel identifier from an {@link com.skyblockexp.ezframework.proxy.EzChannel}.
     *
     * @param ezChannel the EzFramework channel descriptor
    * @return a Velocity {@link ChannelIdentifier}
     */
    private MinecraftChannelIdentifier toChannelId(com.skyblockexp.ezframework.proxy.EzChannel ezChannel) {
        String[] parts = ezChannel.parts();
        return parts.length == 2
                ? MinecraftChannelIdentifier.create(parts[0], parts[1])
                : MinecraftChannelIdentifier.from(ezChannel.getName());
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
     * <p>Serializes the packet from the {@link ServerMessage} and sends it to
     * the named backend server over the message's channel.
     */
    @Override
    public void send(String server, ServerMessage message) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(message, "message");
        MinecraftChannelIdentifier channelId = toChannelId(message.getChannel());
        proxy.getServer(server).ifPresentOrElse(
                registered -> sendToServer(registered, channelId, message.getPacket()),
                () -> logger.warn("[EzMessenger] Cannot send packet '{}' — server '{}' not found",
                        message.getPacket().packetId(), server));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Serializes the packet and delivers it to every backend server registered
     * with the proxy over the message's channel.
     */
    @Override
    public void broadcast(ServerMessage message) {
        Objects.requireNonNull(message, "message");
        MinecraftChannelIdentifier channelId = toChannelId(message.getChannel());
        byte[] data = serializer.serialize(message.getPacket());
        for (RegisteredServer server : proxy.getAllServers()) {
            boolean sent = server.sendPluginMessage(channelId, data);
            if (!sent) {
                logger.debug("[EzMessenger] No players on '{}', plugin message queued/dropped",
                        server.getServerInfo().getName());
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
    // Internal — inbound dispatch (called by VelocityPluginMessageListener)
    // -------------------------------------------------------------------------

    /**
     * Deserialize and dispatch a raw inbound plugin message to the appropriate
     * {@link EzPacketHandler}. Called by {@link VelocityPluginMessageListener}.
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
            logger.warn("[EzMessenger] Failed to deserialize incoming packet from '{}': {}",
                    sourceServer, e.getMessage());
            return;
        }
        EzPacketHandler handler = handlers.get(packet.packetId());
        if (handler != null) {
            EzContext context = new EzContext(playerName, sourceServer, null);
            try {
                handler.handle(packet, context);
            } catch (Exception e) {
                logger.error("[EzMessenger] Handler for '{}' threw an exception", packet.packetId(), e);
            }
        } else {
            logger.debug("[EzMessenger] No handler for packet '{}' from '{}'",
                    packet.packetId(), sourceServer);
        }
        listenerDispatcher.fire(packet);
    }

    private void sendToServer(RegisteredServer server, MinecraftChannelIdentifier channelId, EzPacket packet) {
        byte[] data = serializer.serialize(packet);
        boolean sent = server.sendPluginMessage(channelId, data);
        if (!sent) {
            logger.debug("[EzMessenger] Plugin message for '{}' on '{}' may be queued — no connected players",
                    packet.packetId(), server.getServerInfo().getName());
        }
    }
}
