package com.skyblockexp.ezframework.listener;

/**
 * Marker interface that must be implemented by all packet listener classes.
 *
 * <p>A class implementing this interface can contain any number of methods
 * annotated with {@link PacketListener}. Those methods will be discovered
 * and registered automatically when the listener is passed to
 * {@link ListenerMessenger#registerListener(PacketListenerInterface)}.
 *
 * <h2>Implementing a listener</h2>
 * <blockquote><pre>
 * public class MyPacketListener implements PacketListenerInterface {
 *
 *     {@literal @}PacketListener
 *     public void onMyPacket(MyPacketEvent event) {
 *         // handle event
 *     }
 * }
 * </pre></blockquote>
 *
 * <h2>Registering a listener</h2>
 * <blockquote><pre>
 * messenger.registerListener(new MyPacketListener());
 * </pre></blockquote>
 *
 * @see PacketListener
 * @see ListenerMessenger
 */
public interface PacketListenerInterface {}
