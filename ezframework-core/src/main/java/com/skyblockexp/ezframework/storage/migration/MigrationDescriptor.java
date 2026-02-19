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

    public MigrationDescriptor(String id, String description, MigrationType type, String resourcePath, String providerName) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.resourcePath = resourcePath;
        this.providerName = providerName;
    }

    public String id() { return id; }
    public String description() { return description; }
    public MigrationType type() { return type; }
    public String resourcePath() { return resourcePath; }
    public String providerName() { return providerName; }
}
