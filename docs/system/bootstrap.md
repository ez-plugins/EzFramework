# Bootstrap

The bootstrap process prepares EzFramework and plugin integrations at
startup. It is intentionally lightweight and focuses on deterministic
initialization order so services are available when plugin code runs.

## Typical startup sequence

1. Load configuration and environment variables.
2. Initialize `StorageProvider` and apply any pending migrations.
3. Initialize the `Registry` and register built-in services.
4. Register commands, GUI components and event listeners.

### Example checklist for `onEnable()`

```text
- load config
- create and init storage provider
- ensure schema/migrations
- register services in registry
- register commands and listeners
```

Keep bootstrap idempotent and avoid blocking the main thread for long
operations; use async tasks or scheduled jobs for heavy migrations.
