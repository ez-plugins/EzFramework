# Metrics

The messenger exposes lightweight metrics to help monitor traffic and handler
performance. Use `snapshotMetrics()` to capture current counters.

Example:

```java
VelocityMessengerMetrics snapshot = messenger.snapshotMetrics();
logger.info("received packets: {}", snapshot.getReceiveCount());
logger.info("sent packets: {}", snapshot.getSendCount());
```

What to monitor:

- receive/send counts — quick health check for traffic
- handler errors — track and alert on exceptions seen in handlers

Tip:

- Snapshot metrics periodically (e.g., every 30s) and export to your
  monitoring system for alerting and trend analysis.

See also:

- [messenger.md](messenger.md)
- [testing-and-debugging.md](testing-and-debugging.md)
