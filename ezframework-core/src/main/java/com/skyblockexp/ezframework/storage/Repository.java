package com.skyblockexp.ezframework.storage;

import java.util.List;
import java.util.Optional;

/**
 * Repository contract for domain objects persisted via {@link StorageProvider}s.
 * Implementations translate domain entities to/from storage maps and delegate
 * storage operations to a provider.
 */
public interface Repository<T, ID> {
    Optional<T> find(ID id) throws Exception;

    List<T> findAll() throws Exception;

    void save(T entity) throws Exception;

    void delete(ID id) throws Exception;
}
