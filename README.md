# EzFramework

[![CI](https://github.com/ez-plugins/EzFramework/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/ez-plugins/EzFramework/actions) [![Release](https://img.shields.io/github/v/release/ez-plugins/EzFramework?label=release)](https://github.com/ez-plugins/EzFramework/releases) [![Issues](https://img.shields.io/github/issues/ez-plugins/EzFramework)](https://github.com/ez-plugins/EzFramework/issues) [![License](https://img.shields.io/github/license/ez-plugins/EzFramework)](https://github.com/ez-plugins/EzFramework#license)

Lightweight Java framework utilities to simplify Bukkit/Spigot plugin development.

- Name: EzFramework
- Package: `com.skyblockexp.ezframework`
- Developed by: Shadow48402
- Maintained by: EzPlugins

## Overview

EzFramework provides a compact set of utilities for Bukkit/Spigot plugins:

- A per-plugin `Registry` for storing and initializing managers and services.
- A `Manager` base type with simple lifecycle methods (`init()` / `shutdown()`).
- A `Bootstrap`/`Component` system and a convenience base plugin `EzPlugin` to coordinate startup.

## Quick start

1. Add EzFramework as a dependency (install to your local repo or include as a module).
2. Extend `EzPlugin` and provide bootstrap `Component`s via `components()`.

## Using with JitPack

Add the JitPack repository to your consumer `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then depend on the module coordinates exposed by this POM. Example (use the repo tag/version you want):

```xml
<dependency>
    <groupId>com.github.ez-plugins.EzFramework</groupId>
    <artifactId>ezframework-core</artifactId>
    <version>0.2.1</version>
</dependency>

<dependency>
    <groupId>com.github.ez-plugins.EzFramework</groupId>
    <artifactId>message-minimessage</artifactId>
    <version>0.2.1</version>
</dependency>
```

## Example

The `EzPlugin` base class makes `onEnable()` and `onDisable()` final; provide startup logic
by returning a list of `Component`s from `components()`:

```java
import java.util.Arrays;
import java.util.List;

import com.skyblockexp.ezframework.EzPlugin;
import com.skyblockexp.ezframework.Registry;
import com.skyblockexp.ezframework.bootstrap.Component;
import com.skyblockexp.ezframework.bootstrap.component.RegistryBootstrap;

public class MyPlugin extends EzPlugin {
    @Override
    protected List<Component> components() {
        return Arrays.asList(
            new RegistryBootstrap(this),
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

## Notes

- `EzPlugin` calls `components()` during startup; do not override `onEnable()`/`onDisable()`.
- Use `Registry.forPlugin(plugin)` to access the per-plugin `Registry` instance.
- Register managers by string key or by `Class` and let `initAll()` / `shutdownAll()` manage lifecycles.

## Documentation

See the `docs/` folder for guides and API details:

- [Getting Started](docs/getting_started.md)
- [Overview & CLI docs](docs/README.md)

Command utilities and builders:

- [Command builder guide](docs/command/command_builder.md)
- [ez-cmd usage](docs/command/ez_cmd.md)
- [Subcommand guide](docs/command/subcommand.md)
- [Command README](docs/command/README.md)

Message utilities:

- [Message provider](docs/message/message_provider.md)
- [MiniMessage support](docs/message/mini_message.md)
- [Color codes](docs/message/color_codes.md)
- [Message README](docs/message/README.md)

Storage & DB:

- [Storage providers](docs/storage/storage_provider.md)
- [MySQL integration](docs/storage/mysql_package.md)
- [Schema & migrations](docs/storage/schema.md)
- [Models reference](docs/storage/models.md)
- [Storage README](docs/storage/README.md)

System & bootstrap:

- [Plugin bootstrap and lifecycle](docs/system/bootstrap.md)
- [EzPlugin details](docs/system/ez_plugin.md)
- [Registry & registry patterns](docs/system/registry.md)

## Contributing

Contributions welcome via PR. Follow the repository conventions and run tests before submitting.

## License

Developed by Shadow48402 and maintained by EzPlugins.
