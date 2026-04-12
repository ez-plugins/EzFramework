# Velocity integration

This page covers Velocity-specific integration details and examples.

Getting the messenger

- If you depend on the `ezframework` plugin directly in your Velocity plugin,
  you can obtain the `VelocityBootstrap` instance and call `getMessenger()`.
- Alternatively, `VelocityBootstrap.findMessenger(proxy)` and
  `VelocityMessengerLookup.find(proxy)` provide cross-version safe lookup.

Example setup

```java
VelocityEzMessenger messenger = VelocityBootstrap.findMessenger(proxy).orElseThrow();

VelocityMessengerSetup.using(messenger)
        .registerPackets(ExamplePackets.BalanceRequest.class, ExamplePackets.BalanceResponse.class)
        .registerHandler(ExamplePackets.BalanceResponse.class, (packet, ctx) -> {
            logger.info("Balance {} for {}", packet.balance, packet.playerId);
        });

// Optional: dispatch handlers off the event thread
messenger.setDispatchExecutor(task -> proxy.getScheduler().buildTask(this, task).schedule());

// Metrics
VelocityMessengerMetrics snapshot = messenger.snapshotMetrics();
logger.info("received packets: {}", snapshot.getReceiveCount());
```

Notes

- The `ezframework` Velocity plugin registers the plugin messaging channel on
  startup; ensure your packet classes are registered on both proxy and backends.
- Handler registration requires a public no-arg constructor on packet classes.

Related

- Quick start: [quick_start.md](quick_start.md)
- Wire format: [wire_format.md](wire_format.md)
- Overview: [overview.md](overview.md)

Simple send example

```java
// create a request packet (shared POJO)
String rid = UUID.randomUUID().toString();
ExamplePackets.BalanceRequest req = new ExamplePackets.BalanceRequest("Notch", rid);

// send on the default channel to a backend server named "survival"
messenger.send("survival", req);

// Optionally, send on a custom channel
messenger.send("survival", ServerMessage.of(req, new EzChannel("myplugin:data")));
```

On the backend, deserialize with `EzSerializer` and reply via the player connection:

```java
EzPacket incoming = new EzSerializer().deserialize(data, registry);
if (incoming instanceof ExamplePackets.BalanceRequest r) {
  double balance = lookupBalance(r.playerId);
  byte[] out = new EzSerializer().serialize(
    new ExamplePackets.BalanceResponse(r.playerId, r.requestId, balance));
  player.sendPluginMessage(plugin, EzChannel.DEFAULT.getName(), out);
}
```
