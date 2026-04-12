# Quick start — minimal setup

1. Build or install the proxy artifacts locally (from the monorepo):

```bash
mvn install -P proxy
```

2. On the proxy (Velocity example): install the `ezframework` plugin (the
   `ezframework-velocity` artifact) and obtain the messenger from your plugin
   startup code. See the Velocity guide for platform-specific examples.

```java
// Prefer explicit access via the bootstrap if available
VelocityEzMessenger messenger = bootstrap.getMessenger();

// Or use the cross-version lookup helper
Optional<VelocityEzMessenger> maybe = VelocityBootstrap.findMessenger(proxy);
if (maybe.isPresent()) {
    VelocityMessengerSetup.using(maybe.get())
            .registerPackets(ExamplePackets.BalanceRequest.class)
            .registerHandler(ExamplePackets.BalanceResponse.class, (p, ctx) -> { /* ... */ });
}
```

See also: [velocity.md](velocity.md)
See also
- Wire format: [wire_format.md](wire_format.md)
- Overview: [overview.md](overview.md)
3. On backend servers register the same packet classes and use `EzSerializer`
   to deserialize incoming plugin messages.

4. Use namespaced packet IDs via `EzPacketNamespace` to keep IDs consistent.
