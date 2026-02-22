**Create a GUI — Developer Guide**

This document shows a clear, practical workflow for creating GUIs with EzFramework.

1) Design the menu
- Decide the number of rows (multiple of 9). Use `MenuDefinition.builder(rows)` to start.
- Choose an inventory title and format it with the framework messaging API: `Messaging.forPlugin(plugin).format("<gold>Title")`.

2) Add items and actions
- Add items using the `MenuDefinition` builder or helper methods in the `menu_builder` docs.
- Attach an action (lambda or method reference) for clicks. Actions receive a `GuiClickContext` which provides the `GuiPlayer`, clicked slot, click type, and convenient `close()`/`cancel()` helpers.

3) Open the menu
- Get the `GuiService` from `EzGUI.getGuiService()` and convert your platform `Player` to a `GuiPlayer` using the adapter for your platform (for Bukkit use `BukkitGuiAdapters.wrap(player)`).
- Call `guiService.openMenu(guiPlayer, menuDefinition)`.

4) Persisting item-based actions
- The GUI implementation stores action IDs inside item metadata (PDC) so items retain behavior when moved or returned to players. You do not need to manage the metadata directly; attach actions via the API and the implementation will serialize/restore metadata for you.

5) Update dynamic content
- For dynamic menus (e.g., content that changes over time or per-player), keep references to created `MenuDefinition` instances and call appropriate `GuiService` or menu API methods to refresh specific slots or re-open the menu for a player.

Example (summary):

```java
MenuDefinition menu = MenuDefinition.builder(3)
	.title(Messaging.forPlugin(plugin).format("<gold>Example"))
	.item(13, Material.APPLE, Messaging.forPlugin(plugin).format("<green>Eat"), ctx -> {
		ctx.player().sendMessage("Nom!");
	})
	.build();

GuiService guiService = EzGUI.getGuiService();
GuiPlayer gp = BukkitGuiAdapters.wrap(player);
guiService.openMenu(gp, menu);
```

See `menu_builder.md` for builder options and advanced patterns.

