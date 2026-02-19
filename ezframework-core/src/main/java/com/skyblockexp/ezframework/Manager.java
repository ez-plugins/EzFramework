package com.skyblockexp.ezframework;

/**
 * Base lifecycle class for managers stored in the `Registry`.
 *
 * Extend and override `init()` and `shutdown()` as needed.
 */
public abstract class Manager {
    /** Called during startup/initialization. Default no-op. */
    public void init() throws Exception {}

    /** Called during shutdown/cleanup. Default no-op. */
    public void shutdown() throws Exception {}
}
