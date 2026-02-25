package com.skyblockexp.ezframework.config;

/**
 * Provider interface used by the framework service loader to create
 * plugin-specific `EzConfig` instances. Implementations should be
 * registered via Java SPI (`META-INF/services/...`).
 *
 * Note: providers are created with no context to keep the SPI contract
 * framework-only. Implementations that need runtime context may obtain
 * resources via the current thread context classloader or other means.
 */
public interface EzConfigProvider {
    /**
     * Create an `EzConfig` instance.
     *
     * @return a non-null EzConfig instance
     */
    EzConfig create();
}
