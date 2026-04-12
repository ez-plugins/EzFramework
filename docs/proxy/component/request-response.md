# Request / Response Pattern

Many cross-server interactions follow a request/response pattern. Use request
IDs to correlate replies and register temporary handlers or use futures.

Example: send a request and wait for a matching response

```java
String rid = UUID.randomUUID().toString();
ExamplePackets.BalanceRequest req = new ExamplePackets.BalanceRequest(playerId, rid);
CompletableFuture<BalanceResponse> future = new CompletableFuture<>();

messenger.registerHandler(ExamplePackets.BalanceResponse.class, (pkt, ctx) -> {
  if (rid.equals(pkt.requestId)) future.complete(pkt);
});

messenger.send("survival", req);
BalanceResponse resp = future.get(5, TimeUnit.SECONDS); // handle timeout
```

Alternative: use a correlation map to register one-shot handlers that clean
themselves up after completion to avoid leaking handlers.

Best practices
- Use unique request IDs and expire correlation entries after a timeout.
- Keep responses small and predictable.
- Consider streaming responses or chunking for large payloads.

See also:

- [messenger.md](messenger.md)
- [retries-and-timeouts.md](retries-and-timeouts.md)
- [testing-and-debugging.md](testing-and-debugging.md)
