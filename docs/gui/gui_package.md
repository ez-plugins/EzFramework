**GUI Package — Overview for Developers**

This document summarizes the public pieces of the `ezframework-gui` package and explains where responsibilities live.

Core concepts
- `MenuDefinition` — immutable description of an inventory menu (rows, title, items, actions).
- `GuiService` — runtime entrypoint for opening menus and managing GUI lifecycle.
- `GuiPlayer` — platform-agnostic player handle. Use adapters to convert platform players.

Key implementation classes (Bukkit module)
- `BukkitGuiService` — platform-specific `GuiService` for Bukkit/Paper. It delegates item conversion and metadata handling.
- `ItemConverter` — converts framework item definitions to platform `ItemStack`s and reads items back, setting and reading display names and PDC data.
- `MetadataSerializer` — canonical serializer for the GUI's metadata stored in item PDC.
- `BukkitGuiAdapters` — wraps `Player` into `GuiPlayer` and other adapter helpers.

Messaging and formatting
- The GUI module uses the project `Messaging` abstraction to format titles and item names. Call `Messaging.forPlugin(plugin).format(...)` rather than using Adventure APIs directly.

Extending or replacing the GUI implementation
- Because `GuiService` is discovered via SPI you can provide an alternate implementation for other platforms. Implement the interface and register via ServiceLoader.

Tests and examples
- The module contains unit tests that verify conversion and metadata persistence. See the `ezframework-gui` test sources for examples of using `ItemConverter` and `MetadataSerializer` in isolation.

