package com.skyblockexp.ezframework.storage.migration;

/**
 * Simple descriptor for a migration resource.
 */
public final class MigrationDescriptor {
    private final String id;
    private final String description;
    private final MigrationType type;
    private final String resourcePath;
    private final String providerName; // null means apply to all providers

    /**
     * Create a migration descriptor.
     * @param id unique id
     * @param description human readable description
     * @param type migration type
     * @param resourcePath resource path inside plugin jar
     * @param providerName provider name or null for all providers
     */
    public MigrationDescriptor(String id, String description, MigrationType type, String resourcePath, String providerName) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.resourcePath = resourcePath;
        this.providerName = providerName;
    }

    /**
     * Get the migration id.
     *
     * @return migration id
     */
    public String id() { return id; }

    /**
     * Get the migration description.
     *
     * @return description
     */
    public String description() { return description; }

    /**
     * Get the migration type.
     *
     * @return migration type
     */
    public MigrationType type() { return type; }

    /**
     * Get the resource path inside the plugin JAR.
     *
     * @return resource path
     */
    public String resourcePath() { return resourcePath; }

    /**
     * Get the provider name this migration targets, or null for all providers.
     *
     * @return provider name or null
     */
    public String providerName() { return providerName; }
}
