# Storage Migrations

This document describes the migration support added to EzFramework. The system is pluggable and supports SQL files and Java migration classes. Migrations are discovered per-plugin and are applied automatically during plugin startup by the `MigrationBootstrap` component.
## Conventions
- Place SQL migration files or a `migrations/index.txt` resource inside your plugin JAR under `migrations/`.
- Each line in `migrations/index.txt` should reference a file (e.g. `001_create_users_table.sql` or `002_update_settings.java`).
- Alternatively, add Java migration classes in a package `com.example.plugin.migrations` and register them via service loading (future enhancement).

## Providers
- Only providers that implement the `MigrationCapable` interface will receive SQL migrations. The `MysqlStorageProvider` includes a basic `executeSql(String sql)` method and will run SQL migrations applied to it.

## Tracking
- Applied migrations are recorded in the plugin data folder in `applied_migrations.txt`. Each line is `id|timestamp|checksum`.

## Registering the bootstrap
- To enable automatic migrations for your plugin, register `MigrationBootstrap` in your plugin's `components()` list. Example:

```java
	return Arrays.asList(
		new com.skyblockexp.ezframework.bootstrap.component.RegistryBootstrap(),
		new com.skyblockexp.ezframework.bootstrap.component.MigrationBootstrap(this),
		new com.skyblockexp.ezframework.bootstrap.component.ManagerInitComponent(this)
	);
}
## CLI
- The `ez` CLI supports scaffolding migration files via `make:migration` (stub generator added).

## Java migrations and service registration

To register Java-based migrations you can provide classes that implement `com.skyblockexp.ezframework.storage.migration.Migration` and register them via the standard service loader mechanism.

1. Add a file `META-INF/services/com.skyblockexp.ezframework.storage.migration.Migration` to your plugin resources.
2. Each non-comment line in that file must contain the fully-qualified class name of a migration implementation.

Example `META-INF/services/com.skyblockexp.ezframework.storage.migration.Migration`:

com.example.plugin.migrations.AddUsersTable

You can scaffold a Java migration class using the CLI with a `make:migration-java` command (stub generator added). The generated class should implement `apply()` and optionally `rollback()`.

## Example SQL migration file (`migrations/001_create_users_table.sql`)
CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(255));

(# Storage Migrations

This document describes the new migration support added to EzFramework. The system is pluggable and supports SQL files and Java migration classes. Migrations are discovered per-plugin and are applied automatically during plugin startup by the `MigrationBootstrap` component.

## Conventions
- Place SQL migration files or a `migrations/index.txt` resource inside your plugin JAR under `migrations/`.
- Each line in `migrations/index.txt` should reference a file (e.g. `001_create_users_table.sql` or `002_update_settings.java`).
- Alternatively, add Java migration classes in a package `com.example.plugin.migrations` and register them via service loading (future enhancement).

## Providers
- Only providers that implement the `MigrationCapable` interface will receive SQL migrations. The `MysqlStorageProvider` includes a basic `executeSql(String sql)` method and will run SQL migrations applied to it.

## Tracking
- Applied migrations are currently discovered and logged. A manifest-based tracking mechanism (e.g. `applied_migrations.json` in plugin data folder) is planned and will be added in follow-up updates.

## CLI
- The `ez` CLI supports scaffolding migration files via `make:migration` (stub generator added).

## Example SQL migration file (`migrations/001_create_users_table.sql`):

CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(255));

