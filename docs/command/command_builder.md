# CommandBuilder — Convenience Builder for EzCmd

>This page documents the `CommandBuilder` helper. It is a convenience API for small/example commands — not the primary recommended approach. For production use prefer creating a dedicated class that extends `EzCmd`.

## When to use

- Quick prototypes and examples.
- Unit tests where creating a full class is unnecessary.

## Example

```java
// 'plugin' is your JavaPlugin instance (e.g. 'this' in your main class)
EzCmd cmd = EzCmd.builder(plugin, "hello")
  .description("Sends a greeting")
  .permission("myplugin.command.hello")
  .executor((sender, args) -> {
    sender.sendMessage("Hello, world!");
    return true; // indicate handled
  })
  .completer((sender, args) -> {
    return Collections.emptyList();
  })
  .build();

// If your project uses the framework Registry, register during bootstrap:
// bootstrap.getRegistry().registerCommand(cmd);
```

## Notes

- The builder creates an `EzCmd` instance that will attempt to register itself with Bukkit when constructed (via `plugin.getCommand(...)`).
- The builder's `executor` should return `true` when the command is handled.
- For complex command trees or extensive subcommand logic, implement `Subcommand` classes and register them on an `EzCmd` subclass instead.
