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

    protected Subcommand(String name, String permission, boolean isDefault, String... aliases) {
        this.name = Objects.requireNonNull(name, "name");
        this.permission = permission == null ? "" : permission;
        this.isDefault = isDefault;
        this.aliases = (aliases == null) ? Collections.emptyList() : Arrays.asList(aliases);
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean matches(String token) {
        if (token == null) return false;
        if (name.equalsIgnoreCase(token)) return true;
        for (String a : aliases) if (a.equalsIgnoreCase(token)) return true;
        return false;
    }

    /**
     * Execute the subcommand. Returns true if handled.
     */
    public abstract boolean execute(CommandSender sender, String[] args) throws Exception;

    /**
     * Tab completion for this subcommand. Default empty list.
     */
    public List<String> tabComplete(CommandSender sender, String[] args) throws Exception {
        return Collections.emptyList();
    }

    /**
     * Optional usage string to display to users.
     */
    public String usage() {
        return "/" + getName();
    }
}
