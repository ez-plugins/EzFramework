# Schema & Migrations

EzFramework includes a lightweight migration system for SQL databases and a `Schema` helper for
generating DDL in a provider-agnostic way.

## Schema helper

`Schema` (in `ezframework-api`) builds a `CREATE TABLE` statement using a fluent builder. It
supports both MySQL and H2 dialects:

```java
String sql = Schema.table("players")
    .id()                            // VARCHAR(255) PRIMARY KEY
    .string("name", 191, true)       // VARCHAR(191) NOT NULL
    .integer("coins", false)         // INT (nullable)
    .text("bio", false)              // TEXT (nullable)
    .bool("active", true)            // TINYINT(1) NOT NULL
    .toCreateSql();                  // MySQL dialect (default)

String h2Sql = Schema.table("players")
    ...
    .toCreateSql(Schema.Dialect.H2); // H2 dialect
```

Available column methods:

| Method | SQL type (MySQL) |
| --- | --- |
| `id()` | `VARCHAR(255) NOT NULL` + `PRIMARY KEY` |
| `string(name, length, notNull)` | `VARCHAR(length)` |
| `integer(name, notNull)` | `INT` |
| `text(name, notNull)` | `TEXT` |
| `binary(name, notNull)` | `BLOB` |
| `bool(name, notNull)` | `TINYINT(1)` |

## Migration types

| `MigrationType` | Description |
| --- | --- |
| `SQL` | Raw SQL file resource in the plugin JAR |
| `JAVA` | Java class implementing `Migration` |

## Writing a migration

Implement `Migration`:

```java
public class CreatePlayersTable implements Migration {
    @Override public String id()          { return "2026_01_01_create_players"; }
    @Override public String description() { return "Create players table"; }
    @Override public MigrationType type() { return MigrationType.JAVA; }

    @Override
    public void apply(MigrationContext ctx) throws Exception {
        if (!(ctx.provider() instanceof MigrationCapable mc)) {
            throw new IllegalStateException("Provider does not support migrations");
        }

        String sql = Schema.table("players")
            .id()
            .string("name", 191, true)
            .integer("coins", false)
            .toCreateSql();

        mc.executeSqlStatements(SqlSplitter.split(sql));

        // Register table metadata for ModelRepository / JdbcStorage
        ModelTableRegistry.register("players", "players",
            Map.of("id", "VARCHAR(255)", "name", "VARCHAR(191)", "coins", "INT"));
    }
}
```

## Registering migrations

Register via the Java `ServiceLoader` mechanism:

1. Create a file `META-INF/services/com.skyblockexp.ezframework.storage.migration.Migration`
   in your plugin's resources.
2. Each non-comment line should be the fully-qualified class name of a migration:

```text
com.example.myplugin.migrations.CreatePlayersTable
com.example.myplugin.migrations.AddActiveColumn
```

## MigrationBootstrap

Add `MigrationBootstrap` to your `components()` list to apply pending migrations at startup:

```java
return List.of(
    new MigrationBootstrap(this),
    new ManagerInitComponent(this)
);
```

Applied migrations are recorded in `<dataFolder>/applied_migrations.txt` and skipped on
subsequent starts.

## MigrationContext

`MigrationContext` is passed to `apply()` and gives access to:

```java
ctx.plugin()             // host plugin instance
ctx.provider()           // StorageProvider
ctx.migrationCapable()   // Optional<MigrationCapable> — present if provider supports SQL
ctx.jdbc()               // Optional<JdbcStorage>       — present if provider is JdbcStorage
ctx.executeSqlStatements(List<String>) // delegate to MigrationCapable (throws if not capable)
```

## SqlSplitter

`SqlSplitter.split(sql)` parses a multi-statement SQL string and returns a list of individual
statements, stripping line comments (`--`), block comments (`/* */`), and respecting quoted
strings.

```java
List<String> stmts = SqlSplitter.split(rawSql);
```
