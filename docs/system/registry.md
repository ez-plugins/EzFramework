# Registry

The `Registry` is a lightweight service locator used by EzFramework to
publish and discover shared services (storage clients, migration helpers,
and other framework utilities).

## Guidelines

- Keep registrations small and typed: register interfaces rather than
  concrete classes where possible.
- Prefer constructor injection for services your components depend on.
- Use clear lifecycle semantics: register services during bootstrap and
  unregister/close them during shutdown.

### Example usage

```java
Registry registry = new Registry();
registry.register(StorageClient.class, new StorageClient(dataSource));

StorageClient client = registry.get(StorageClient.class).orElseThrow();
```

Avoid using the registry as a global mutable store; prefer it for
well-defined, shared framework services.
