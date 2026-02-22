# Getting Started with EzFramework

This guide walks you through adding EzFramework to your project, creating a minimal plugin, and using the framework's core features: bootstrap, registry, commands, messaging, and storage.

## Prerequisites

- Java 17+ (or the project target version).
- Maven or Gradle build tooling.
- Familiarity with creating server plugins.

## Installation

Add EzFramework as a dependency in your build. Example Maven coordinate (adjust `groupId`/`artifactId`/`version` to match your distribution):

```xml
<dependency>
  <groupId>com.skyblockexp</groupId>
  <artifactId>ezframework</artifactId>
  <version>1.0.0</version>
</dependency>
```

Or the equivalent Gradle entry:

```groovy
implementation 'com.skyblockexp:ezframework:1.0.0'
```

## Minimal Plugin Example

Create a main plugin class that integrates with EzFramework's lifecycle.

```java
package com.example.myplugin;

import com.skyblockexp.ezframework.EzPlugin;

public class MyPlugin extends EzPlugin {
    @Override
    public void onEnable() {
        // Typical initialization occurs in bootstrap components
    }
}
```

### Registering Components

Use the framework's bootstrap components to register managers and providers. A common pattern is to create a bootstrap `Component` implementation to register your `Manager` instances with the `Registry`.

Example sketch:

```java
public class MyBootstrap implements Component {
    @Override
    public void bootstrap(Bootstrap bootstrap) {
        bootstrap.getRegistry().register(new MyManager());
        bootstrap.getRegistry().register(new MyStorageProvider());
    }
}
```

## Commands

EzFramework provides `EzCmd` and `Subcommand` utilities to define command trees and execution handlers. See the command docs for detailed API and examples.

Files: [command/ez_cmd.md](command/ez_cmd.md), [command/subcommand.md](command/subcommand.md)

## Messaging

The framework includes a messaging API and provider abstraction. Use the `MessageProvider` interface to create or swap message backends (mini-message, custom providers). See the messaging docs for formatting and color codes.

Files: [message/mini_message.md](message/mini_message.md), [message/color_codes.md](message/color_codes.md)

## Storage

Use `StorageProvider` implementations to persist data. An example YAML provider is included with the framework; implement `StorageProvider` or extend `AbstractRepository` to create custom persistence layers.

File: [storage/storage_provider.md](storage/storage_provider.md)

## Build & Run

- Build with Maven: `mvn clean package`.
- Place the resulting plugin JAR into your server's `plugins` folder and start the server.

## Next Steps

1. Read [system/bootstrap.md](system/bootstrap.md) to understand lifecycle and initialization.
2. Read the command and storage guides to register managers and create usable APIs.

---

If you'd like, I can now draft `command/ez_cmd.md` with API examples and code snippets. Proceed?
