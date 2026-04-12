# Cross-server messaging — Overview

EzFramework provides a lightweight, platform-agnostic API for sending structured messages between
a proxy (Velocity or BungeeCord) and backend servers.

## Architecture

```
backend plugin           proxy plugin
  EzSerializer   ──────►  EzPacketRegistry / EzSerializer
  EzPacketRegistry         VelocityEzMessenger / BungeeEzMessenger
  ServerMessage  ◄──────   ServerMessage
```

- `ezframework-api` contains the protocol types (`EzPacket`, `EzSerializer`, `EzPacketRegistry`,
  `EzChannel`, `ServerMessage`) — no proxy APIs are referenced.
- `ezframework-velocity` and `ezframework-bungee` provide the transport implementations.
- Packets are identified by namespaced IDs (`pluginId:action`) to prevent collisions between
  different plugins sharing the same proxy.

## Key types

| Type | Location | Purpose |
|---|---|---|
| `EzPacket` | `ezframework-api` | Marker interface; implementations declare `packetId()` |
| `EzPacketNamespace` | `ezframework-api` | Validates and lowercases a namespace; generates IDs |
| `EzPacketRegistry` | `ezframework-api` | Maps packet IDs to classes |
| `EzSerializer` | `ezframework-api` | Serialises/deserialises packets to/from JSON envelopes |
| `EzChannel` | `ezframework-api` | Plugin channel name wrapper; `EzChannel.DEFAULT` = `"BungeeCord"` |
| `ServerMessage` | `ezframework-api` | Pairs a packet with a channel for dispatch |

## Packet IDs

All packet IDs must be namespaced: `<namespace>:<action>`. Use `EzPacketNamespace` to generate
consistent IDs:

```java
EzPacketNamespace ns = new EzPacketNamespace("myeconomy");
String id = ns.id("balance_request");  // → "myeconomy:balance_request"
```

Namespaces are lower-cased automatically. Colons in namespace or action strings are rejected.

## Sending a packet (backend → proxy)

```java
EzPacket packet = new BalanceRequest(player.getUniqueId().toString());
ServerMessage msg = ServerMessage.of(packet);  // uses EzChannel.DEFAULT
// Send msg via the platform plugin channel API
```

## Receiving and deserialising (proxy side)

```java
EzPacketRegistry registry = new EzPacketRegistry();
registry.register(BalanceRequest.class);   // registers with packetId() declared on the class

EzSerializer serializer = new EzSerializer(registry);
EzPacket packet = serializer.deserialize(bytes, registry);
```

## Related pages

- [Quick start](quick_start.md) — minimal setup example
- [Wire format](wire_format.md) — JSON envelope specification and registration rules
- [Velocity integration](velocity.md) — Velocity-specific transport
- [BungeeCord integration](bungee.md) — BungeeCord-specific transport
