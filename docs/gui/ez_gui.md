**EzGUI Integration Notes**

This page documents integration points and runtime behavior of the EzFramework GUI system.

- `GuiService` — the core runtime entrypoint. Implementations are platform-specific and discovered via the framework's service loader. Use `EzGUI.getGuiService()` to obtain the active implementation.
- `GuiPlayer` — an abstraction over a platform player. Convert platform players with provided adapters (for Bukkit: `BukkitGuiAdapters.wrap(player)`).
- `Messaging` — always format titles and item display names with `Messaging.forPlugin(plugin).format(...)` to respect the configured message provider (MiniMessage or legacy color provider).

Service discovery and lifecycle
- The GUI implementation registers via the ServiceLoader SPI. Your plugin should not instantiate the service directly — use `EzGUI.getGuiService()`.
- The GUI service handles listener registration and inventory lifecycle (open, click, close) on supported platforms.

Threading and concurrency
- GUI operations are expected to be executed on the server's main thread. If you must perform blocking work inside an action, schedule it asynchronously and then return updates to the main thread.

Compatibility notes
- The `ezframework-gui` module separates platform-specific adapters and converters (`ItemConverter`, `MetadataSerializer`) from the core `GuiService` interface so the core remains usable by alternative implementations.
