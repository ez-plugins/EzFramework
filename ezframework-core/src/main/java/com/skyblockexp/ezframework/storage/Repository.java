package com.skyblockexp.ezframework.storage;

import java.util.List;
import java.util.Optional;

/**
 * Repository contract for domain objects persisted via {@link StorageProvider}s.
 * Implementations translate domain entities to/from storage maps and delegate
 * storage operations to a provider.
 *
 * @param <T>  the entity type managed by the repository
 * @param <ID> the type used for entity identifiers
 */
public interface Repository<T, ID> {
    /**
     * Find an entity by id.
     *
     * @param id the id of the entity to find
     * @return an Optional containing the entity if found
     * @throws Exception on storage errors
     */
    Optional<T> find(ID id) throws Exception;

    /**
     * Retrieve all entities.
     *
     * @return list of all entities
     * @throws Exception on storage errors
     */
    List<T> findAll() throws Exception;

    /**
     * Persist or update an entity.
     *
     * @param entity the entity to save
     * @throws Exception on storage errors
     */
    void save(T entity) throws Exception;

    /**
     * Delete an entity by id.
     *
     * @param id the id of the entity to delete
     * @throws Exception on storage errors
     */
    void delete(ID id) throws Exception;
}
