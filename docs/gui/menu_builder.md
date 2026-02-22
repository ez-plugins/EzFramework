**Menu Builder — API and Patterns**

The `MenuDefinition` builder is the recommended way to construct menus. It provides clear chaining methods for rows, titles, items, and actions.

Basic usage

```java
MenuDefinition menu = MenuDefinition.builder(3)
	.title(Messaging.forPlugin(plugin).format("<gold>My Menu"))
	.item(10, Material.BOOK, Messaging.forPlugin(plugin).format("<white>Info"), ctx -> {
		ctx.player().sendMessage("You opened info");
	})
	.build();
```

Slots and placeholders
- You can fill arbitrary slot indexes. To add repeated patterns (borders, placeholders), add helper methods in your plugin to place items across ranges.

Pagination
- For lists larger than a single page, build a `MenuDefinition` per page and wire navigation actions that open the next/previous page via `GuiService.openMenu(...)`.

Dynamic content and updates
- For frequently-updated slots (e.g., cooldowns, counters), keep a reference to the `MenuDefinition` or a lightweight model and re-create only the changed slots. Re-open or refresh the inventory for the player to show updates.

Advanced: custom item conversion
- If you need custom serialization for item metadata beyond what `ItemConverter` provides, implement your own converter and ensure your `GuiService` delegates to it. For platform adapters, follow `ItemConverter` and `MetadataSerializer` patterns.

