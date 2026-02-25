# storage-mysql

MySQL-backed `StorageProvider` implementation for EzFramework.

Configuration (add to your plugin `config.yml`):

- `mysql.host` (default: `localhost`)
- `mysql.port` (default: `3306`)
- `mysql.database` (required)
- `mysql.user` (required)
- `mysql.password` (optional)

Usage:

Register the provider during plugin bootstrap:

```java
bootstrap.getRegistry().register(new com.skyblockexp.ezframework.storage.provider.mysql.MysqlStorageProvider());
```

## Helpers

The module provides helpers to simplify production usage:

- `DataSourceFactory` — builds a HikariCP `DataSource` from plugin config or JDBC URL.
- `StorageClient` — typed helper with `save/load/delete/batch` operations and async wrappers; stores JSON payloads.
- `MigrationHelper` — simple idempotent migration runner that records applied migrations.

Example (basic):

```java
DataSource ds = DataSourceFactory.fromConfig(plugin);
StorageClient client = new StorageClient(ds);
client.ensureTable();
client.save("user:123", Map.of("coins", 100));
```
