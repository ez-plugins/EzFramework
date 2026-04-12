# Storage Provider

`StorageProvider` (in `ezframework-api`) is the core persistence abstraction. Implementations
provide a concrete backing store while the framework exposes higher-level helpers such as
`AbstractRepository` and `EzModel`.

## Interface contract

```java
public interface StorageProvider {
    /** Unique name identifying this provider (used in StorageRegistry). */
    String name();

    /** Called once at startup; receives the host plugin instance. */
    void init(Object plugin) throws Exception;

    /** Called at shutdown to release resources. */
    void close() throws Exception;

    /** Persist a record by ID. */
    void save(String id, Map<String, Object> data) throws Exception;

    /** Load a record by ID; empty Optional if not found. */
    Optional<Map<String, Object>> load(String id) throws Exception;

    /** Delete a record by ID. */
    void delete(String id) throws Exception;

    /** Load all records as a map of ID → data. */
    Map<String, Map<String, Object>> loadAll() throws Exception;
}
```

Providers that support SQL migrations also implement `MigrationCapable`:

```java
public interface MigrationCapable {
    void executeSqlStatements(List<String> statements) throws Exception;
}
```

## StorageRegistry

Register providers globally (typically in a bootstrap `Component`):

```java
StorageRegistry.register(new MysqlStorageProvider(dataSource));
```

Retrieve by name:

```java
StorageProvider p = StorageRegistry.get("mysql");   // name() must match
```

`initAll(plugin)` and `closeAll()` call `init`/`close` on every registered provider.

## AbstractRepository

Extend `AbstractRepository<T, ID>` to create typed repositories. The repository prefixes all
storage keys with the configured prefix to namespace records:

```java
public class PlayerRepo extends AbstractRepository<PlayerData, String> {

    public PlayerRepo(StorageProvider provider) {
        super(provider, "players");
    }

    @Override
    protected PlayerData fromMap(String id, Map<String, Object> data) {
        PlayerData p = new PlayerData(id);
        p.fromMap(data);
        return p;
    }
}
```

Available methods:

```java
Optional<PlayerData> find(String id)
Collection<PlayerData> findAll()
void save(PlayerData entity)
void delete(String id)
StorageProvider provider()
```

## Available implementations

| Artifact | Class | Notes |
| --- | --- | --- |
| `ezframework-core` | `YamlStorageProvider` | YAML flat-file storage; no migrations |
| `storage-mysql` | `MysqlStorageProvider` | MySQL/MariaDB JDBC; implements `MigrationCapable` and `JdbcStorage` |

## Notes

- Prefix all keys with a logical namespace to avoid collisions — `AbstractRepository` does this automatically.
- Always call `StorageRegistry.initAll(plugin)` in your bootstrap before using repositories.
- Call `StorageRegistry.closeAll()` in your stop component to flush and close connections.
