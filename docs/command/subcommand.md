# Subcommand — Building Command Trees

For commands with multiple actions, `Subcommand` provides a structured way to group handlers under a parent command. This improves organization and helps with argument routing, permissions, and usage printing.

## Concept

- A parent `EzCmd` delegates to registered `Subcommand` implementations.
- Each `Subcommand` handles its own validation, permission checks, and execution.

## Example Structure

Parent command: `/shop`

Subcommands:

- `list` — lists items
- `buy <id>` — purchase an item
- `sell <id>` — sell an item

## Implementing a Subcommand

```java
public class BuySubcommand implements Subcommand {
    @Override
    public String name() { return "buy"; }

    @Override
    public String permission() { return "myplugin.shop.buy"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /shop buy <id>");
            return;
        }
        int id = Integer.parseInt(args[0]);
        // perform purchase logic
    }
}
```

## Registering Subcommands

Attach subcommands to the parent command during bootstrap:

```java
EzCmd shop = EzCmd.builder("shop")
    .description("Shop operations")
    .build();

shop.registerSubcommand(new ListSubcommand());
shop.registerSubcommand(new BuySubcommand());
shop.registerSubcommand(new SellSubcommand());

bootstrap.getRegistry().registerCommand(shop);
```

The parent `EzCmd` will route arguments: `/shop buy 3` → `BuySubcommand.execute(sender, ["3"])`.

## Help & Usage

Implement a default `help` subcommand or detect unknown subcommands and print a usage summary listing available subcommands and their brief descriptions.

## Best Practices

- Keep subcommands small and focused.
- Reuse validation utilities across subcommands.
- Use the Messaging API for consistent responses and localization.
