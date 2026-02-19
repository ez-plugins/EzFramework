package com.skyblockexp.ezframework.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Lightweight command helper that delegates to a list of {@link Subcommand}s.
 * Register by constructing with your `JavaPlugin` and command name (the
 * command must be defined in the host plugin's `plugin.yml`).
 */
public abstract class EzCmd implements org.bukkit.command.CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final String commandName;
    private final List<Subcommand> subcommands;

    protected EzCmd(JavaPlugin plugin, String commandName, List<Subcommand> subcommands) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.commandName = Objects.requireNonNull(commandName, "commandName");
        this.subcommands = (subcommands == null) ? new ArrayList<>() : new ArrayList<>(subcommands);

        try {
            if (plugin.getCommand(commandName) != null) {
                plugin.getCommand(commandName).setExecutor(this);
                plugin.getCommand(commandName).setTabCompleter(this);
            } else {
                plugin.getLogger().warning("Command '" + commandName + "' not defined in plugin.yml");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register command '" + commandName + "': " + e.getMessage());
        }
    }

    protected List<Subcommand> getSubcommands() {
        return Collections.unmodifiableList(subcommands);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args == null || args.length == 0) {
                for (Subcommand s : subcommands) if (s.isDefault()) return s.execute(sender, new String[0]);
                sender.sendMessage("Usage: " + usage());
                return true;
            }

            String token = args[0];
            for (Subcommand s : subcommands) {
                if (!s.matches(token)) continue;
                if (s.getPermission() != null && !s.getPermission().isEmpty() && !sender.hasPermission(s.getPermission())) {
                    sender.sendMessage("You don't have permission to use this command.");
                    return true;
                }
                String[] rest = Arrays.copyOfRange(args, 1, args.length);
                return s.execute(sender, rest);
            }

            sender.sendMessage("Unknown subcommand. " + usage());
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing command '" + commandName + "': " + e.getMessage());
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        try {
            if (args == null) return Collections.emptyList();
            if (args.length == 1) {
                String partial = args[0].toLowerCase();
                return subcommands.stream()
                        .filter(s -> s.getPermission() == null || s.getPermission().isEmpty() || sender.hasPermission(s.getPermission()))
                        .flatMap(s -> {
                            List<String> names = new ArrayList<>();
                            names.add(s.getName());
                            names.addAll(s.getAliases());
                            return names.stream();
                        })
                        .filter(n -> n.toLowerCase().startsWith(partial))
                        .collect(Collectors.toList());
            }

            // delegate to matching subcommand tab completion
            String token = args[0];
            for (Subcommand s : subcommands) {
                if (!s.matches(token)) continue;
                if (s.getPermission() != null && !s.getPermission().isEmpty() && !sender.hasPermission(s.getPermission())) continue;
                String[] rest = Arrays.copyOfRange(args, 1, args.length);
                return s.tabComplete(sender, rest);
            }

            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public String usage() {
        String subs = subcommands.stream().map(Subcommand::getName).collect(Collectors.joining("|"));
        return "/" + commandName + " <" + (subs.isEmpty() ? "..." : subs) + ">";
    }

    /**
     * Convenience builder entry point for quick command construction.
     * Preferred primary usage is to extend `EzCmd`, but this builder is useful
     * for small or example commands.
     */
    public static CommandBuilder builder(JavaPlugin plugin, String commandName) {
        return new CommandBuilder(plugin, commandName);
    }
}
