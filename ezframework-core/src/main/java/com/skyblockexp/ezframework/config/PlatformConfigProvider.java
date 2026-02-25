package com.skyblockexp.ezframework.config;

import com.skyblockexp.ezframework.EzPlugin;

/**
 * Platform modules implement this interface to provide config files into the
 * framework's ConfigRegistry. Implementations should register themselves via
 * {@code META-INF/services} so the core can discover them via ServiceLoader.
 */
public interface PlatformConfigProvider {
    void provide(EzPlugin plugin, ConfigRegistry registry) throws Exception;
}
