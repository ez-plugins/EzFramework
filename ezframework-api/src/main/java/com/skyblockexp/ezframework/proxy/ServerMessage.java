package com.skyblockexp.ezframework.proxy;

import java.util.Objects;

/**
 * Immutable POJO representing a complete message ready to be sent over a
 * cross-server channel.
 *
 * <p>A {@code ServerMessage} binds an {@link EzPacket} (the payload) to the
 * {@link EzChannel} it should travel over. Use the static factory methods to
 * construct instances; the no-arg form uses {@link EzChannel#DEFAULT}.
 *
 * <pre>{@code
 * // Send on the default channel
 * ServerMessage msg = ServerMessage.of(new BalanceRequestPacket(player, requestId));
 *
 * // Send on a custom channel
 * ServerMessage msg = ServerMessage.of(new BalanceRequestPacket(player, requestId),
 *                                      new EzChannel("myplugin:data"));
 * }</pre>
 */
public final class ServerMessage {

    private final EzPacket packet;
    private final EzChannel channel;

    private ServerMessage(EzPacket packet, EzChannel channel) {
        this.packet = Objects.requireNonNull(packet, "packet");
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    /**
     * Create a {@code ServerMessage} using {@link EzChannel#DEFAULT}.
     *
     * @param packet the packet payload; must not be null
     * @return a new server message
     */
    public static ServerMessage of(EzPacket packet) {
        return new ServerMessage(packet, EzChannel.DEFAULT);
    }

    /**
     * Create a {@code ServerMessage} with an explicit channel.
     *
     * @param packet  the packet payload; must not be null
     * @param channel the channel to send on; must not be null
     * @return a new server message
     */
    public static ServerMessage of(EzPacket packet, EzChannel channel) {
        return new ServerMessage(packet, channel);
    }

    /**
     * Return the packet payload.
     *
     * @return the packet
     */
    public EzPacket getPacket() {
        return packet;
    }

    /**
     * Return the channel this message should be sent on.
     *
     * @return the channel
     */
    public EzChannel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "ServerMessage{packetId=" + packet.packetId()
                + ", channel=" + channel.getName() + "}";
    }
}
