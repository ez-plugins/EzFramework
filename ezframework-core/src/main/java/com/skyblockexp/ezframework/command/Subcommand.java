package com.skyblockexp.ezframework.command;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base class for subcommands used with {@link EzCmd}.
 */
public abstract class Subcommand {
    private final String name;
    private final List<String> aliases;
    private final String permission;
    private final boolean isDefault;

    /**
     * Create a subcommand with optional aliases and permission.
     *
     * @param name the primary name of the subcommand
     * @param permission required permission node (may be null/empty)
     * @param isDefault whether this subcommand is the default fallback
     * @param aliases optional aliases
     */
    protected Subcommand(String name, String permission, boolean isDefault, String... aliases) {
        this.name = Objects.requireNonNull(name, "name");
        this.permission = permission == null ? "" : permission;
        this.isDefault = isDefault;
        this.aliases = (aliases == null) ? Collections.emptyList() : Arrays.asList(aliases);
    }

    /**
     * Get the primary name of this subcommand.
     *
     * @return subcommand name
     */
    public String getName() {
        return name;
    }

    /**
     * Get aliases registered for this subcommand.
     *
     * @return immutable list of aliases
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Get the permission required to execute this subcommand.
     *
     * @return permission node (may be empty)
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Whether this subcommand is marked as the default.
     *
     * @return true if default
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Check whether the provided token matches this subcommand's name or aliases.
     *
     * @param token input token
     * @return true if matches
     */
    public boolean matches(String token) {
        if (token == null) return false;
        if (name.equalsIgnoreCase(token)) return true;
        for (String a : aliases) if (a.equalsIgnoreCase(token)) return true;
        return false;
    }

    /**
     * Execute the subcommand.
     *
     * @param sender command sender
     * @param args command arguments
     * @return true if handled
     * @throws Exception on execution error
     */
    public abstract boolean execute(CommandSender sender, String[] args) throws Exception;

    /**
     * Tab completion for this subcommand. Default empty list.
     *
     * @param sender command sender
     * @param args current arguments
     * @return candidate completions
     * @throws Exception on error
     */
    public List<String> tabComplete(CommandSender sender, String[] args) throws Exception {
        return Collections.emptyList();
    }

    /**
     * Optional usage string to display to users.
     *
     * @return usage string
     */
    public String usage() {
        return "/" + getName();
    }
}
