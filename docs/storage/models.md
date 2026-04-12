# Models

EzFramework provides a lightweight Eloquent-inspired model layer through `EzModel` and
`ModelRepository` (both in `ezframework-api`).

## EzModel

`EzModel` is an abstract base class backed by a `Map<String, Object>`. Subclasses define the
domain shape; the framework handles persistence via a `ModelRepository`.

### Defining a model

```java
public class PlayerData extends EzModel {

    public PlayerData(String id) { super(id); }

    // Allow mass-assignment of specific fields
    public PlayerData() {
        super(null);
        setFillable("coins", "name");
    }

    public int getCoins() { return getAs("coins", Integer.class, 0); }
    public void setCoins(int v) { set("coins", v); }

    public String getName() { return getAs("name", String.class, ""); }
    public void setName(String v) { set("name", v); }
}
```

### Key API

```java
// Read / write attributes
model.set("coins", 42);
Object raw = model.get("coins");

// Type-coerced read
int coins  = model.getAs("coins", Integer.class);         // null if missing
int coins  = model.getAs("coins", Integer.class, 0);      // 0 if missing

// Mass assignment (respects fillable / guarded)
model.setFillable("coins", "name");       // only these keys are mass-assignable
model.setGuarded("secret");               // this key is blocked from mass-assign
model.fill(Map.of("coins", 100, "secret", "s"));  // "secret" is NOT written

// Serialisation
Map<String, Object> map = model.toMap();  // excludes "id"
model.fromMap(map);                       // replaces attributes; sets id if present

// Unmodifiable view of current attributes (excludes "id")
Map<String, Object> attrs = model.attributes();

// Query builder (for use with ModelRepository)
Query q = EzModel.queryBuilder().whereEquals("coins", 100).build();
```

`"id"` is always guarded — `fill()` cannot change it.

## ModelRepository

`ModelRepository<T>` wraps a `StorageProvider` and a `ModelFactory<T>` to provide typed CRUD
operations:

```java
ModelRepository<PlayerData> repo = new ModelRepository<>(
    provider,
    "players",              // prefix; also used as ModelTableRegistry key
    PlayerData::new         // ModelFactory: (id, data) -> T
);

// CRUD
repo.save(player);
Optional<PlayerData> p = repo.find("uuid-1234");
repo.delete("uuid-1234");

// Static helper on EzModel
PlayerData p = EzModel.find(repo, "uuid-1234");  // returns null if not found
```

## ModelTableRegistry

Register table metadata once (typically in a migration) so that `ModelRepository` knows which
SQL table and columns to use when the provider implements `JdbcStorage`:

```java
ModelTableRegistry.register(
    "players",                     // prefix — must match the repository prefix
    "players",                     // SQL table name
    Map.of("name", "VARCHAR(191)", "coins", "INT")
);

ModelTableRegistry.TableMeta meta = ModelTableRegistry.get("players");
meta.tableName();   // "players"
meta.columns();     // unmodifiable map
```

## Notes

- `EzModel` stores only attributes; the `id` field is separate and always excluded from `toMap()`.
- Keep models lightweight: no reflection, no annotations — serialize/deserialize explicitly.
- Pair with the `Schema` helper (see [schema.md](schema.md)) to create tables and register
  `ModelTableRegistry` entries in a single migration.
