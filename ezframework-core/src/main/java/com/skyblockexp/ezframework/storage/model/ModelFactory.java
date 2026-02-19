package com.skyblockexp.ezframework.storage.model;

import java.util.Map;

/**
 * Factory used by ModelRepository to instantiate model instances from stored
 * data.
 */
@FunctionalInterface
public interface ModelFactory<T extends Model> {
    /**
     * Create a model instance for the given id and persisted data.
     */
    T create(String id, Map<String, Object> data);
}
