# GUI — Developer Guide

EzFramework provides a platform-agnostic GUI layer with a clean separation between the
API (in `ezframework-api`) and the Bukkit implementation (in `ezframework-gui`).

## Architecture

```text
ezframework-api          ezframework-gui
  MenuDefinition           BukkitGuiService (implements GuiService)
  MenuBuilder              BukkitGuiAdapters.wrap(Player) → GuiPlayer
  GuiItem
  GuiAction
  GuiClickContext
  GuiPlayer (interface)
  GuiService (interface)
  EzGUI (per-plugin accessor)
```

## Quick Start

### 1. Define a menu

Use `MenuBuilder` for fluent construction:

```java
MenuDefinition menu = MenuBuilder.create()
    .title("<gold>My Menu")
    .size(27)                                          // inventory size in slots
    .item(13, new GuiItem("DIAMOND", 1, "<aqua>Click me!", null))
    .action(13, GuiAction.of(ctx -> {
        ctx.player().sendMessage("You clicked the diamond!");
    }))
    .build();
```

Or construct `MenuDefinition` directly:

```java
Map<Integer, GuiItem> items = new HashMap<>();
items.put(0, new GuiItem("BARRIER", 1, "<red>Close", null));

Map<Integer, GuiAction> actions = new HashMap<>();
actions.put(0, GuiAction.of(ctx -> EzGUI.forPlugin(plugin).getProvider().closeMenu(ctx.player())));

MenuDefinition menu = new MenuDefinition("<gray>Options", 9, items, actions);
```

### 2. Open the menu

```java
EzGUI gui = EzGUI.forPlugin(plugin);
gui.openMenu(guiPlayer, menu);
```

The `GuiService` implementation is discovered via `ServiceLoader` at runtime. The Bukkit
implementation in `ezframework-gui` registers itself automatically.

## Key types

### `GuiItem`

Represents an item slot snapshot (no platform types):

```java
new GuiItem(
    "DIAMOND_SWORD",    // material name
    1,                  // amount (clamped to ≥ 1)
    "My Sword",         // display name (null → "")
    Map.of("key", "val") // metadata (null → empty; returned as unmodifiable view)
)
```

### `GuiAction`

A click handler for a slot:

```java
GuiAction action = GuiAction.of(ctx -> {
    GuiPlayer player = ctx.player();
    int slot = ctx.slot();
    GuiItem item = ctx.item();      // may be null
    player.sendMessage("Clicked slot " + slot);
});

GuiAction noop = GuiAction.noop(); // silently ignores clicks
```

### `GuiClickContext`

Passed to every `GuiAction`:

```java
ctx.player()   // GuiPlayer
ctx.slot()     // clicked slot index
ctx.item()     // GuiItem in that slot, or null
```

### `GuiPlayer`

Platform-agnostic player interface. Implement or obtain via the platform adapter:

```java
// Bukkit
GuiPlayer p = BukkitGuiAdapters.wrap(bukkitPlayer);

// Interface methods available everywhere
UUID   id  = player.getUniqueId();
String name = player.getName();
player.sendMessage("Hello");
```

### `EzGUI`

Per-plugin accessor:

```java
EzGUI gui = EzGUI.forPlugin(plugin);
gui.openMenu(player, menu);
gui.getProvider();                    // current GuiService
gui.registerProvider(customService);  // override the discovered provider
```

## Notes

- Always use string material names (`"DIAMOND"`, `"STONE"`) rather than platform enums in
  `GuiItem` so menus defined in API code stay platform-free.
- Format titles with `Messaging.forPlugin(plugin).format(...)` (MiniMessage) to stay consistent
  with the configured message provider.
- The built-in Bukkit implementation automatically persists action IDs in item PDC so slots
  retain their handlers across inventory events.

## Further Reading

- [MenuBuilder](menu_builder.md)
- [GUI actions](gui_action.md)
- [EzGUI integration](ez_gui.md)
- [GUI package overview](gui_package.md)
- [Create a GUI — step-by-step](create_a_gui.md)
