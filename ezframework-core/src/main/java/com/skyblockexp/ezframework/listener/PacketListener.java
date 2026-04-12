package com.skyblockexp.ezframework.listener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a proxy packet event handler.
 *
 * <p>Annotated methods must:
 * <ul>
 *   <li>be declared in a class that implements {@link PacketListenerInterface}</li>
 *   <li>accept exactly <em>one</em> parameter — the packet event type to handle</li>
 *   <li>return {@code void}</li>
 * </ul>
 *
 * <p>Methods that do not satisfy these requirements are silently ignored during
 * registration.
 *
 * <h2>Example</h2>
 * <blockquote><pre>
 * public class MyListener implements PacketListenerInterface {
 *
 *     {@literal @}PacketListener(priority = EventPriority.NORMAL)
 *     public void onPlayerLogin(PlayerLoginPacketEvent event) {
 *         if (isBanned(event.getPlayerName())) {
 *             event.setCancelled(true);
 *         }
 *     }
 *
 *     {@literal @}PacketListener(priority = EventPriority.MONITOR, ignoreCancelled = true)
 *     public void logPlayerLogin(PlayerLoginPacketEvent event) {
 *         logger.info("Login from {}", event.getPlayerName());
 *     }
 * }
 * </pre></blockquote>
 *
 * @see PacketListenerInterface
 * @see EventPriority
 * @see ListenerMessenger#registerListener(PacketListenerInterface)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PacketListener {

    /**
     * The execution priority of this handler relative to other handlers
     * registered for the same packet type.
     *
     * <p>Handlers with lower priority ordinals execute first. Defaults to
     * {@link EventPriority#NORMAL}.
     *
     * @return the handler's execution priority
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * Whether this handler should be skipped when the packet event has been
     * cancelled by an earlier handler.
     *
     * <p>Only meaningful when the packet event implements {@link Cancellable}.
     * When {@code true} and the event is cancelled, this method will not be
     * invoked. Defaults to {@code false}.
     *
     * @return {@code true} to skip execution for cancelled events
     */
    boolean ignoreCancelled() default false;
}
