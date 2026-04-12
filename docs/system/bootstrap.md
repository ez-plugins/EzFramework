# Bootstrap

The `Bootstrap` class manages an ordered list of `Component`s and drives the plugin lifecycle.
`EzPlugin` creates one `Bootstrap` instance internally; components returned from
`components()` are registered into it before `onEnable()` finishes setup.

## How it works

```text
onEnable()
  → Bootstrap.register(c1), register(c2), ...
  → Bootstrap.startAll()   — calls c.start() in registration order; stops already-started
                             components on first exception and re-throws
onDisable()
  → Bootstrap.stopAll()    — calls c.stop() in reverse registration order;
                             exceptions are logged and suppressed (best-effort)
```

Each `Component` provides two lifecycle methods (both `throws Exception`):

```java
public interface Component {
    void start() throws Exception;
    void stop()  throws Exception;   // default: no-op
    default void reload() throws Exception { stop(); start(); }
}
```

## Typical startup sequence

A recommended ordering for `components()`:

1. Config loading component (or auto-registered `ConfigBootstrapComponent`).
2. Storage provider registration + schema migration (`MigrationBootstrap`).
3. `ManagerInitComponent` — calls `Registry.initAll()`.
4. Plugin-specific components (commands, event listeners, GUI setup).

Example:

```java
@Override
protected List<Component> components() {
    return List.of(
        new MigrationBootstrap(this),
        new ManagerInitComponent(this),
        new Component() {
            @Override public void start() {
                getServer().getPluginManager().registerEvents(new MyListener(), MyPlugin.this);
            }
        }
    );
}
```

## Available built-in components (ezframework-core)

| Class | Purpose |
| --- | --- |
| `RegistryBootstrap` | Ensures a `Registry` exists for the plugin |
| `MigrationBootstrap` | Discovers and applies pending SQL/Java migrations |
| `ManagerInitComponent` | Calls `Registry.initAll()` on all registered managers |
| `ConfigBootstrapComponent` | Loads plugin config via registered `PlatformConfigProvider` |

## Notes

- Keep components small and single-purpose — one concern per component.
- Avoid blocking the main thread in `start()`; schedule heavy operations asynchronously.
- `stopAll()` swallows exceptions to give all components a chance to clean up.
- `startAll()` is fail-fast: the first exception stops the sequence and shuts down already-started components.
