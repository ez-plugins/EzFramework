# Eloquent-like Models (Lightweight)

EzFramework provides a small, Eloquent-inspired convenience layer for
defining and persisting simple domain models backed by the existing
`StorageProvider` abstraction.

## Core concepts

- `Model` — base abstract class that holds an `id` and requires `toMap()` and
  `fromMap(Map)` implementations.
- `ModelFactory<T>` — functional factory used to instantiate a model from
  persisted data.
- `ModelRepository<T>` — generic repository that wraps a `StorageProvider`
  and exposes common operations: `save`, `find`, `delete`, `exists`.

## Quick example

Define a model:

```java
public class PlayerData extends EzModel {
    public PlayerData(String id) { super(id); }

    // allow mass-assignment only for 'coins' and 'name'
    public PlayerData() { super(null); setFillable("coins", "name"); }

    public int getCoins() { return getAs("coins", Integer.class, 0); }
    public void setCoins(int coins) { set("coins", coins); }
}
```

### Saving and querying examples

```java
StorageProvider provider = ...; // from framework or plugin
ModelRepository<PlayerData> repo = new ModelRepository<>(provider, "players", (id, data) -> new PlayerData(id));

PlayerData p = new PlayerData("uuid-1234");
p.setCoins(42);
repo.save(p);

// find by id
Optional<PlayerData> loaded = repo.find("uuid-1234");

// query
com.skyblockexp.ezframework.query.Query q = PlayerData.queryBuilder()
    .whereEquals("coins", 42)
    .limit(10)
    .build();
java.util.List<PlayerData> matches = repo.query(q);
```

## Notes

- This is intentionally lightweight: models are responsible for their own
  (de)serialization to a `Map`. This avoids reflection magic and keeps the
  API explicit and testable.
- The `ModelRepository` can be reused with different `StorageProvider`
  implementations (YAML, MySQL, etc.).
