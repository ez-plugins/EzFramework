# EzCmd — Command Reference

This page describes the `EzCmd` utility included in EzFramework for registering and handling commands in a consistent, testable way.

## Purpose

`EzCmd` provides a lightweight abstraction for defining command handlers, argument parsing, permission checks, and tab-completion. It promotes a tree-style command structure and integrates with the framework's lifecycle so commands are registered during bootstrap.

## Creating a Command

A typical `EzCmd` implementation is a class that encapsulates the command name, description, permission, and an execution handler.

Example — preferred production approach

Create a dedicated command class that extends `EzCmd`. This keeps command logic organized, testable, and easy to extend with `Subcommand` implementations.

```java
public class HelloCommand extends EzCmd {
  /**
   * The third constructor argument for `EzCmd` is a `List<Subcommand>` — an
   * initial set of subcommands that the parent command will route to. The
   * framework makes an internal copy of the list and exposes it via
   * `getSubcommands()` as an unmodifiable view.
   *
   * You typically create concrete `Subcommand` implementations and pass them
   * into `super(...)` so they are available immediately when the command is
   * registered.
   */
  public HelloCommand(JavaPlugin plugin) {
    super(plugin, "hello", Arrays.asList(
      new Subcommand("greet", null, false) {
        @Override
        public boolean execute(CommandSender sender, String[] args) throws Exception {
          sender.sendMessage("Hello, " + (args.length > 0 ? args[0] : "world") );
          return true;
        }
      }
    ));
  }

  @Override
  public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
    // top-level fallback or delegation is handled by EzCmd; override
    // only when you need special top-level behavior.
    return super.onCommand(sender, command, label, args);
  }
}

// Register from your main plugin class that extends EzPlugin:
public class MyPlugin extends EzPlugin {
  @Override
  public void onEnable() {
    new HelloCommand(this); // EzCmd constructor wires executor/tab-completer
  }
}

See the Subcommand reference for examples of full subcommand implementations: [subcommand.md](subcommand.md).
```

Notes:

- Prefer encapsulating command behavior in an `EzCmd` subclass for production code.
- Use `Subcommand` implementations to structure multi-action commands.
- Use permission checks (`sender.hasPermission(...)`) to gate sensitive operations.

## Arguments & Validation

- Validate argument count and types early and provide helpful feedback to the sender.
- Consider utility methods to parse integers, UUIDs, or player references and surface friendly error messages.

Example argument check:

```java
executor((sender, args) -> {
  if (args.length < 1) {
    sender.sendMessage("Usage: /hello <name>");
    return;
  }
  String name = args[0];
  sender.sendMessage("Hello, " + name);
});
```

## Tab Completion

Provide a completion handler where available to improve UX. Return an empty list when nothing matches.

```java
.completer((sender, args) -> {
  if (args.length == 1) return playerNamesStartingWith(args[0]);
  return Collections.emptyList();
})
```

## Registration

Register commands during bootstrap so they are available when the plugin is enabled. Use the framework's `Registry` or bootstrap API to register command objects.

## Common Patterns

- Wrap permission checks in helper methods.
- Provide centralized error/usage formatting via the Messaging API.
- Use subcommands for complex command trees (see `Subcommand` docs).

## Troubleshooting

- If commands are not visible, ensure they are registered during bootstrap and the plugin enabled successfully.
- Confirm permission nodes are declared in your plugin manifest if your platform requires it.

For subcommand patterns and examples, see `docs/command/subcommand.md`.

Note: For completeness the repository includes a `CommandBuilder` convenience helper for quick examples and tests — see `docs/command/command_builder.md`. Production code should prefer an `EzCmd` subclass registered from your main plugin class (the one that `extends EzPlugin`).
