package com.skyblockexp.ezframework.proxy.velocity;

import com.skyblockexp.ezframework.proxy.EzPacket;
import com.skyblockexp.ezframework.proxy.EzPacketHandler;

import java.util.Objects;

/**
 * Convenience helper for registering packets and handlers on Velocity.
 */
public final class VelocityMessengerSetup {

    private final VelocityEzMessenger messenger;

    private VelocityMessengerSetup(VelocityEzMessenger messenger) {
        this.messenger = Objects.requireNonNull(messenger, "messenger");
    }

    /**
     * Create a setup helper for a known messenger.
     *
     * @param messenger Velocity messenger instance
     * @return setup helper
     */
    public static VelocityMessengerSetup using(VelocityEzMessenger messenger) {
        return new VelocityMessengerSetup(messenger);
    }

    /**
     * Create a setup helper using the service-registered messenger.
     *
     * NOTE: `forProxy` helper was removed due to API differences across
     * Velocity releases. Use `using(messenger)` with an explicit
     * `VelocityEzMessenger` instance instead.
     */

    /**
     * Register packet classes with the messenger registry.
     *
     * @param packetTypes packet classes to register
     * @return this helper for chaining
     */
    @SafeVarargs
    public final VelocityMessengerSetup registerPackets(Class<? extends EzPacket>... packetTypes) {
        Objects.requireNonNull(packetTypes, "packetTypes");
        for (Class<? extends EzPacket> packetType : packetTypes) {
            messenger.getRegistry().register(packetType);
        }
        return this;
    }

    /**
     * Register a handler for a packet type.
     *
     * @param packetType packet class
     * @param handler    packet handler
     * @param <T>        packet type
     * @return this helper for chaining
     */
    public <T extends EzPacket> VelocityMessengerSetup registerHandler(
            Class<T> packetType,
            EzPacketHandler<T> handler) {
        messenger.registerHandler(packetType, handler);
        return this;
    }

    /**
     * Return the underlying messenger.
     *
     * @return messenger
     */
    public VelocityEzMessenger messenger() {
        return messenger;
    }
}
