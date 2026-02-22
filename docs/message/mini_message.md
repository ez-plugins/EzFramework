# MiniMessage-style Formatting & Placeholders

EzFramework is compatible with markup-style message formatting (commonly known as MiniMessage) and placeholder substitution. Use structured markup to express colors, styles and simple placeholders while keeping message source files readable.

Note: prefer using the bundled provider `com.skyblockexp.ezframework.message.minimessage.MiniMessageProvider` (in the `message-minimessage` module) rather than invoking `MiniMessage` directly. Register and initialize the provider with `Messaging.forPlugin(plugin)` so the framework handles integration with `BukkitAudiences`.

Common features

- Tags for color and style: `<green>`, `<red>`, `<bold>`, etc.
- Hex color support: `<#rrggbb>colored text</#rrggbb>` or inline like `<#ff8800>Text` depending on your formatter.
- Placeholders: tokens such as `{player}` or `{amount}` that are replaced at runtime.

Example message template (resource file):

```text
welcome.message = <green>Welcome, <player>! <gray>You have <yellow>{coins}</yellow> coins.
```

Usage (conceptual):

```java
// Simple replacement before sending (works with the default provider):
String template = "<green>Welcome, {player}! <gray>You have <yellow>{coins}</yellow> coins.";
String formatted = template.replace("{player}", player.getName()).replace("{coins}", "42");
messageProvider.send(player, formatted);
```

Notes

- The exact tag syntax and placeholder replacement behavior depends on the `MessageProvider` implementation you use. For richer MiniMessage features (components, hover/click events, hex colors) prefer the bundled `MiniMessageProvider` or implement a provider that uses Kyori Adventure `MiniMessage` with `TagResolver` support.
- If you need true component messages (preserving events and advanced styling), include `adventure-platform-bukkit` and use a provider that sends via `BukkitAudiences`.
- Prefer structured keys (dot-separated) for message entries to simplify localization.
- Keep formatting and placeholders separate from runtime logic: message templates should only include placeholders, not business logic.
