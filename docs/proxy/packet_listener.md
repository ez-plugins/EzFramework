# Proxy Packet Listener

One-sentence summary: register annotation-driven listeners for proxy packet events using a familiar `@PacketListener` / `EventPriority` model that mirrors the Bukkit event system.

---

## Overview

EzFramework ships an event bus for the proxy layer (`ezframework-api`, package `com.skyblockexp.ezframework.proxy.event`) that lets you react to incoming packets with the same mental model as Bukkit's `@EventHandler`. There is no platform-specific code — the bus is pure Java and works identically on Velocity and BungeeCord.

Key classes:

| Class / type | Role |
| --- | --- |
| `ProxyPacketEvent` | Base interface all custom packet events implement |
| `@PacketListener` | Method annotation that marks a handler |
| `EventPriority` | Enum controlling handler invocation order |
| `ProxyEventManager` | Event bus — register listeners, fire events |
| `EzMessenger#bridgeEvent(...)` | Wires an incoming `EzPacket` type directly to the manager |

---

## 1. Define a custom packet event

Implement `ProxyPacketEvent`. The interface requires three methods (`getContext()`, `isCancelled()`, `setCancelled(boolean)`). Carry your packet-specific fields as additional state.

```java
import com.skyblockexp.ezframework.proxy.EzContext;
import com.skyblockexp.ezframework.proxy.event.ProxyPacketEvent;

public class PlayerLoginPacketEvent implements ProxyPacketEvent {

    private final EzContext context;
    private final String playerName;
    private boolean cancelled;

    public PlayerLoginPacketEvent(EzContext context, String playerName) {
        this.context = context;
        this.playerName = playerName;
    }

    public String getPlayerName() { return playerName; }

    @Override public EzContext getContext()            { return context; }
    @Override public boolean   isCancelled()           { return cancelled; }
    @Override public void      setCancelled(boolean c) { this.cancelled = c; }
}
```

> The `EzContext` is automatically populated by `bridgeEvent()` when the packet arrives; it gives you `getPlayerName()` (the player whose connection carried the packet) and `getSourceServer()` (the originating backend server).

---

## 2. Write a listener class

Annotate public methods with `@PacketListener`. The method must accept exactly one parameter that extends `ProxyPacketEvent`.

```java
import com.skyblockexp.ezframework.proxy.event.EventPriority;
import com.skyblockexp.ezframework.proxy.event.PacketListener;

public class MyProxyListener {

    @PacketListener
    public void onLogin(PlayerLoginPacketEvent event) {
        if (event.getPlayerName().equals("BannedPlayer")) {
            event.setCancelled(true);
        }
    }

    // MONITOR fires even when the event is already cancelled — use it for
    // logging or metrics, never to mutate state.
    @PacketListener(priority = EventPriority.MONITOR)
    public void onLoginMonitor(PlayerLoginPacketEvent event) {
        if (!event.isCancelled()) {
            System.out.println(event.getPlayerName() + " logged in from "
                    + event.getContext().getSourceServer());
        }
    }
}
```

### Priority table

| Priority | Fires | Typical use |
| --- | --- | --- |
| `LOWEST` | First | Early read / soft-cancel |
| `LOW` | 2nd | Low-priority mutations |
| `NORMAL` | 3rd (default) | Main business logic |
| `HIGH` | 4th | Override earlier decisions |
| `HIGHEST` | 5th | Final say on mutations |
| `MONITOR` | Last — **always**, even if cancelled | Logging, metrics, auditing |

`ignoreCancelled = true` causes the handler to be skipped when the event is already cancelled **except at `MONITOR` priority**, which always invokes.

---

## 3. Construct and populate a `ProxyEventManager`

```java
import java.util.logging.Logger;
import com.skyblockexp.ezframework.proxy.event.ProxyEventManager;

ProxyEventManager eventManager = new ProxyEventManager(logger);
eventManager.registerListener(new MyProxyListener());
```

`registerListener` reflects over all `@PacketListener` methods at registration time. Invalid signatures (wrong parameter count, parameter type does not extend `ProxyPacketEvent`) throw `IllegalArgumentException` immediately.

To remove all handlers contributed by a listener:

```java
eventManager.unregisterAll(myListenerInstance);
```

---

## 4. Wire to `EzMessenger` via `bridgeEvent()`

`EzMessenger#bridgeEvent()` connects a specific `EzPacket` type to the `ProxyEventManager` in one call. When the messenger receives a packet of that type it will call the factory to construct the event and fire it.

```java
// factory: (packet, context) -> new PlayerLoginPacketEvent(context, packet.getPlayerName())
messenger.bridgeEvent(
    PlayerLoginPacket.class,
    (packet, ctx) -> new PlayerLoginPacketEvent(ctx, packet.getPlayerName()),
    eventManager);
```

`bridgeEvent()` is a default method on `EzMessenger`, so it is available on both `BungeeEzMessenger` and `VelocityEzMessenger` without any changes to those classes.

---

## 5. Full Velocity example

```java
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.skyblockexp.ezframework.proxy.EzPacketRegistry;
import com.skyblockexp.ezframework.proxy.event.ProxyEventManager;
import com.skyblockexp.ezframework.proxy.velocity.VelocityBootstrap;

@Plugin(id = "myplugin", name = "MyPlugin", version = "1.0")
public class MyVelocityPlugin extends VelocityBootstrap {

    private ProxyEventManager eventManager;

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent initEvent) {
        initMessenger(new EzPacketRegistry().register(PlayerLoginPacket.class));

        eventManager = new ProxyEventManager(logger);
        eventManager.registerListener(new MyProxyListener());

        // Bridge: any incoming PlayerLoginPacket fires a PlayerLoginPacketEvent
        getMessenger().bridgeEvent(
            PlayerLoginPacket.class,
            (packet, ctx) -> new PlayerLoginPacketEvent(ctx, packet.getPlayerName()),
            eventManager);
    }
}
```

---

## 6. Full BungeeCord example

```java
import net.md_5.bungee.api.plugin.Plugin;
import com.skyblockexp.ezframework.proxy.EzPacketRegistry;
import com.skyblockexp.ezframework.proxy.event.ProxyEventManager;
import com.skyblockexp.ezframework.proxy.bungee.BungeeBootstrap;

public class MyBungeePlugin extends BungeeBootstrap {

    private ProxyEventManager eventManager;

    @Override
    public void onEnable() {
        initMessenger(new EzPacketRegistry().register(PlayerLoginPacket.class));

        eventManager = new ProxyEventManager(getLogger());
        eventManager.registerListener(new MyProxyListener());

        getMessenger().bridgeEvent(
            PlayerLoginPacket.class,
            (packet, ctx) -> new PlayerLoginPacketEvent(ctx, packet.getPlayerName()),
            eventManager);
    }
}
```

---

## 7. Firing events manually

You can also fire events outside of the packet bridge (e.g. from your own plugin logic):

```java
PlayerLoginPacketEvent event = new PlayerLoginPacketEvent(
    new EzContext("Alex", "survival", null), "Alex");

eventManager.fireEvent(event);

if (!event.isCancelled()) {
    // continue with processing
}
```

---

## Cancellation semantics summary

1. Any handler may call `event.setCancelled(true)`.
2. Subsequent handlers at the same or higher priority with `ignoreCancelled = true` will be skipped.
3. `MONITOR`-priority handlers **always** execute regardless of cancellation state or the `ignoreCancelled` flag.
4. The fired event object is the same mutable instance throughout; handlers may read `event.isCancelled()` to check the current state.

---

## See also

- [Cross-server messaging overview](cross_server_messaging.md)
- [Velocity transport](velocity.md)
- [BungeeCord transport](bungee.md)
- [Wire format](wire_format.md)
