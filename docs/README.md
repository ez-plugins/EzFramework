# EzFramework — Documentation

> Lightweight Java plugin framework for modular server extensions. Version 0.3.0.

## Overview

EzFramework provides a focused foundation for building modular Bukkit/Spigot plugins. It defines a
clear lifecycle for components, a per-plugin registry and bootstrap system, built-in command
handling, a messaging API, pluggable storage providers, a proxy messaging layer, and a GUI
abstraction.

## Table of Contents

- [Getting Started](getting_started.md)
- System
  - [Bootstrap & lifecycle](system/bootstrap.md)
  - [EzPlugin details](system/ez_plugin.md)
  - [Registry usage](system/registry.md)
- Commands
  - [EzCmd reference](command/ez_cmd.md)
  - [Subcommand guide](command/subcommand.md)
  - [CommandBuilder reference](command/command_builder.md)
- Messaging
  - [MiniMessage formatting](message/mini_message.md)
  - [Color codes & formatting](message/color_codes.md)
  - [Message provider](message/message_provider.md)
- Storage
  - [Storage provider guide](storage/storage_provider.md)
  - [Schema & migrations](storage/schema.md)
  - [Models reference](storage/models.md)
  - [MySQL integration](storage/mysql_package.md)
- Proxy (cross-server)
  - [Overview](proxy/overview.md)
  - [Quick start](proxy/quick_start.md)
  - [Wire format](proxy/wire_format.md)
  - [Velocity integration](proxy/velocity.md)
  - [BungeeCord integration](proxy/bungee.md)
- GUI
  - [GUI overview](gui/README.md)
  - [MenuBuilder](gui/menu_builder.md)
  - [GUI actions](gui/gui_action.md)
  - [EzGUI integration](gui/ez_gui.md)
- Config
  - [EzConfig](config/ez_config.md)

## Quick Start

1. Add `ezframework-core` (and any optional modules) as a dependency.
2. Extend `EzPlugin` and implement `components()` to return your bootstrap components.
3. `onEnable()` and `onDisable()` are `final` — all lifecycle logic lives in components.

```java
public class MyPlugin extends EzPlugin {
    @Override
    protected List<Component> components() {
        return List.of(
            new Component() {
                @Override public void start() throws Exception {
                    Registry.forPlugin(MyPlugin.this).register("svc", new MyService());
                    Registry.forPlugin(MyPlugin.this).initAll();
                }
                @Override public void stop() throws Exception {
                    Registry.forPlugin(MyPlugin.this).shutdownAll();
                }
            }
        );
    }
}
```

Key concepts:

- **Bootstrap** — the `Bootstrap` class holds an ordered list of `Component`s; calls `start()` on each in order, `stop()` in reverse.
- **Registry** — per-plugin service locator (`Registry.forPlugin(plugin)`). Supports string keys and class keys.
- **Manager** — implement `Manager` to get `init()`/`shutdown()` called by `Registry.initAll()`/`shutdownAll()`.
- **StorageProvider** — pluggable persistence interface; use `StorageRegistry` to register providers globally.
- **EzModel** — base class for simple domain objects; backed by a `Map<String, Object>` with fill/guard controls.

## Recommended Reading Order

1. `getting_started.md` — install and run a minimal example.
2. `system/bootstrap.md` and `system/registry.md` — understand lifecycle and service registration.
3. `command/ez_cmd.md` — implement commands.
4. `message/mini_message.md` — messages and formatting.
5. `storage/storage_provider.md` and `storage/models.md` — data persistence.
6. `proxy/overview.md` — cross-server messaging if you use a proxy.
7. `gui/README.md` — inventory GUI if you need it.
