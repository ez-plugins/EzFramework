# Registry

`Registry` is EzFramework's per-plugin service locator. Each plugin gets its own isolated registry;
use `Registry.forPlugin(plugin)` to access it from anywhere.

## Registration

```java
Registry reg = Registry.forPlugin(this);

// By string key
reg.register("economy", new EconomyService());

// By class (key = fully-qualified class name)
reg.register(EconomyService.class, new EconomyService());
```

## Retrieval

```java
// By string key — returns Optional<Object>
Optional<Object> raw = reg.get("economy");

// By string key + expected class — returns T or null
EconomyService svc = reg.get("economy", EconomyService.class);

// By class — returns T or null
EconomyService svc = reg.get(EconomyService.class);

// All entries — returns unmodifiable Map<String, Object>
Map<String, Object> all = reg.getAll();
```

## Lifecycle management

Implement `Manager` to participate in bulk lifecycle calls:

```java
public class EconomyService implements Manager {
    @Override public void init()     { /* open resources */ }
    @Override public void shutdown() { /* close resources */ }
}
```

Then call on the registry:

```java
reg.initAll();     // calls init()  on every registered Manager
reg.shutdownAll(); // calls shutdown() on every registered Manager (reverse order)
```

Objects that are not `Manager` instances but have a no-arg `init()` or `load()` method will also
have that method invoked reflectively by `initAll()`.

## Guidelines

- Register by class when there is only one implementation per service type.
- Register by string key when you need multiple instances of the same type.
- Prefer registering during `start()` and calling `initAll()` after all services are registered.
- Access the plugin instance backing a registry via `reg.getPlugin()`.
