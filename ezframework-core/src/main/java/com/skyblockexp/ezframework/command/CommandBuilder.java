package com.skyblockexp.ezframework.command;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Lightweight builder for creating quick `EzCmd` instances for examples
 * and small commands. This is a convenience helper; the preferred approach
 * for production code is to create a dedicated class that extends `EzCmd`.
 */
public final class CommandBuilder {
    private final JavaPlugin plugin;
    private final String commandName;
    private String description = "";
    private String permission = "";
    private SimpleExecutor executor;
    private SimpleTabCompleter completer;
    private final List<Subcommand> subcommands = new ArrayList<>();

    public CommandBuilder(JavaPlugin plugin, String commandName) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.commandName = Objects.requireNonNull(commandName, "commandName");
    }

    public CommandBuilder description(String description) {
        this.description = description == null ? "" : description;
        return this;
    }

    public CommandBuilder permission(String permission) {
        this.permission = permission == null ? "" : permission;
        return this;
    }

    public CommandBuilder executor(SimpleExecutor executor) {
        this.executor = executor;
        return this;
    }

    public CommandBuilder completer(SimpleTabCompleter completer) {
        this.completer = completer;
        return this;
    }

    public CommandBuilder subcommand(Subcommand subcommand) {
        if (subcommand != null) this.subcommands.add(subcommand);
        return this;
    }

    /**
     * Build an `EzCmd` instance. The returned command is registered with the
     * underlying Bukkit `plugin.getCommand(...)` when constructed by `EzCmd`.
     */
    public EzCmd build() {
        final SimpleExecutor exec = this.executor;
        final SimpleTabCompleter comp = this.completer;

        EzCmd cmd = new EzCmd(plugin, commandName, Collections.unmodifiableList(subcommands)) {
            @Override
            public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
                try {
                    if (exec != null) {
                        return exec.execute(sender, args == null ? new String[0] : args);
                    }
                    return super.onCommand(sender, command, label, args);
                } catch (Exception e) {
                    plugin.getLogger().severe("Error executing command '" + commandName + "': " + e.getMessage());
                    return true;
                }
            }

            @Override
            public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
                try {
                    if (comp != null) return comp.complete(sender, args == null ? new String[0] : args);
                    return super.onTabComplete(sender, command, alias, args);
                } catch (Exception e) {
                    return Collections.emptyList();
                }
            }
        };

        return cmd;
    }

    @FunctionalInterface
    public interface SimpleExecutor {
        boolean execute(CommandSender sender, String[] args) throws Exception;
    }

    @FunctionalInterface
    public interface SimpleTabCompleter {
        List<String> complete(CommandSender sender, String[] args) throws Exception;
    }
}
