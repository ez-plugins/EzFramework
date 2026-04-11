# Cross-server messaging (proxy)

EzFramework defines a **platform-agnostic** protocol and API in `ezframework-core` for sending structured messages between a **proxy** (Velocity or BungeeCord) and **backend** servers. Transport implementations live in optional modules so Bukkit-only projects never pull proxy APIs onto the classpath.

## What you get

| Layer | Module / package | Role |
| --- | --- | --- |
| Protocol & types | `ezframework-core` → `com.skyblockexp.ezframework.proxy` | `EzPacket`, namespaced IDs, `EzSerializer`, `EzPacketRegistry`, `EzMessenger`, `ServerConnection`, `ServerMessage`, `EzContext` |
| Velocity transport | `ezframework-velocity` | `VelocityEzMessenger`, `VelocityBootstrap` plugin |
| BungeeCord transport | `ezframework-bungee` | `BungeeEzMessenger`, `BungeeBootstrap` plugin |

**Chat formatting** (`Messaging`, MiniMessage) is unrelated; this doc is about **plugin channels** and **JSON envelopes** over the shared channel `ezframework:channel` (`EzChannel.DEFAULT`).

## Building the proxy modules

The parent POM only builds Bukkit-oriented modules by default. To compile and install the proxy artifacts locally:

```bash
mvn install -P proxy
```

Proxy plugin projects then depend on `ezframework-velocity` or `ezframework-bungee` (and usually `ezframework-core` for shared packet classes).

## Packet IDs: mandatory namespacing

Every `EzPacket` must return an ID in **`pluginId:action`** form (exactly one colon, non-empty on both sides), for example:

- `ezeconomy:balance.request`
- `ezshops:purchase.confirm`

All plugins on a network typically share **one** proxy `EzMessenger` and **one** handler map keyed by packet ID. Without this rule, two plugins could both use `balance.request` and **silently steal each other’s traffic**.

Use **`EzPacketNamespace`** so IDs stay consistent:

```java
public static final EzPacketNamespace NS = new EzPacketNamespace("myplugin");

@Override
public String packetId() {
    return NS.id("balance.request");  // "myplugin:balance.request"
}
```

`EzPacketRegistry` rejects IDs that are not namespaced. `EzSerializer` also validates incoming wire IDs before lookup.

## Wire format (`EzSerializer`)

Payloads are UTF-8 JSON:

```json
{
  "id": "myplugin:balance.request",
  "data": { "playerId": "Notch", "requestId": "…" }
}
```

The `id` field must match a registered class; `data` is deserialized with Gson into that type. Packet classes need a **public no-arg constructor** and Gson-friendly fields.

## Core API sketch

- **`EzPacket`** — `String packetId()`; namespaced ID.
- **`EzPacketHandler<T>`** — `void handle(T packet, EzContext context)`.
- **`EzContext`** — optional `playerName`, `sourceServer`, `targetServer` (all strings, may be null).
- **`ServerConnection`** — `send(String server, ServerMessage)`, `broadcast(ServerMessage)`.
- **`EzMessenger`** extends `ServerConnection` and adds `registerHandler(Class<T>, EzPacketHandler<T>)`, plus **default** helpers `send(String, EzPacket)` and `broadcast(EzPacket)` that wrap `ServerMessage.of(packet)` on `EzChannel.DEFAULT`.
- **`ServerMessage`** — `ServerMessage.of(packet)` or `ServerMessage.of(packet, new EzChannel("namespace:key"))` for a non-default channel.
- **`EzPacketRegistry`** — `register(MyPacket.class)` / `register(String id, Class)`; both enforce namespaced IDs.
- **`EzSerializer`** — `byte[] serialize(EzPacket)`, `EzPacket deserialize(byte[], EzPacketRegistry)`.

## Example: shared packet definitions

Keep these in a small shared module or duplicate identical POJOs on proxy and backend (same `packetId()` and fields).

```java
public final class ExamplePackets {

    public static final EzPacketNamespace NS = new EzPacketNamespace("myplugin");

    public static final class BalanceRequest implements EzPacket {
        public String playerId;
        public String requestId;

        public BalanceRequest() {}

        public BalanceRequest(String playerId, String requestId) {
            this.playerId = playerId;
            this.requestId = requestId;
        }

        @Override
        public String packetId() {
            return NS.id("balance.request");
        }
    }

    public static final class BalanceResponse implements EzPacket {
        public String playerId;
        public String requestId;
        public double balance;

        public BalanceResponse() {}

        public BalanceResponse(String playerId, String requestId, double balance) {
            this.playerId = playerId;
            this.requestId = requestId;
            this.balance = balance;
        }

        @Override
        public String packetId() {
            return NS.id("balance.response");
        }
    }
}
```

Do **not** import Bukkit, Velocity, or Bungee types in these classes if you want them reusable from core-style modules.

## Example: Velocity (proxy)

1. Install the **EzFramework** Velocity plugin (`VelocityBootstrap`); it registers `ezframework:channel` and exposes `VelocityEzMessenger`.
2. Your Velocity plugin depends on `ezframework-velocity`, declares a dependency on plugin id `ezframework`, and on startup:

```java
// Obtain VelocityBootstrap from Velocity’s plugin manager (plugin id "ezframework")
VelocityEzMessenger messenger = bootstrap.getMessenger();
EzPacketRegistry reg = messenger.getRegistry();

reg.register(ExamplePackets.BalanceRequest.class);
reg.register(ExamplePackets.BalanceResponse.class);

messenger.registerHandler(ExamplePackets.BalanceResponse.class, (packet, ctx) -> {
    // ctx.getSourceServer() = backend that sent the message
    logger.info("Balance {} for {} (request {})", packet.balance, packet.playerId, packet.requestId);
});

String rid = UUID.randomUUID().toString();
messenger.send("survival", new ExamplePackets.BalanceRequest("Notch", rid));
```

Custom channel per message:

```java
messenger.send("survival",
    ServerMessage.of(new ExamplePackets.BalanceRequest("Notch", rid),
                     new EzChannel("myplugin:data")));
```

## Example: BungeeCord (proxy)

Same pattern using `BungeeEzMessenger` from `ezframework-bungee` and `BungeeBootstrap` as the host plugin. Register packets and handlers the same way; `send` / `broadcast` semantics match `EzMessenger`.

**Note:** BungeeCord plugin messaging only flows when players are connected on the relevant server link; empty servers may drop or fail delivery depending on version and setup.

## Example: backend (Paper / Spigot)

The core library does not ship a Bukkit listener; you wire the channel yourself:

1. Register outgoing/incoming channel `ezframework:channel` (or the same channel you used in `ServerMessage`).
2. On `PluginMessageReceivedEvent` (or equivalent), if the channel matches, deserialize:

```java
EzPacketRegistry reg = new EzPacketRegistry();
reg.register(ExamplePackets.BalanceRequest.class);
reg.register(ExamplePackets.BalanceResponse.class);
EzSerializer serializer = new EzSerializer();

// Inside your listener when channel is EzChannel.DEFAULT.getName():
EzPacket incoming = serializer.deserialize(data, reg);
if (incoming instanceof ExamplePackets.BalanceRequest req) {
    double balance = /* your economy */;
    byte[] out = serializer.serialize(
        new ExamplePackets.BalanceResponse(req.playerId, req.requestId, balance));
    player.sendPluginMessage(plugin, "ezframework:channel", out);
}
```

Adjust method names to your server API version. The critical part is **same channel string**, **same JSON envelope**, and **same namespaced packet IDs** as on the proxy.

## Optional: store `EzMessenger` on Bukkit

For hybrid plugins you can keep a reference in the existing registry:

```java
Registry.forPlugin(plugin).register(EzMessenger.class, messenger);
```

There is no change to `EzPlugin` required; this is optional integration.

## Summary

- Use **`ezframework-core`** for packets, serializer, registry, and interfaces.
- Use **`ezframework-velocity`** or **`ezframework-bungee`** only on the proxy; build them with **`mvn -P proxy`** when working from the EzFramework monorepo.
- Always use **`pluginId:action`** packet IDs via **`EzPacketNamespace`** so every plugin under the same proxy can coexist safely.
