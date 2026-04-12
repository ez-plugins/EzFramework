# Packet registration

Packets are simple POJOs shared between proxy and backend. They must be
registered with the messenger's registry so the serializer can map IDs to types.

Example (registering classes):

```java
// on proxy startup
messenger.getRegistry().register(ExamplePackets.BalanceRequest.class);
messenger.getRegistry().register(ExamplePackets.BalanceResponse.class);

// on backend startup (same classes must be registered)
registry.register(ExamplePackets.BalanceRequest.class);
registry.register(ExamplePackets.BalanceResponse.class);
```

Requirements:

- Packet classes should be public and have a public no-arg constructor if
  the serializer requires it for deserialization.
- Register identical classes (same package/name) on both sides before use.

Best practice:

- Group packet registration in a single setup method so both proxy and backends
  can call the same registration sequence.

See also:

- [messenger.md](messenger.md)
- [serialization.md](serialization.md)
- [testing-and-debugging.md](testing-and-debugging.md)
