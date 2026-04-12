package com.skyblockexp.ezframework.proxy;

import java.util.Objects;

/**
 * Identifies a plugin's namespace within the EzFramework cross-server messaging
 * protocol, and serves as a factory for correctly-namespaced packet IDs.
 *
 * <h3>Why namespacing matters</h3>
 * <p>All plugins in an ecosystem (EzEconomy, EzShops, EzAuction, …) share a
 * single {@link EzMessenger} on the proxy. Without namespacing, packet IDs like
 * {@code "balance.request"} from two different plugins would collide silently,
 * causing the wrong handler to be called and corrupting state.
 *
 * <p>By requiring every packet ID to be in the {@code "pluginId:action"} format,
 * EzFramework guarantees that {@code "ezeconomy:balance.request"} and
 * {@code "ezshops:purchase.confirm"} can never conflict.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Declare once per plugin (e.g. as a constant in your main class)
 * public static final EzPacketNamespace NS = new EzPacketNamespace("ezeconomy");
 *
 * // Use in your packet implementations
 * public class BalanceRequestPacket implements EzPacket {
 *     private static final String ID = EzEconomy.NS.id("balance.request");
 *
 *     @Override
 *     public String packetId() { return ID; }
 * }
 * }</pre>
 */
public final class EzPacketNamespace {

    private final String namespace;

    /**
     * Create a namespace for the given plugin identifier.
     *
     * <p>The namespace must be a non-empty lowercase string containing only
     * letters, digits, underscores, and hyphens (no colons). Good examples:
     * {@code "ezeconomy"}, {@code "ezshops"}, {@code "ezauction"}.
     *
     * @param namespace the plugin identifier; must not be null, empty, or contain a colon
     * @throws IllegalArgumentException if the namespace is invalid
     */
    public EzPacketNamespace(String namespace) {
        Objects.requireNonNull(namespace, "namespace");
        if (namespace.isBlank()) {
            throw new IllegalArgumentException("Namespace must not be blank");
        }
        if (namespace.contains(":")) {
            throw new IllegalArgumentException(
                    "Namespace '" + namespace + "' must not contain a colon — "
                            + "the colon is used as separator between namespace and action");
        }
        this.namespace = namespace.toLowerCase();
    }

    /**
     * Build a fully-qualified packet ID in {@code "namespace:action"} format.
     *
     * <p>Use this as the return value of {@link EzPacket#packetId()}:
     * <pre>{@code
     * private static final String ID = MyPlugin.NS.id("balance.request");
     *
     * @Override public String packetId() { return ID; }
     * }</pre>
     *
     * @param action the action part of the ID (e.g. {@code "balance.request"});
     *               must not be null, empty, or contain a colon
     * @return the fully-qualified ID, e.g. {@code "ezeconomy:balance.request"}
     */
    public String id(String action) {
        Objects.requireNonNull(action, "action");
        if (action.isBlank()) {
            throw new IllegalArgumentException("Action must not be blank");
        }
        if (action.contains(":")) {
            throw new IllegalArgumentException(
                    "Action '" + action + "' must not contain a colon — "
                            + "use dots to separate logical parts, e.g. \"balance.request\"");
        }
        return namespace + ":" + action;
    }

    /**
     * Return whether this namespace owns a given fully-qualified packet ID,
     * i.e. whether the ID starts with {@code "namespace:"}.
     *
     * @param packetId the fully-qualified packet ID to check
     * @return {@code true} if the ID belongs to this namespace
     */
    public boolean owns(String packetId) {
        if (packetId == null) return false;
        return packetId.startsWith(namespace + ":");
    }

    /**
     * Return the raw namespace string (e.g. {@code "ezeconomy"}).
     *
     * @return namespace identifier
     */
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String toString() {
        return "EzPacketNamespace{" + namespace + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EzPacketNamespace)) return false;
        return namespace.equals(((EzPacketNamespace) o).namespace);
    }

    @Override
    public int hashCode() {
        return namespace.hashCode();
    }
}
