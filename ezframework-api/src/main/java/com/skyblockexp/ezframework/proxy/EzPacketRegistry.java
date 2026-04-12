package com.skyblockexp.ezframework.proxy;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps namespaced packet ID strings to their corresponding {@link EzPacket}
 * implementation classes. Required by {@link EzSerializer} to reconstruct
 * packets from raw bytes without knowing the type ahead of time.
 *
 * <h3>Namespace requirement</h3>
 * <p>Every packet ID <em>must</em> be in {@code "pluginId:action"} format
 * (e.g. {@code "ezeconomy:balance.request"}). This is enforced at registration
 * time to prevent silent ID collisions when multiple plugins — EzEconomy,
 * EzShops, EzAuction, etc. — share the same proxy messenger.
 *
 * <p>Use {@link EzPacketNamespace#id(String)} to build IDs safely:
 * <pre>{@code
 * public static final EzPacketNamespace NS = new EzPacketNamespace("ezeconomy");
 *
 * public class BalanceRequestPacket implements EzPacket {
 *     @Override public String packetId() { return NS.id("balance.request"); }
 * }
 * }</pre>
 *
 * <p>Thread-safe.
 */
public final class EzPacketRegistry {

    private final Map<String, Class<? extends EzPacket>> idToClass = new ConcurrentHashMap<>();

    /**
     * Register a packet class under its declared {@link EzPacket#packetId()}.
     *
     * <p>The packet ID must be in {@code "pluginId:action"} format. The class
     * must also have a public no-arg constructor so {@link EzSerializer} can
     * instantiate it via Gson.
     *
     * @param packetClass the packet implementation class to register
     * @param <T>         the packet type
     * @return this registry for fluent chaining
     * @throws IllegalArgumentException if the ID is not namespaced, or if the
     *                                  class lacks a public no-arg constructor
     */
    public <T extends EzPacket> EzPacketRegistry register(Class<T> packetClass) {
        Objects.requireNonNull(packetClass, "packetClass");
        try {
            T instance = packetClass.getDeclaredConstructor().newInstance();
            String id = instance.packetId();
            Objects.requireNonNull(id, "packetId() returned null for " + packetClass.getName());
            requireNamespaced(id, packetClass.getSimpleName());
            idToClass.put(id, packetClass);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Cannot register " + packetClass.getName()
                            + ": requires a public no-arg constructor", e);
        }
        return this;
    }

    /**
     * Register a packet class under an explicit ID, overriding its
     * {@link EzPacket#packetId()} value. The explicit ID must still be in
     * {@code "pluginId:action"} format.
     *
     * @param id          the namespaced packet ID to register under
     * @param packetClass the packet implementation class
     * @param <T>         the packet type
     * @return this registry for fluent chaining
     * @throws IllegalArgumentException if the ID is not in {@code "pluginId:action"} format
     */
    public <T extends EzPacket> EzPacketRegistry register(String id, Class<T> packetClass) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(packetClass, "packetClass");
        requireNamespaced(id, packetClass.getSimpleName());
        idToClass.put(id, packetClass);
        return this;
    }

    /**
     * Look up the packet class for the given fully-qualified ID.
     *
     * @param id the namespaced packet ID (e.g. {@code "ezeconomy:balance.request"})
     * @return an {@link Optional} containing the class if registered
     */
    public Optional<Class<? extends EzPacket>> getClass(String id) {
        return Optional.ofNullable(idToClass.get(id));
    }

    /**
     * Return whether a packet ID is currently registered.
     *
     * @param id packet ID to check
     * @return {@code true} if registered
     */
    public boolean isRegistered(String id) {
        return idToClass.containsKey(id);
    }

    /**
     * Return the number of registered packet types.
     *
     * @return size of the registry
     */
    public int size() {
        return idToClass.size();
    }

    // -------------------------------------------------------------------------
    // Internal validation
    // -------------------------------------------------------------------------

    /**
     * Enforce that {@code id} is in {@code "namespace:action"} format.
     * Both the namespace and action parts must be non-empty.
     *
     * @param id          the packet ID to validate
     * @param contextName the class or registration context name (for error messages)
     * @throws IllegalArgumentException if the format is violated
     */
    static void requireNamespaced(String id, String contextName) {
        int colon = id.indexOf(':');
        if (colon <= 0 || colon == id.length() - 1) {
            throw new IllegalArgumentException(
                    "Packet ID '" + id + "' for " + contextName + " is not namespaced. "
                            + "IDs must be in 'pluginId:action' format "
                            + "(e.g. 'ezeconomy:balance.request') to prevent collisions "
                            + "between plugins sharing the same proxy messenger. "
                            + "Use EzPacketNamespace to build IDs safely.");
        }
        if (id.indexOf(':', colon + 1) >= 0) {
            throw new IllegalArgumentException(
                    "Packet ID '" + id + "' for " + contextName
                            + " contains more than one colon. "
                            + "Use a single colon to separate namespace from action, "
                            + "e.g. 'ezeconomy:balance.request'.");
        }
    }
}
