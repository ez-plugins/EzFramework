# EzFramework

[![CI](https://github.com/ez-plugins/EzFramework/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/ez-plugins/EzFramework/actions) [![Release](https://img.shields.io/github/v/release/ez-plugins/EzFramework?label=release)](https://github.com/ez-plugins/EzFramework/releases) [![Issues](https://img.shields.io/github/issues/ez-plugins/EzFramework)](https://github.com/ez-plugins/EzFramework/issues) [![License](https://img.shields.io/github/license/ez-plugins/EzFramework)](https://github.com/ez-plugins/EzFramework#license)

Lightweight Java framework utilities to simplify Bukkit/Spigot plugin development.

## Overview

EzFramework is a modular multi-module library for Bukkit/Spigot plugins:

| Module | Artifact | Description |
|---|---|---|
| API | `ezframework-api` | Platform-free API: bootstrap, storage, proxy packets, GUI definitions, config |
| Core | `ezframework-core` | Bukkit integration: `EzPlugin`, `Registry`, commands, messaging, YAML storage |
| Config | `ezframework-config` | `ConfigRegistry` and `EzConfig` abstraction |
| Config Bukkit | `ezframework-config-bukkit` | Bukkit `PlatformConfigProvider` adapter |
| GUI | `ezframework-gui` | Bukkit inventory GUI implementation |
| MySQL | `storage-mysql` | MySQL/JDBC `StorageProvider` |
| MiniMessage | `message-minimessage` | MiniMessage formatting adapter |
| CLI | `ezframework-cli` | `ez` CLI scaffolding tool |
| Velocity | `ezframework-velocity` | Velocity proxy transport |
| BungeeCord | `ezframework-bungee` | BungeeCord proxy transport |

Key capabilities:

- `Bootstrap`/`Component` lifecycle — deterministic ordered startup and shutdown.
- Per-plugin `Registry` — typed service locator; supports `Manager` `init()`/`shutdown()` lifecycle.
- `StorageRegistry` + `AbstractRepository` — pluggable persistence with optional SQL migrations.
- `EzModel` — lightweight domain model with `fill`, `getAs`, `toMap`/`fromMap`, and query builder.
- Proxy messaging — namespaced packets serialised over plugin channels; Velocity and BungeeCord transports included.
- GUI — platform-agnostic `MenuDefinition`/`MenuBuilder` with per-slot `GuiAction`s.
- Config — `ConfigRegistry` with per-plugin `EzConfig`s and an optional default.

## Quick start

1. Add EzFramework as a dependency (install to your local Maven repo or use JitPack).
2. Extend `EzPlugin` and return your bootstrap `Component`s from `components()`.
3. `onEnable()` and `onDisable()` are `final` — all lifecycle logic goes in components.

## Using with JitPack

Add the JitPack repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add the modules you need (replace `0.3.0` with the desired release tag):

```xml
<!-- Bukkit integration: EzPlugin, Registry, commands, messaging, YAML storage -->
<dependency>
    <groupId>com.github.ez-plugins.EzFramework</groupId>
    <artifactId>ezframework-core</artifactId>
    <version>0.3.0</version>
</dependency>

<!-- Optional: MySQL storage provider -->
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
```

Build and install proxy artifacts locally when you need them:

```bash
mvn install -P proxy
```

## Example

`EzPlugin` makes `onEnable()` and `onDisable()` final. Return your `Component`s from `components()`:

```java
import java.util.List;
import com.skyblockexp.ezframework.EzPlugin;
import com.skyblockexp.ezframework.Registry;
import com.skyblockexp.ezframework.bootstrap.Component;

public class MyPlugin extends EzPlugin {

    @Override
    protected List<Component> components() {
        return List.of(
            new Component() {
                @Override
                public void start() throws Exception {
                    Registry.forPlugin(MyPlugin.this).register("economy", new MyEconomyManager());
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

Key rules:
- `components()` controls startup order; the list is processed in declaration order.
- Use `Registry.forPlugin(plugin)` to obtain the per-plugin registry anywhere.
- Register managers by string key (`register("name", obj)`) or by class (`register(MyManager.class, obj)`).
- Call `initAll()` after registering all managers; call `shutdownAll()` in your stop component.

## Documentation

See the `docs/` folder for guides and API references:

- [Getting Started](docs/getting_started.md)
- [Full table of contents](docs/README.md)

System & bootstrap:

- [Bootstrap & lifecycle](docs/system/bootstrap.md)
- [EzPlugin details](docs/system/ez_plugin.md)
- [Registry patterns](docs/system/registry.md)

Commands:

- [EzCmd usage](docs/command/ez_cmd.md)
- [Subcommand guide](docs/command/subcommand.md)
- [CommandBuilder reference](docs/command/command_builder.md)

Messages:

- [MiniMessage support](docs/message/mini_message.md)
- [Color codes](docs/message/color_codes.md)
- [Message provider](docs/message/message_provider.md)

Storage:

- [Storage providers](docs/storage/storage_provider.md)
- [Schema & migrations](docs/storage/schema.md)
- [Models reference](docs/storage/models.md)
- [MySQL integration](docs/storage/mysql_package.md)

Proxy messaging:

- [Overview](docs/proxy/overview.md)
- [Quick start](docs/proxy/quick_start.md)
- [Wire format](docs/proxy/wire_format.md)
- [Velocity integration](docs/proxy/velocity.md)
- [BungeeCord integration](docs/proxy/bungee.md)

GUI:

- [GUI overview](docs/gui/README.md)
- [MenuBuilder](docs/gui/menu_builder.md)
- [GUI actions](docs/gui/gui_action.md)

Config:

- [EzConfig](docs/config/ez_config.md)

## Contributing

Contributions welcome via PR. Follow the repository conventions and run `mvn clean install -P proxy`
before submitting to ensure all tests pass.

## License

Developed by Shadow48402 and maintained by EzPlugins.
