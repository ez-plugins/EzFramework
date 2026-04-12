package com.skyblockexp.ezframework.proxy.velocity;

/**
 * Snapshot of metrics captured by {@link VelocityEzMessenger}.
 */
public final class VelocityMessengerMetrics {

    private final long sendCount;
    private final long sendServerMissingCount;
    private final long sendQueuedCount;
    private final long broadcastCount;
    private final long broadcastTargetCount;
    private final long broadcastQueuedCount;
    private final long receiveCount;
    private final long deserializeFailureCount;
    private final long handlerMissingCount;
    private final long handlerErrorCount;

    VelocityMessengerMetrics(
            long sendCount,
            long sendServerMissingCount,
            long sendQueuedCount,
            long broadcastCount,
            long broadcastTargetCount,
            long broadcastQueuedCount,
            long receiveCount,
            long deserializeFailureCount,
            long handlerMissingCount,
            long handlerErrorCount) {
        this.sendCount = sendCount;
        this.sendServerMissingCount = sendServerMissingCount;
        this.sendQueuedCount = sendQueuedCount;
        this.broadcastCount = broadcastCount;
        this.broadcastTargetCount = broadcastTargetCount;
        this.broadcastQueuedCount = broadcastQueuedCount;
        this.receiveCount = receiveCount;
        this.deserializeFailureCount = deserializeFailureCount;
        this.handlerMissingCount = handlerMissingCount;
        this.handlerErrorCount = handlerErrorCount;
    }

    public long getSendCount() {
        return sendCount;
    }

    public long getSendServerMissingCount() {
        return sendServerMissingCount;
    }

    public long getSendQueuedCount() {
        return sendQueuedCount;
    }

    public long getBroadcastCount() {
        return broadcastCount;
    }

    public long getBroadcastTargetCount() {
        return broadcastTargetCount;
    }

    public long getBroadcastQueuedCount() {
        return broadcastQueuedCount;
    }

    public long getReceiveCount() {
        return receiveCount;
    }

    public long getDeserializeFailureCount() {
        return deserializeFailureCount;
    }

    public long getHandlerMissingCount() {
        return handlerMissingCount;
    }

    public long getHandlerErrorCount() {
        return handlerErrorCount;
    }
}
