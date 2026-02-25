# EzConfig (ezframework-config)

## Summary

- `EzConfig` is a small, framework-neutral API for plugin configuration files.
- The repository provides a Bukkit-backed adapter `YamlEzConfig` (packaged in the `ezframework-config-bukkit` module) and a `ConfigMigrationManager` to simplify default-copy, reload, save and migration workflows.

## Key concepts

- `EzConfig` — interface for loading, reloading, saving and locating a config file.
- `YamlEzConfig` — Bukkit `YamlConfiguration` adapter (located in `ezframework-config-bukkit`) that copies defaults (`saveDefault()`), loads and saves the file, and exposes the wrapped `FileConfiguration`.
- `ConfigMigration` — implement this to perform idempotent migrations for a config file.
- `ConfigMigrationManager` — discovers and runs migrations; records applied migrations in `applied_config_migrations.txt` inside the plugin data folder.
- `ConfigBootstrap` — bootstrap component (located in `ezframework-config-bukkit`) that ensures a config is created/loaded early and applies discovered migrations.
- `EzConfigProvider` — SPI provider interface that plugins can implement to provide one or more `EzConfig` instances to the framework (see Auto-discovery below).
- `ConfigManager` — framework component that discovers `EzConfigProvider` implementations via Java SPI, instantiates configs, calls `saveDefault()/load()` and runs migrations for each discovered config.

## Migration discovery

- Resource index: include a file at `config_migrations/index.txt` in your plugin JAR. Each non-empty, non-`#` line should contain a fully-qualified migration class name (a class implementing `com.skyblockexp.ezframework.config.ConfigMigration`). Example:

  com.example.migrations.RenameKeyV1
  com.example.migrations.SplitSectionV2

- Service loader: alternatively or additionally, register migration classes with the standard service loader by placing a resource at `META-INF/services/com.skyblockexp.ezframework.config.ConfigMigration` listing implementation class names (one per line).

## Applied manifest

- When a migration runs successfully its id (class-defined) is appended to `applied_config_migrations.txt` in the plugin data folder. This prevents re-applying migrations.

## Bootstrap integration

- `ConfigBootstrap` is optionally auto-registered by `EzPlugin` if the `ezframework-config` module is present. It loads `config.yml` by default and applies migrations discovered via the index and service loader.
- To register manually, return a `ConfigBootstrap` in your plugin components list (order matters):

  Add to your `components()`:

  ```java
  import com.skyblockexp.ezframework.config.ConfigBootstrap;
  import com.skyblockexp.ezframework.bootstrap.Component;

  protected List<Component> components() {
      return List.of(new ConfigBootstrap(this, "my-config.yml"), /* other components */);
  }
  ```

## YamlEzConfig usage

- Basic example:

  ```java
  YamlEzConfig cfg = new YamlEzConfig(plugin, "config.yml");
  cfg.saveDefault(); // copy resource config.yml -> plugin folder if missing
  cfg.load();        // load into memory
  FileConfiguration fc = cfg.getConfig();
  String value = fc.getString("some.path", "default");
  // modify and save
  fc.set("some.path", 123);
  cfg.save();
  // reload from disk
  cfg.reload();
  ```

## Notes & recommendations

- Keep migrations small and idempotent.
- Prefer service-loader registration for Java-based migrations if you ship multiple migration classes.
- The config migration manifest is intentionally simple (text file) to keep behavior consistent with existing storage migrations in the framework.

## Auto-discovery

The framework now supports auto-discovery of plugin-provided configs via Java SPI. This makes shipping custom plugin configs as easy as adding an `EzConfigProvider` implementation and registering it in `META-INF/services`.

1. Implement `com.skyblockexp.ezframework.config.EzConfigProvider` in your plugin. Example that returns a YAML-backed config:

```java
public class MyConfigProvider implements EzConfigProvider {
  @Override
  public EzConfig create(com.skyblockexp.ezframework.EzPlugin plugin) {
    return new com.skyblockexp.ezframework.config.impl.YamlEzConfig(plugin, "my-config.yml");
  }
}
```

2. Add an SPI resource in your JAR at `META-INF/services/com.skyblockexp.ezframework.config.EzConfigProvider` containing the FQCN of your provider implementation, e.g.:

```text
com.example.plugin.MyConfigProvider
```

3. When your plugin starts, the framework's `ConfigManager` (auto-registered by `EzPlugin` if present) will load providers using the plugin classloader, instantiate each `EzConfig`, call `saveDefault()` and `load()`, and then run migrations discovered via the existing `config_migrations/index.txt` or `META-INF/services` migration registrations.

## Programmatic registration

If you prefer runtime registration, `ConfigManager.register(EzConfig)` is available for manual registration (for example, to create configs dynamically). You can obtain the `ConfigManager` instance by registering it as a component or by using `EzPlugin`'s bootstrap component list.

## Where to look in the codebase

- `ezframework-config` module: API, `EzConfig`, `ConfigManager`, and migration support (platform-neutral)
- `ezframework-config-bukkit` module: Bukkit-specific implementations such as `YamlEzConfig` and `ConfigBootstrap`
- `ezframework-core`: `EzPlugin` and `Bootstrap` integration

## Building the Bukkit adapters

- The `ezframework-config-bukkit` module depends on the Paper/Spigot API and snapshot repositories to compile.
- Building the full multi-module project requires network access to the Paper/Spigot repositories (for example, `https://repo.papermc.io` and `https://hub.spigotmc.org`).
- If you need to build without network access, consider skipping the `ezframework-config-bukkit` module or using a separate CI job that has repository access.
