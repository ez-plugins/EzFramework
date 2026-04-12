# Handler registration

Handlers are callbacks invoked when a packet of a registered type is received.
Register handlers on the messenger and optionally offload execution to a
task executor to avoid blocking the event thread.

Example:

```java
// register a handler for responses
messenger.registerHandler(ExamplePackets.BalanceResponse.class, (packet, ctx) -> {
    logger.info("Balance {} for {}", packet.balance, packet.playerId);
});

// dispatch handlers off the event thread (Velocity example)
messenger.setDispatchExecutor(task -> proxy.getScheduler().buildTask(this, task).schedule());
```

Handler tips:

- Keep handlers short; offload heavy work (I/O, DB) to async tasks.
- `ctx` (handler context) may contain metadata such as source server or sender
  connection useful for replies.
- Register handlers before the messenger starts receiving messages.

See also:

- [messenger.md](messenger.md)
- [request-response.md](request-response.md)
- [error-handling.md](error-handling.md)
