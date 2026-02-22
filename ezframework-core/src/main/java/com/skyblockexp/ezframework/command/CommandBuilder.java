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

    /**
     * Create a new CommandBuilder for the given plugin and command name.
     *
     * @param plugin the plugin instance
     * @param commandName the command name to register
     */
    public CommandBuilder(JavaPlugin plugin, String commandName) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.commandName = Objects.requireNonNull(commandName, "commandName");
    }

    /**
     * Set a short description for the command.
     *
     * @param description short description text
     * @return this builder for chaining
     */
    public CommandBuilder description(String description) {
        this.description = description == null ? "" : description;
        return this;
    }

    /**
     * Set the permission required to execute the command.
     *
     * @param permission permission node (empty for none)
     * @return this builder for chaining
     */
    public CommandBuilder permission(String permission) {
        this.permission = permission == null ? "" : permission;
        return this;
    }

    /**
     * Provide a simple executor callback for the command.
     *
     * @param executor callback to execute command logic
     * @return this builder for chaining
     */
    public CommandBuilder executor(SimpleExecutor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Provide a simple tab-completer callback.
     *
     * @param completer tab completion provider
     * @return this builder for chaining
     */
    public CommandBuilder completer(SimpleTabCompleter completer) {
        this.completer = completer;
        return this;
    }

    /**
     * Add a subcommand to the constructed command.
     *
     * @param subcommand the subcommand to add
     * @return this builder for chaining
     */
    public CommandBuilder subcommand(Subcommand subcommand) {
        if (subcommand != null) this.subcommands.add(subcommand);
        return this;
    }

    /**
     * Build an {@link EzCmd} instance. The returned command is registered with
     * the underlying Bukkit command system when constructed by {@link EzCmd}.
     *
     * @return constructed EzCmd instance
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

    /**
     * Functional callback used to execute a simple command implementation.
     */
    @FunctionalInterface
    public interface SimpleExecutor {
        /**
         * Execute the command.
         *
         * @param sender the command sender
         * @param args command arguments
         * @return true if command was handled
         * @throws Exception on errors
         */
        boolean execute(CommandSender sender, String[] args) throws Exception;
    }

    /**
     * Functional callback used to provide tab-completion candidates.
     */
    @FunctionalInterface
    public interface SimpleTabCompleter {
        /**
         * Provide tab-completion candidates.
         *
         * @param sender the command sender
         * @param args current arguments
         * @return candidate completions
         * @throws Exception on errors
         */
        List<String> complete(CommandSender sender, String[] args) throws Exception;
    }
}
