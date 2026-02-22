package com.skyblockexp.ezframework.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

/**
 * Convenience base class for repositories that use a {@link StorageProvider}.
 * Subclasses must implement simple serialization hooks `toMap`/`fromMap` and
 * provide an ID-to-path mapping.
 *
 * @param <T>  the entity type
 * @param <ID> the identifier type
 */
public abstract class AbstractRepository<T, ID> implements Repository<T, ID> {
    private final StorageProvider provider;
    private final String prefix;

    /**
     * Create a new AbstractRepository.
     *
     * @param provider storage provider to use (must not be null)
     * @param prefix   optional path prefix for stored entities
     */
    protected AbstractRepository(StorageProvider provider, String prefix) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.prefix = (prefix == null) ? "" : prefix;
    }

    /**
     * Access the configured {@link StorageProvider}.
     *
     * @return the storage provider
     */
    protected StorageProvider provider() {
        return provider;
    }

    /**
     * Build a storage path for the given id using the repository prefix.
     *
     * @param id the identifier
     * @return the storage path for the id
     */
    protected String pathFor(ID id) {
        return prefix + id.toString();
    }

    /**
     * Serialize an entity to a Map representation for storage.
     *
     * @param entity the entity to serialize
     * @return map suitable for the underlying storage provider
     */
    protected abstract Map<String, Object> toMap(T entity);

    /**
     * Reconstruct an entity instance from stored map data.
     *
     * @param map stored values
     * @return a reconstructed entity
     */
    protected abstract T fromMap(Map<String, Object> map);

    @Override
    public Optional<T> find(ID id) throws Exception {
        String path = pathFor(id);
        Optional<Map<String, Object>> data = provider.load(path);
        return data.map(this::fromMap);
    }

    @Override
    /**
     * Default implementation returns an empty list. Subclasses may override
     * to provide listing support.
     *
     * @return list of all entities (may be empty)
     * @throws Exception on storage errors
     */
    public List<T> findAll() throws Exception {
        // Generic storage providers may not offer listing—subclasses can override.
        return new ArrayList<>();
    }

    @Override
    /**
     * Persist the given entity using {@link #toMap(T)} and {@link #extractId(T)}.
     *
     * @param entity the entity to persist
     * @throws Exception on storage errors
     */
    public void save(T entity) throws Exception {
        Map<String, Object> data = toMap(entity);
        ID id = extractId(entity);
        provider.save(pathFor(id), data);
    }

    @Override
    public void delete(ID id) throws Exception {
        provider.delete(pathFor(id));
    }

    /**
     * Extract the identifier from the entity instance.
     *
     * @param entity the entity to inspect
     * @return the entity identifier
     */
    protected abstract ID extractId(T entity);
}
