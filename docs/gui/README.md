**GUI — Developer Guide**

This guide explains how to create GUIs for your plugin using EzFramework's GUI package.

**Quick Start**
- **Add dependencies**: include `ezframework-core` and `ezframework-gui` in your plugin build.
- **Obtain a GuiService**: the framework exposes a `GuiService` implementation (the platform-specific implementation is discovered at runtime).
- **Messaging**: use `Messaging.forPlugin(plugin).format(...)` for MiniMessage formatting and legacy color handling when building titles or item display names.

**Create a Menu**
1. Build a `MenuDefinition` (or use the `MenuDefinition`/builder helpers in the `menu_builder` docs).
2. Add items and associate actions that accept a `GuiClickContext` (the click context includes the `GuiPlayer` and click details).
3. Open the menu via the `GuiService` for a `GuiPlayer`.

Example (conceptual):

```java
// Build a menu
MenuDefinition menu = MenuDefinition.builder(3)
	.title(Messaging.forPlugin(plugin).format("<gold>Example Menu"))
	.item(0, Material.DIAMOND, Messaging.forPlugin(plugin).format("<white>Click me"),
		ctx -> {
			ctx.player().sendMessage("You clicked!");
			ctx.close(); // close the menu if desired
		})
	.build();

// Open the menu for a player (Bukkit example)
GuiService guiService = EzGUI.getGuiService();
GuiPlayer guiPlayer = BukkitGuiAdapters.wrap(player);
guiService.openMenu(guiPlayer, menu);
```

**Actions & Persistent Metadata**
- Actions attached to slots are persisted inside item metadata (PDC) by the GUI implementation so items keep their action IDs when inventories are moved, serialized, or returned to players. Use the `ItemAction`/lambda style handlers with the `GuiClickContext` to implement behavior.

**Titles & Item Names**
- Always use `Messaging.forPlugin(plugin).format(...)` to format inventory titles and item display names so your plugin stays compatible with the configured message provider (MiniMessage/legacy color codes).

**Platform Notes**
- In Bukkit/Paper environments use `BukkitGuiAdapters.wrap(player)` to convert a platform `Player` into a `GuiPlayer` for use with the `GuiService`.
- The GUI module separates conversion and metadata concerns into `ItemConverter` and `MetadataSerializer`; you generally do not need to access item PDC directly.

**Further Reading**
- [Create a GUI](docs/gui/create_a_gui.md)
- [Menu Builder](docs/gui/menu_builder.md)
- [GUI Actions](docs/gui/gui_action.md)
- [GUI Package Overview](docs/gui/gui_package.md)
- [EzGUI Integration Notes](docs/gui/ez_gui.md)

- [Bukkit Example](docs/gui/bukkit_example.md)

If you want, I can also add step-by-step examples for Bukkit and a minimal runnable example plugin. Want me to add that?

