# Message Provider — Implementing and Registering

EzFramework abstracts message delivery and formatting behind a `MessageProvider` interface so you can swap implementations (for example, to support MiniMessage, legacy color codes, or external localization systems).

Key concepts

- `MessageProvider` (interface): implement this to provide message lookup, formatting and send operations.
- `DefaultMessageProvider`: a built-in, minimal provider included with the framework (see the source for exact behavior).
- `Messaging` (helper): framework entry-point to acquire or register the active provider.

Implementing a provider

1. Implement the `com.skyblockexp.ezframework.message.api.MessageProvider` interface.
2. Provide message lookup (from resources or files), formatting, and a method to send or format messages for different receiver types (console, player, command sender).

Example skeleton (conceptual):

```java
public class MyMessageProvider implements MessageProvider {
    @Override
    public String format(String key, Object... args) {
        // lookup key in resource bundle or file, apply placeholders
    }

    @Override
    public void send(CommandSender target, String key, Object... args) {
        target.sendMessage(format(key, args));
    }
}
```

Registering a provider

Register your provider during bootstrap so it is available to all components when the plugin is enabled. There are two common approaches depending on your integration:

- Register via the framework `Registry` or bootstrap component: add your `MessageProvider` instance into the registry during the `bootstrap` phase.
- Use the `Messaging` helper (see `com.skyblockexp.ezframework.message.Messaging`) to set or register the active provider.

Tip: `Messaging.forPlugin(plugin)` will attempt to auto-detect and instantiate the bundled `MiniMessageProvider` (if present on the classpath) during the first call. If you include the `message-minimessage` module and Adventure dependencies, MiniMessage support will be enabled automatically unless you register a different provider explicitly.

Recommended placement: register providers early in your boot sequence so managers and command handlers can rely on them.

Usage

Once registered, consumers should obtain the current `MessageProvider` and use it to format or send localized messages. Use one of these patterns depending on your needs:

1) Simple placeholder replacement (no custom provider needed)

```java
// in your plugin code
Messaging.forPlugin(this).setPrefix("&6[MyPlugin]&r");
String template = "<green>Welcome, {player}!";
String filled = template.replace("{player}", player.getName());
Messaging.forPlugin(this).send(player, filled);
```

2) Use the bundled `message-minimessage` provider (recommended)

If you include the `message-minimessage` module on your plugin's classpath the framework will auto-detect and use it. To explicitly register and initialize the provider yourself:

```java
com.skyblockexp.ezframework.message.minimessage.MiniMessageProvider mm = new com.skyblockexp.ezframework.message.minimessage.MiniMessageProvider();
mm.init(this); // optional, calls the provider init hook with your plugin
Messaging.forPlugin(this).registerProvider(mm);

// Then usage is identical:
Messaging.forPlugin(this).send(player, "<green>Welcome, <player>!");
```

3) Full TagResolver support via a custom provider

If you need to inject complex components (hover/click events or component placeholders), implement a custom `MessageProvider` that uses Kyori Adventure's MiniMessage/TagResolver internally and register it via `Messaging.forPlugin(plugin).registerProvider(myProvider)`.

Notes on lifecycle

- `Messaging.forPlugin(plugin)` caches a per-plugin instance. Call it from your main plugin class (the one that extends `EzPlugin`/`JavaPlugin`).
- If a provider allocates resources (for example `BukkitAudiences`), ensure the provider cleans up on plugin disable. The `MessageProvider` interface has no lifecycle `close` method — implement cleanup in your provider and call it from your plugin's `onDisable` if necessary.

See also: [MiniMessage formatting & placeholders](mini_message.md) and [Color codes reference](color_codes.md).

See also: [MiniMessage formatting & placeholders](mini_message.md) and [Color codes reference](color_codes.md).

## Using MiniMessage (Adventure)

If you want full MiniMessage support (rich tags, hex colors, placeholders as components) the recommended approach is to use Kyori Adventure's MiniMessage together with the Adventure Bukkit platform. Two common integration patterns are shown below.

Add these dependencies to your plugin (example Maven coordinates — adjust versions as appropriate):

```xml
<dependency>
  <groupId>net.kyori</groupId>
  <artifactId>adventure-minimessage</artifactId>
  <version>4.12.0</version>
</dependency>
<dependency>
  <groupId>net.kyori</groupId>
  <artifactId>adventure-platform-bukkit</artifactId>
  <version>4.3.0</version>
</dependency>
```

1) Full Adventure (preferred for players)

Use `MiniMessage` to deserialize templates to `Component` and send via `BukkitAudiences` so players receive true component messages (retaining colors, hover/click events, etc.). Example (conceptual):

```java
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public class MiniMessageProvider implements MessageProvider {
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final BukkitAudiences audiences;

    public MiniMessageProvider(JavaPlugin plugin) {
        this.audiences = BukkitAudiences.create(plugin);
    }

    @Override
    public void send(CommandSender target, String key, Object... args) {
        String template = lookup(key); // implement your lookup
        TagResolver resolver = TagResolver.resolver(Placeholder.component("player", Component.text("Alice")));
        Component comp = mm.deserialize(template, resolver);
        if (target instanceof Player) {
            audiences.player((Player) target).sendMessage(comp);
        } else {
            // consoles/other send plain text fallback
            String legacy = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(comp);
            target.sendMessage(legacy);
        }
    }
}
```

2) Lightweight (no Adventure runtime)

If you don't want to include the Adventure Bukkit platform, you can still parse MiniMessage and serialize to legacy text for `CommandSender.sendMessage(String)`:

```java
Component comp = MiniMessage.miniMessage().deserialize(template, resolver);
String legacy = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(comp);
sender.sendMessage(legacy);
```

Notes

- When using placeholders, prefer `TagResolver`/`Placeholder` to inject components or string values safely.
- Register and initialize your provider during bootstrap so other managers and commands can rely on it.
- Remember to close `BukkitAudiences` on plugin shutdown to avoid resource leaks.
