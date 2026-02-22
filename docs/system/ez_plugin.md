# Main plugin class - EzPlugin

The `EzPlugin` class is the recommended base for plugins integrating with
EzFramework. It performs framework bootstrap, registers core services and
provides convenient lifecycle hooks for plugin authors.

## Typical responsibilities

- Initialize `StorageProvider`, `Registry` and other core components.
- Register commands, GUI handlers and event listeners.
- Provide a short, stable API surface for third-party integrations.

### Simple skeleton

```java
public class MyPlugin extends EzPlugin {
    @Override
    public void onEnable() {
        super.onEnable();

        // Initialize framework services
        StorageProvider provider = StorageProviderFactory.createFromConfig(getConfig());
        provider.init(this);

        // Register app-specific services
        getRegistry().register(MyService.class, new MyServiceImpl());
    }
}
```

Extend `EzPlugin` when you want the framework to manage shared services
and provide consistent lifecycle behavior across plugins.
