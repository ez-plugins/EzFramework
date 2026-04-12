package com.skyblockexp.ezframework.storage.migration;

/**
 * Type of migration artifact. SQL indicates plain SQL scripts; JAVA indicates
 * a programmatic migration (Java-based runner).
 */
public enum MigrationType {
    /** SQL-based migration (script resource). */
    SQL,
    /** Java-based programmatic migration. */
    JAVA
}
