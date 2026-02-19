package com.skyblockexp.ezframework.bootstrap;

/**
 * Simple bootstrap component interface representing a startup/stop unit.
 * Implementations should perform plugin initialization in `start()` and
 * cleanup in `stop()`.
 */
public interface Component {
    void start() throws Exception;

    void stop() throws Exception;

    default void reload() throws Exception {}
}
