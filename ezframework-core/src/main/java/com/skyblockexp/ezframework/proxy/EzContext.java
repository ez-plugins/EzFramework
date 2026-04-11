package com.skyblockexp.ezframework.proxy;

/**
 * Immutable metadata accompanying an inbound {@link EzPacket}.
 *
 * <p>All fields are nullable; their presence depends on the transport
 * implementation and the nature of the message (e.g. server-to-server
 * messages may not have an associated player).
 */
public final class EzContext {

    private final String playerName;
    private final String sourceServer;
    private final String targetServer;

    /**
     * Construct a context with all known fields.
     *
     * @param playerName   the name of the player associated with this message, or {@code null}
     * @param sourceServer the name of the server the message originated from, or {@code null}
     * @param targetServer the name of the server the message was sent to, or {@code null}
     */
    public EzContext(String playerName, String sourceServer, String targetServer) {
        this.playerName = playerName;
        this.sourceServer = sourceServer;
        this.targetServer = targetServer;
    }

    /**
     * Convenience factory for a server-only context (no player).
     *
     * @param sourceServer originating server name
     * @param targetServer destination server name
     * @return a new context
     */
    public static EzContext of(String sourceServer, String targetServer) {
        return new EzContext(null, sourceServer, targetServer);
    }

    /**
     * The player whose connection carried this message, if any.
     *
     * @return player name or {@code null}
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * The server the message was sent from.
     *
     * @return source server name or {@code null}
     */
    public String getSourceServer() {
        return sourceServer;
    }

    /**
     * The server the message was addressed to.
     *
     * @return target server name or {@code null}
     */
    public String getTargetServer() {
        return targetServer;
    }

    @Override
    public String toString() {
        return "EzContext{player=" + playerName
                + ", source=" + sourceServer
                + ", target=" + targetServer + "}";
    }
}
