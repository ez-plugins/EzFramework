# EzPlugin

`EzPlugin` (in `ezframework-core`) extends Bukkit's `JavaPlugin` and coordinates the framework
bootstrap. Plugin authors extend it and implement a single abstract method.

## AbstractMethod

```java
protected abstract List<Component> components();
```

Return the ordered list of `Component`s that make up your plugin's startup sequence.
Components are started in list order and stopped in reverse order.

## Lifecycle

| Event | What happens |
|---|---|
| `onEnable()` | Auto-registers config/migration components if their modules are present; registers your `components()`; calls `Bootstrap.startAll()` |
| `onDisable()` | Calls `Bootstrap.stopAll()` |

Both methods are `final` — do not override them.

## Auto-registered components

When certain optional modules are on the classpath, `EzPlugin.onEnable()` automatically registers
additional bootstrap components before yours:

- `ConfigBootstrapComponent` — if a `PlatformConfigProvider` SPI is found (i.e. `ezframework-config-bukkit` is present).
- `ConfigBootstrap` and `ConfigManager` — if `ezframework-config` is present and the classes are loadable.

These run before your own components, so config is available when your `start()` methods execute.

## Accessing the bootstrap

```java
Bootstrap b = getBootstrap(); // protected method — accessible from your plugin class
```

## Minimal skeleton

```java
public class MyPlugin extends EzPlugin {

    @Override
    protected List<Component> components() {
        return List.of(
            new MigrationBootstrap(this),  // apply pending DB migrations
            new ManagerInitComponent(this), // call initAll() on registered managers
            new Component() {
                @Override
                public void start() {
                    // register commands, listeners, etc.
                }
            }
        );
    }
}
```

## Notes

- `getBootstrap()` is `protected final` — only accessible from the plugin class itself.
- The framework does not ship its own `plugin.yml`; provide one for your plugin as normal.
