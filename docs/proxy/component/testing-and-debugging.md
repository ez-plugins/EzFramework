# Testing & Debugging

Quick tips for testing messenger integrations.

Unit testing

- Mock the messenger or registry interfaces to assert that packets are sent and
  handlers are invoked.
- Test serialization round-trips for every packet type:

```java
EzSerializer s = new EzSerializer();
byte[] out = s.serialize(pkt);
EzPacket in = s.deserialize(out, registry);
assertEquals(pkt, in);
```

Integration testing

- Run the proxy in a test profile or use the project's existing test utilities
  to spin up a lightweight proxy + backend.
- Use `snapshotMetrics()` to assert send/receive counts during tests.

Debugging

- Add logging in handlers and serialize/deserialize boundaries to inspect
  problematic packets.
- Use unique request IDs in tests to correlate logs across processes.
