package com.skyblockexp.ezframework;

/**
 * Base lifecycle class for managers stored in the `Registry`.
 *
 * Extend and override `init()` and `shutdown()` as needed.
 */
public abstract class Manager {
    /** Create a new Manager instance. */
    protected Manager() {}

    /**
     * Called during startup/initialization. Default no-op.
     *
     * @throws Exception when initialization fails
     */
    public void init() throws Exception {}

    /**
     * Called during shutdown/cleanup. Default no-op.
     *
     * @throws Exception when shutdown fails
     */
    public void shutdown() throws Exception {}
}
