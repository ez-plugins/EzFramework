# MySQL Storage Provider

This document describes how to configure and use the `MysqlStorageProvider`
from EzFramework. The provider supports two modes of configuration:

- Configuration via your plugin `config.yml` (default)
- Programmatic / hardcoded configuration (call setters before `init`)

## Configuration (config.yml)

Add the following entries to your plugin config to use the provider with
configuration values:

```yaml
mysql:
  host: localhost
  port: 3306
  database: my_database
  user: my_user
  password: secret
```

## Programmatic configuration

If you prefer to hardcode settings or construct the provider at runtime,
you can configure it without touching `config.yml`. Example usage in your
plugin's `onEnable()`:

```java
// fluent setters
MysqlStorageProvider provider = new MysqlStorageProvider()
    .setHost("127.0.0.1")
    .setPort(3306)
    .setDatabase("my_database")
    .setUser("my_user")
    .setPassword("secret");

provider.init(this); // `this` is your JavaPlugin instance
```

You can also supply an existing `java.sql.Connection` (for example an H2
in-memory connection used in tests) and the provider will reuse it:

```java
Connection conn = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
MysqlStorageProvider provider = new MysqlStorageProvider(conn);
provider.init(this);
```

## Notes

- Programmatic setters take precedence over values from the plugin
  configuration file.
- If neither config nor setters provide `mysql.database` and `mysql.user`,
  `init` will throw an exception.
- The provider will automatically create a table named `ezframework_storage`
  (or the name you configure via `setTable(...)`) if it does not exist.

## Advanced

- Use `setTable(String)` to change the storage table name.
- For transactional DDL/DDL sequences use `executeSqlStatements(List<String>)`.

## Helpers and production recommendations

- Prefer using a pooled `DataSource` in production. Use `DataSourceFactory.fromConfig(plugin)` to create a HikariCP `DataSource` from your plugin config.
- Use `StorageClient` for typed operations and JSON storage rather than raw Java serialization; it offers `save/load/delete/batch` and async wrappers to avoid blocking the main thread.
- Use `MigrationHelper` to apply idempotent migrations (DDL/seed) and track applied migrations.

### Example using helpers

```java
DataSource ds = DataSourceFactory.fromConfig(this);
StorageClient client = new StorageClient(ds);
client.ensureTable();
client.save("user:123", Map.of("coins", 42));

MigrationHelper mh = new MigrationHelper(ds);
mh.applyMigrations(List.of(new MigrationHelper.Migration("001-add-index", List.of("CREATE INDEX IF NOT EXISTS idx_path ON ezframework_storage(path)"))));
```

## Security

Avoid embedding credentials in source control; prefer environment variables
or secure configuration management when deploying to production.
