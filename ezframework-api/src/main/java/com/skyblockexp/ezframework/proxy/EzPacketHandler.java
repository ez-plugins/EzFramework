package com.skyblockexp.ezframework.proxy;

/**
 * Handles an incoming {@link EzPacket} of a specific type.
 *
 * <p>Register handlers via {@link EzMessenger#registerHandler(Class, EzPacketHandler)}.
 * The transport layer will call {@link #handle(EzPacket, EzContext)} when a packet
 * of the matching type arrives.
 *
 * @param <T> the concrete packet type this handler processes
 */
@FunctionalInterface
public interface EzPacketHandler<T extends EzPacket> {

    /**
     * Called when a packet of type {@code T} is received.
     *
     * @param packet  the deserialized packet
     * @param context metadata about the message source (player, server, etc.)
     */
    void handle(T packet, EzContext context);
}
