**GUI Actions & Click Context**

Actions are the handlers attached to slots in a `MenuDefinition`. They are the primary way your plugin reacts to player clicks inside a GUI.

Action signature
- Actions receive a `GuiClickContext` instance. Typical usage:

```java
menu.item(0, Material.PAPER, "Do thing", ctx -> {
  GuiPlayer player = ctx.player();
  int slot = ctx.slot();
  ctx.player().sendMessage("Clicked slot " + slot);
});
```

What `GuiClickContext` provides
- `player()` — the `GuiPlayer` who clicked.
- `slot()` — the clicked slot index.
- `clickType()` — type of click (left/right/middle).
- `cancel()` — prevent default behavior (if supported by platform).
- `close()` — close the menu for the player.

Action IDs and metadata
- When attaching an action, the GUI system assigns an internal action ID and stores it inside the item metadata (PDC). This allows action persistence across inventory copies and serialization.
- You can rely on the action ID to survive inventory moves; do not attempt to manage PDC keys directly unless implementing cross-platform adapters or custom converters.

Best practices
- Keep actions concise and non-blocking. For long-running work, schedule asynchronously and send results back to the player on the main thread.
- Avoid capturing large outer-scope state in lambdas — prefer referencing service objects or small immutable values.
