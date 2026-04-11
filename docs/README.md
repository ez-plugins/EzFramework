
# EzFramework — Documentation

> Lightweight Java plugin framework for modular server extensions.

## Overview

EzFramework provides a small, focused foundation for building modular plugins. It defines a clear lifecycle for components, a lightweight registry and bootstrap system, built-in command handling, a messaging API, and simple pluggable storage providers.

This documentation contains guided usage, API overviews, and practical examples to get you productive quickly.

## Table of Contents

- [Getting Started](getting_started.md)
- Commands
  - [EzCmd reference](command/ez_cmd.md)
  - [Subcommands](command/subcommand.md)
- Messaging
  - [Message provider overview](message/mini_message.md)
  - [Color codes & formatting](message/color_codes.md)
- Proxy (cross-server)
  - [Cross-server messaging (Velocity / BungeeCord)](proxy/cross_server_messaging.md)
- Storage
  - [Storage provider guide](storage/storage_provider.md)
  - (Repository API details) (storage/storage_provider.md)
- System
  - [Bootstrap & lifecycle](system/bootstrap.md)
  - [Registry usage](system/registry.md)

## Quick Start

1. Add EzFramework as a dependency to your plugin project.
2. Create your main plugin class by extending the framework's base plugin class and register your managers or services during bootstrap.

Example minimal plugin skeleton:

```java
public class MyPlugin extends EzPlugin {
    @Override
    public void onEnable() {
        // Register managers, providers, commands here
    }
}
```

Key concepts:

- **Bootstrap** — the startup sequence that initializes framework components.
- **Registry** — central lookup for managers and services.
- **Manager** — encapsulated service for a particular domain (commands, storage, messaging).
- **StorageProvider** — pluggable persistence implementations (e.g., YAML provider).

## Recommended Reading Order

1. `getting_started.md` — install and run a quick example.
2. `system/bootstrap.md` and `system/registry.md` — understand lifecycle and service registration.
3. `command/ez_cmd.md` and `command/subcommand.md` — implement commands.
4. `message/*` — messaging APIs and formatting.
5. `proxy/cross_server_messaging.md` — proxy ↔ backend plugin channels and packet protocol.
6. `storage/*` — data persistence and providers.

## Contributing

Contributions are welcome. Please follow the repository's code style and include concise tests for any behavioral changes.

---

If you'd like, I can now populate `getting_started.md` with installation steps and a complete quick-start example. Proceed?
