package com.skyblockexp.ezframework.storage.migration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MigrationDescriptorFeatureTest {

    @Test
    void constructorAndGetters() {
        MigrationDescriptor d = new MigrationDescriptor(
                "v1_create_users",
                "Create users table",
                MigrationType.SQL,
                "migrations/v1_create_users.sql",
                "mysql"
        );
        assertEquals("v1_create_users", d.id());
        assertEquals("Create users table", d.description());
        assertEquals(MigrationType.SQL, d.type());
        assertEquals("migrations/v1_create_users.sql", d.resourcePath());
        assertEquals("mysql", d.providerName());
    }

    @Test
    void nullProviderNameAllowed() {
        MigrationDescriptor d = new MigrationDescriptor(
                "v1", "desc", MigrationType.SQL, "path/v1.sql", null
        );
        assertNull(d.providerName(), "null providerName means apply to all providers");
    }

    @Test
    void differentMigrationTypes() {
        for (MigrationType type : MigrationType.values()) {
            MigrationDescriptor d = new MigrationDescriptor("id", "desc", type, "res", "prov");
            assertEquals(type, d.type());
        }
    }

    @Test
    void idAndDescriptionPreserved() {
        MigrationDescriptor d = new MigrationDescriptor("my-id", "my description", MigrationType.SQL, "r", "p");
        assertEquals("my-id", d.id());
        assertEquals("my description", d.description());
    }

    @Test
    void resourcePathPreserved() {
        String path = "migrations/complex/path/v2_add_column.sql";
        MigrationDescriptor d = new MigrationDescriptor("v2", "desc", MigrationType.SQL, path, null);
        assertEquals(path, d.resourcePath());
    }
}
