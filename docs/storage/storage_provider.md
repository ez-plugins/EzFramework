# Storage Provider

`StorageProvider` is the core abstraction used by EzFramework to store
and retrieve raw key/value data. Implementations provide persistence
backing (YAML, MySQL, etc.) while the framework exposes higher-level
helpers such as `StorageClient` and `ModelRepository`.

## Key responsibilities

- Provide `save`, `load`, `delete` and `query` primitives.
- Handle serialization details (binary, JSON, or Map-based storage).
- Offer lifecycle hooks: `init`, `close`, and optional migration helpers.

## Simple implementation example (conceptual)

```java
public class InMemoryStorageProvider implements StorageProvider {
    private final Map<String, Map<String, Object>> store = new ConcurrentHashMap<>();

    @Override
    public void save(String path, Map<String, Object> data) {
        store.put(path, new HashMap<>(data));
    }

    @Override
    public Optional<Map<String, Object>> load(String path) {
        return Optional.ofNullable(store.get(path)).map(HashMap::new);
    }

    // query/delete/etc.
}
```

Use `StorageProvider` implementations with `StorageClient` or
`ModelRepository` for typed access and convenience helpers.
