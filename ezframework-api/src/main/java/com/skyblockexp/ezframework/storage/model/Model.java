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
    /**
     * Primary identifier for this model.
     */
    protected String id;

    /**
     * Construct a model with the given id.
     *
     * @param id model identifier
     */
    protected Model(String id) {
        this.id = id;
    }

    /**
     * Get the model id.
     *
     * @return model identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Set the model id.
     *
     * @param id model identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the storage path used for this model given a repository prefix.
     * If `prefix` is empty the path is simply the model id.
     */
    /**
     * Build the storage path for this model.
     * @param prefix repository prefix (may be null or empty)
     * @return storage path (prefix/id or id when prefix is empty)
     */
    public String getStoragePath(String prefix) {
        if (prefix == null || prefix.isEmpty()) return id;
        return prefix + "/" + id;
    }

    /**
     * Convert this model to a Map suitable for {@code StorageProvider.save()}.
     * @return map of attribute names to values
     */
    public abstract Map<String, Object> toMap();

    /**
     * Populate this model's fields from a Map loaded from storage.
     * @param map source attributes (may be null)
     */
    public abstract void fromMap(Map<String, Object> map);
}
