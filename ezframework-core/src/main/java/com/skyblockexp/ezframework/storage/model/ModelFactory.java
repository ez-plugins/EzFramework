package com.skyblockexp.ezframework.storage.model;

import java.util.Map;

/**
 * Factory used by {@link com.skyblockexp.ezframework.storage.model.ModelRepository}
 * to instantiate model instances from stored data.
 *
 * @param <T> the model type
 */
@FunctionalInterface
public interface ModelFactory<T extends Model> {
    /**
     * Create a model instance for the given id and persisted data.
     *
     * @param id   the model id
     * @param data persisted values
     * @return a new model instance (implementation may populate fields via {@code fromMap})
     */
    T create(String id, Map<String, Object> data);
}
