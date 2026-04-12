# Getting Started with EzFramework

This guide walks you through adding EzFramework to your project and creating a minimal plugin using
the framework's core features: bootstrap, registry, commands, messaging, and storage.

## Prerequisites

- Java 17 or later.
- Maven 3.8+ build tooling.
- A server plugin project (Bukkit/Spigot/Paper).

## Installation

### JitPack (recommended)

Add the JitPack repository and the modules you need to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- Core Bukkit integration -->
    <dependency>
        <groupId>com.github.ez-plugins.EzFramework</groupId>
        <artifactId>ezframework-core</artifactId>
        <version>0.3.0</version>
    </dependency>

    <!-- Optional: MySQL storage -->
    <dependency>
        <groupId>com.github.ez-plugins.EzFramework</groupId>
        <artifactId>storage-mysql</artifactId>
        <version>0.3.0</version>
    </dependency>

    <!-- Optional: MiniMessage formatting -->
    <dependency>
        <groupId>com.github.ez-plugins.EzFramework</groupId>
        <artifactId>message-minimessage</artifactId>
        <version>0.3.0</version>
    </dependency>
</dependencies>
```

### Local build

Clone and install to your local Maven repository:

```bash
git clone https://github.com/ez-plugins/EzFramework.git
cd EzFramework
mvn install -P proxy   # -P proxy also builds Velocity & BungeeCord modules
```

## Minimal Plugin

Extend `EzPlugin` and implement `components()`. The `onEnable()` and `onDisable()` methods are
`final` — put all startup and shutdown logic inside `Component` implementations:

```java
package com.example.myplugin;

import com.skyblockexp.ezframework.EzPlugin;
import com.skyblockexp.ezframework.Registry;
import com.skyblockexp.ezframework.bootstrap.Component;

import java.util.List;

public class MyPlugin extends EzPlugin {

    @Override
    protected List<Component> components() {
        return List.of(
            new Component() {
                @Override
                public void start() throws Exception {
                    // Register services and call initAll() to trigger init() on Managers
                    Registry.forPlugin(MyPlugin.this).register("economy", new MyEconomyService());
                    Registry.forPlugin(MyPlugin.this).initAll();
                }

                @Override
                public void stop() throws Exception {
                    Registry.forPlugin(MyPlugin.this).shutdownAll();
                }
            }
        );
    }
}
```

## Registering Services

Access the per-plugin `Registry` anywhere via `Registry.forPlugin(plugin)`:

```java
// register by string key
registry.register("economy", economyService);

// register by class (uses fully-qualified class name as key)
registry.register(EconomyService.class, economyService);

// retrieve
EconomyService svc = registry.get(EconomyService.class);
```

Implement `Manager` to opt into automatic lifecycle calls (called by `initAll()` / `shutdownAll()`):

```java
public class MyEconomyService implements Manager {
    @Override public void init() { /* open DB connection, etc. */ }
    @Override public void shutdown() { /* close resources */ }
}
```

## Storage

Use `StorageRegistry` to register a provider globally and `AbstractRepository` to build typed
repositories:

```java
// Register a named provider (e.g. in your start component)
StorageRegistry.register(new MysqlStorageProvider(dataSource));

// A typed repository keyed by player UUID
public class PlayerRepo extends AbstractRepository<PlayerData, String> {
    public PlayerRepo(StorageProvider provider) { super(provider, "players"); }

    @Override
    protected PlayerData fromMap(String id, Map<String, Object> data) {
        return new PlayerData(id, (Integer) data.getOrDefault("coins", 0));
    }
}
```

See [storage/storage_provider.md](storage/storage_provider.md) and [storage/models.md](storage/models.md)
for the full storage API.

## Commands

Extend `EzCmd` and add `Subcommand` instances:

```java
public class EconomyCommand extends EzCmd {
    public EconomyCommand() {
        super("economy");
        addSubcommand(new BalanceSubcommand());
    }
}
```

See [command/ez_cmd.md](command/ez_cmd.md) for the full command API.

## Messaging

Use `Messaging.forPlugin(plugin)` to format and send messages:

```java
String formatted = Messaging.forPlugin(this).format("<gold>Hello, <name>!",
    Map.of("name", sender.getName()));
sender.sendMessage(formatted);
```

See [message/mini_message.md](message/mini_message.md) for format tags and configuration.

## Build & Run

```bash
mvn clean package
# Copy target/myplugin-*.jar to your server's plugins/ folder and start the server.
```

## Next Steps

- [system/bootstrap.md](system/bootstrap.md) — bootstrap ordering and component patterns.
- [system/registry.md](system/registry.md) — registry patterns and typed access.
- [storage/schema.md](storage/schema.md) — database migrations.
- [proxy/overview.md](proxy/overview.md) — cross-server messaging.
- [gui/README.md](gui/README.md) — inventory GUI.
