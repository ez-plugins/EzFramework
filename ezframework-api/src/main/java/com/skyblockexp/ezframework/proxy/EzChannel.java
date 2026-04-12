package com.skyblockexp.ezframework.proxy;

import java.util.Objects;

/**
 * Represents a named plugin messaging channel used to transport {@link EzPacket}s.
 *
 * <p>The channel name follows the {@code namespace:key} format required by
 * Minecraft's plugin channel protocol (e.g. {@code "ezframework:channel"}).
 * Instances are value objects and are safe to share across threads.
 *
 * <p>The default channel used by EzFramework is {@link #DEFAULT}.
 */
public final class EzChannel {

    /**
     * The default channel identifier used when no custom channel is specified.
     */
    public static final EzChannel DEFAULT = new EzChannel("ezframework:channel");

    private final String name;

    /**
     * Create a channel with the given name.
     *
     * @param name channel name in {@code namespace:key} format; must not be null
     */
    public EzChannel(String name) {
        this.name = Objects.requireNonNull(name, "channel name must not be null");
    }

    /**
     * Return the raw channel name string.
     *
     * @return channel name
     */
    public String getName() {
        return name;
    }

    /**
     * Convenience method to split the name into its {@code [namespace, key]} parts.
     * Returns an array with one element if the name contains no colon.
     *
     * @return array of [namespace, key] or [fullName] if no colon is present
     */
    public String[] parts() {
        int colon = name.indexOf(':');
        if (colon < 0) return new String[]{name};
        return new String[]{name.substring(0, colon), name.substring(colon + 1)};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EzChannel)) return false;
        return name.equals(((EzChannel) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "EzChannel{" + name + "}";
    }
}
