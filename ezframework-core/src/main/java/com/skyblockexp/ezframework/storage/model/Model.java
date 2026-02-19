package com.skyblockexp.ezframework.storage.model;

import java.util.Map;

/**
 * Base model class for lightweight storage-backed domain objects.
 *
 * Implementations must provide conversion to/from a flat Map of values.
 * The `id` is the primary identifier used by repositories to build storage
 * paths.
 */
public abstract class Model {
    protected String id;

    protected Model(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the storage path used for this model given a repository prefix.
     * If `prefix` is empty the path is simply the model id.
     */
    public String getStoragePath(String prefix) {
        if (prefix == null || prefix.isEmpty()) return id;
        return prefix + "/" + id;
    }

    /** Convert this model to a Map suitable for StorageProvider.save(). */
    public abstract Map<String, Object> toMap();

    /** Populate this model's fields from a Map loaded from storage. */
    public abstract void fromMap(Map<String, Object> map);
}
