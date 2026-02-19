package com.skyblockexp.ezframework.message.minimessage;

import com.skyblockexp.ezframework.message.api.MessageProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Message provider implementation using Adventure MiniMessage.
 */
public class MiniMessageProvider implements MessageProvider {
    private volatile String prefix = "";
    private volatile BukkitAudiences bukkitAudiences;
    // Use Object to avoid compile-time dependency on specific MiniMessage API
    private volatile Object miniMessage;

    @Override
    public void init(JavaPlugin plugin) throws Exception {
        try {
            this.bukkitAudiences = BukkitAudiences.create(plugin);
        } catch (Throwable t) {
            this.bukkitAudiences = null;
        }

        try {
            try {
                Class<?> mmClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
                try {
                    // prefer static get() if present
                    this.miniMessage = mmClass.getMethod("get").invoke(null);
                } catch (NoSuchMethodException ns) {
                    this.miniMessage = null;
                }
            } catch (Throwable t) {
                this.miniMessage = null;
            }
        } catch (Throwable t) {
            this.miniMessage = null;
        }
    }

    @Override
    public String format(String message) {
        if (message == null) return "";
        String withPrefix = prefix.isEmpty() ? message : (prefix + " " + message);

        if (bukkitAudiences != null && miniMessage != null) {
            try {
                java.lang.reflect.Method parse = miniMessage.getClass().getMethod("parse", String.class);
                Object component = parse.invoke(miniMessage, withPrefix);
                return LegacyComponentSerializer.legacySection().serialize((Component) component);
            } catch (Throwable ignored) {
            }
        }

        return ChatColor.translateAlternateColorCodes('&', withPrefix);
    }

    @Override
    public void send(CommandSender to, String message) {
        if (bukkitAudiences != null && miniMessage != null) {
            try {
                java.lang.reflect.Method parse = miniMessage.getClass().getMethod("parse", String.class);
                Object component = parse.invoke(miniMessage, message);
                bukkitAudiences.sender(to).sendMessage((Component) component);
                return;
            } catch (Throwable ignored) {
            }
        }

        to.sendMessage(format(message));
    }

    @Override
    public void broadcast(String message) {
        if (bukkitAudiences != null && miniMessage != null) {
            try {
                java.lang.reflect.Method parse = miniMessage.getClass().getMethod("parse", String.class);
                Object component = parse.invoke(miniMessage, message);
                try {
                    // try server() no-arg
                    java.lang.reflect.Method server = bukkitAudiences.getClass().getMethod("server");
                    Object audience = server.invoke(bukkitAudiences);
                    audience.getClass().getMethod("sendMessage", Component.class).invoke(audience, (Component) component);
                    return;
                } catch (NoSuchMethodException ns) {
                    // try server(String)
                    try {
                        java.lang.reflect.Method server2 = bukkitAudiences.getClass().getMethod("server", String.class);
                        Object audience = server2.invoke(bukkitAudiences, "");
                        audience.getClass().getMethod("sendMessage", Component.class).invoke(audience, (Component) component);
                        return;
                    } catch (Throwable ignored) {
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        Bukkit.getServer().broadcastMessage(format(message));
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = (prefix == null) ? "" : prefix;
    }
}
