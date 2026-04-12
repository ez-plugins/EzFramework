# BungeeCord integration

Short guide for using the Bungee transport implementation.

Setup

1. Add a dependency on `ezframework-bungee` and ensure the `ezframework` plugin
   is enabled on the proxy.
2. Obtain the `BungeeBootstrap` instance from the proxy's plugin manager and
   call `getMessenger()` to access `BungeeEzMessenger`.

Example

```java
BungeeEzMessenger messenger = ((BungeeBootstrap) getProxy().getPluginManager()
        .getPlugin("ezframework")).getMessenger();

messenger.getRegistry().register(ExamplePackets.BalanceRequest.class);
messenger.registerHandler(ExamplePackets.BalanceResponse.class, (p, ctx) -> { /* ... */ });

messenger.send("survival", new ExamplePackets.BalanceRequest("Notch", id));
```

Notes

- Bungee plugin messaging requires players connected to the target server to
  deliver messages; empty servers may not receive plugin messages.

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
