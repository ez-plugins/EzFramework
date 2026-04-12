# Retries & Timeouts

EzFramework's messenger focuses on reliable delivery primitives but does not
impose a specific retry policy. Implement retries and timeouts in your plugin
code to match your app's semantics.

Simple request with timeout + retry (example using Java CompletableFuture):

```java
String rid = UUID.randomUUID().toString();
ExamplePackets.BalanceRequest req = new ExamplePackets.BalanceRequest(playerId, rid);

CompletableFuture<BalanceResponse> future = new CompletableFuture<>();

// temporary handler to complete the future
messenger.registerHandler(ExamplePackets.BalanceResponse.class, (pkt, ctx) -> {
    if (rid.equals(pkt.requestId)) future.complete(pkt);
});

// send + retry helper
int attempts = 0;
ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
Runnable sendAttempt = new Runnable() {
  @Override public void run() {
    if (future.isDone() || attempts >= 3) return;
    attempts++;
    messenger.send("survival", req);
  }
};
ex.scheduleAtFixedRate(sendAttempt, 0, 2, TimeUnit.SECONDS);

// apply overall timeout
future.orTimeout(6, TimeUnit.SECONDS).whenComplete((r, t) -> {
  ex.shutdownNow();
  if (t != null) {
    // handle timeout / failure
  } else {
    // got response
  }
});
```

Notes
- Tune retry count and backoff to avoid amplifying load.
- Prefer idempotent request handlers or ensure server-side deduplication by
  request ID.
- Consider exponential backoff and jitter for production systems.

See also:

- [request-response.md](request-response.md)
- [handler-registration.md](handler-registration.md)
- [testing-and-debugging.md](testing-and-debugging.md)
