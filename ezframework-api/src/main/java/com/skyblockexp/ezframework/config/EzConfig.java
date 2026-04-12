package com.skyblockexp.ezframework.config;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Lightweight framework-neutral config API used by plugins and framework components.
 */
public interface EzConfig {
    /** Load or create the underlying config file. */
    void load() throws IOException;

    /** Reload the config from disk. */
    void reload() throws IOException;

    /** Save current config to disk. */
    void save() throws IOException;

    /** Copy default resource to disk if missing. */
    void saveDefault() throws IOException;

    /** Returns plugin data folder path where this config is stored. */
    Path getDataFolder();

    /** Returns the config file name. */
    String getFileName();
}
