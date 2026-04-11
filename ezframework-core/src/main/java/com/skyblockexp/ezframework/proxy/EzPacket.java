package com.skyblockexp.ezframework.proxy;

/**
 * Marker interface for all cross-server packets. Implementations should be
 * plain POJOs with no platform-specific imports.
 *
 * <h3>Packet ID format — namespacing is mandatory</h3>
 * <p>{@link #packetId()} <em>must</em> return a string in
 * {@code "pluginId:action"} format, e.g. {@code "ezeconomy:balance.request"}.
 * This is enforced by {@link EzPacketRegistry} at registration time.
 *
 * <p>Because all plugins in the ecosystem (EzEconomy, EzShops, EzAuction, …)
 * share the same proxy messenger, namespacing is the only thing preventing
 * silent ID collisions where two plugins register the same string and one's
 * handler silently handles the other's packets.
 *
 * <p>Use {@link EzPacketNamespace} to build IDs safely and consistently:
 * <pre>{@code
 * // Declare once per plugin
 * public static final EzPacketNamespace NS = new EzPacketNamespace("ezeconomy");
 *
 * // Reference in each packet class
 * public class BalanceRequestPacket implements EzPacket {
 *     private static final String ID = EzEconomy.NS.id("balance.request");
 *     // → "ezeconomy:balance.request"
 *
 *     @Override
 *     public String packetId() { return ID; }
 * }
 * }</pre>
 *
 * <h3>Implementation rules</h3>
 * <ul>
 *   <li>Packets must have a public no-arg constructor (required by {@link EzSerializer}).
 *   <li>All fields must be Gson-serializable (plain Java types, no transient state).
 *   <li>No platform-specific imports (Bukkit, Velocity, BungeeCord, …).
 * </ul>
 */
public interface EzPacket {

    /**
     * Return the stable, unique, namespaced identifier for this packet type.
     *
     * <p><strong>Required format:</strong> {@code "pluginId:action"} where
     * both parts are non-empty and separated by exactly one colon, e.g.
     * {@code "ezeconomy:balance.request"}, {@code "ezshops:purchase.confirm"}.
     *
     * @return the packet type ID; never null
     */
    String packetId();
}
