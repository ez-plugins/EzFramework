package com.skyblockexp.ezframework.bootstrap;

/**
 * Simple bootstrap component interface representing a startup/stop unit.
 * Implementations should perform plugin initialization in {@link #start()} and
 * cleanup in {@link #stop()}.
 */
public interface Component {
    /**
     * Start or initialize the component.
     *
     * @throws Exception on failure
     */
    void start() throws Exception;

    /**
     * Stop or cleanup the component.
     *
     * @throws Exception on failure
     */
    void stop() throws Exception;

    /**
     * Reload the component configuration/state. Default no-op.
     *
     * @throws Exception on failure
     */
    default void reload() throws Exception {}
}
