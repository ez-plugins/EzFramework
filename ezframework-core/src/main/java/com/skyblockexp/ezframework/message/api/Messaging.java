package com.skyblockexp.ezframework.message.api;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-plugin messaging manager. Use {@link #forPlugin(JavaPlugin)} to obtain
 * the messaging instance and register custom {@link MessageProvider}s.
 *
 * Placed in `message.api` so the manager and its interface can be extracted
 * without pulling in implementation details.
 */
public final class Messaging {
    private static final Map<JavaPlugin, Messaging> INSTANCES = new ConcurrentHashMap<>();

    private final JavaPlugin plugin;
    private volatile MessageProvider provider;

    private Messaging(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        // Try to instantiate the optional implementation via reflection so the
        // API module does not require the implementation at compile-time.
        MessageProvider p = null;
        try {
            Class<?> impl = Class.forName("com.skyblockexp.ezframework.message.minimessage.MiniMessageProvider");
            Object o = impl.getDeclaredConstructor().newInstance();
            p = (MessageProvider) o;
            p.init(plugin);
        } catch (Throwable ignored) {
        }

        if (p == null) {
            // fallback simple provider using legacy color codes
            p = new MessageProvider() {
                private String prefix = "";

                @Override
                public String format(String message) {
                    if (message == null) return "";
                    String withPrefix = prefix.isEmpty() ? message : (prefix + " " + message);
                    return org.bukkit.ChatColor.translateAlternateColorCodes('&', withPrefix);
                }

                @Override
                public void send(org.bukkit.command.CommandSender to, String message) {
                    to.sendMessage(format(message));
                }

                @Override
                public void broadcast(String message) {
                    org.bukkit.Bukkit.getServer().broadcastMessage(format(message));
                }

                @Override
                public String getPrefix() {
                    return prefix;
                }

                @Override
                public void setPrefix(String prefix) {
                    this.prefix = (prefix == null) ? "" : prefix;
                }
            };
        }

        this.provider = p;
    }

    public static Messaging forPlugin(JavaPlugin plugin) {
        return INSTANCES.computeIfAbsent(plugin, Messaging::new);
    }

    public MessageProvider getProvider() {
        return provider;
    }

    public void registerProvider(MessageProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    public void setPrefix(String prefix) {
        provider.setPrefix(prefix);
    }

    public String format(String message) {
        return provider.format(message);
    }

    public void send(CommandSender to, String message) {
        provider.send(to, message);
    }

    public void broadcast(String message) {
        provider.broadcast(message);
    }
}
