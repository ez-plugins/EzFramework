package com.skyblockexp.ezframework.proxy.velocity;

import com.skyblockexp.ezframework.proxy.EzMessenger;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * Cross-version safe lookup helpers for locating the shared messenger.
 *
 * <p>This tries multiple strategies in order of compatibility:
 * <ol>
 *   <li>Find the `ezframework` plugin container and call `getInstance()` to obtain
 *       the plugin object, then call `getMessenger()` reflectively.</li>
 *   <li>If `getMessenger()` is not present, try to read a `messenger` field
 *       reflectively from the plugin instance.</li>
 *   <li>Return {@link Optional#empty()} if none of the strategies succeed.</li>
 * </ol>
 */
public final class VelocityMessengerLookup {

    private VelocityMessengerLookup() {
    }

    /**
     * Attempt to locate a `VelocityEzMessenger` instance for the running proxy.
     *
     * @param proxy Velocity proxy server
     * @return optional messenger when found
     */
    public static Optional<VelocityEzMessenger> find(ProxyServer proxy) {
        Objects.requireNonNull(proxy, "proxy");

        try {
            Optional<PluginContainer> maybe = proxy.getPluginManager().getPlugin("ezframework");
            if (maybe.isEmpty()) {
                return Optional.empty();
            }
            PluginContainer container = maybe.get();

            // Try to get the plugin instance (some Velocity versions expose getInstance())
            try {
                Method getInstance = PluginContainer.class.getMethod("getInstance");
                Object instOptional = getInstance.invoke(container);
                if (instOptional instanceof Optional<?> opt && opt.isPresent()) {
                    Object inst = opt.get();
                    Optional<VelocityEzMessenger> byMethod = findViaGetMessenger(inst);
                    if (byMethod.isPresent()) return byMethod;
                    Optional<VelocityEzMessenger> byField = findViaField(inst);
                    if (byField.isPresent()) return byField;
                }
            } catch (NoSuchMethodException ignored) {
                // Fall through — some older/newer API shapes differ.
            }

            // As a last resort try container.getInstance().orNull() style via reflection
            try {
                // Some implementations may expose getInstance() returning the raw plugin
                Method getInstanceRaw = PluginContainer.class.getMethod("getInstanceRaw");
                Object inst = getInstanceRaw.invoke(container);
                if (inst != null) {
                    Optional<VelocityEzMessenger> byMethod = findViaGetMessenger(inst);
                    if (byMethod.isPresent()) return byMethod;
                    Optional<VelocityEzMessenger> byField = findViaField(inst);
                    if (byField.isPresent()) return byField;
                }
            } catch (NoSuchMethodException ignored) {
                // ignore
            }
        } catch (Exception e) {
            // Silent failure — we don't want lookup to throw for callers
        }
        return Optional.empty();
    }

    private static Optional<VelocityEzMessenger> findViaGetMessenger(Object inst) {
        try {
            Method gm = inst.getClass().getMethod("getMessenger");
            Object res = gm.invoke(inst);
            if (res instanceof VelocityEzMessenger vem) return Optional.of(vem);
            if (res instanceof EzMessenger && res instanceof VelocityEzMessenger) return Optional.of((VelocityEzMessenger) res);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    private static Optional<VelocityEzMessenger> findViaField(Object inst) {
        try {
            Field f = inst.getClass().getDeclaredField("messenger");
            f.setAccessible(true);
            Object res = f.get(inst);
            if (res instanceof VelocityEzMessenger vem) return Optional.of(vem);
            if (res instanceof EzMessenger && res instanceof VelocityEzMessenger) return Optional.of((VelocityEzMessenger) res);
        } catch (NoSuchFieldException ignored) {
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}
