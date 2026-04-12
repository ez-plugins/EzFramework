# Error handling

Design your handlers and senders to fail fast and report errors clearly.

Handler-side best practices:

- Catch exceptions inside handlers to avoid propagating to the event thread.
- Log context (packet type, request id, source server) when reporting errors.
- If the protocol supports it, send an error response packet with details.

Example handler with safe error handling:

```java
messenger.registerHandler(ExamplePackets.DoThingRequest.class, (pkt, ctx) -> {
  try {
    doWork(pkt);
  } catch (Exception e) {
    logger.error("failed processing {} from {}", pkt.requestId, ctx.getSource(), e);
    // optional: reply with an error packet
    messenger.send(ctx.getSource(), new ExamplePackets.ErrorResponse(pkt.requestId, e.getMessage()));
  }
});
```

Sender-side tips:

- Validate inputs before sending to avoid wasting bandwidth.
- On failure, decide whether to retry, escalate, or surface a user error.

See also:

- [handler-registration.md](handler-registration.md)
- [retries-and-timeouts.md](retries-and-timeouts.md)
- [testing-and-debugging.md](testing-and-debugging.md)
