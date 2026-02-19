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
 */
public abstract class AbstractRepository<T, ID> implements Repository<T, ID> {
    private final StorageProvider provider;
    private final String prefix;

    protected AbstractRepository(StorageProvider provider, String prefix) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.prefix = (prefix == null) ? "" : prefix;
    }

    protected StorageProvider provider() {
        return provider;
    }

    protected String pathFor(ID id) {
        return prefix + id.toString();
    }

    protected abstract Map<String, Object> toMap(T entity);

    protected abstract T fromMap(Map<String, Object> map);

    @Override
    public Optional<T> find(ID id) throws Exception {
        String path = pathFor(id);
        Optional<Map<String, Object>> data = provider.load(path);
        return data.map(this::fromMap);
    }

    @Override
    public List<T> findAll() throws Exception {
        // Generic storage providers may not offer listing—subclasses can override.
        return new ArrayList<>();
    }

    @Override
    public void save(T entity) throws Exception {
        Map<String, Object> data = toMap(entity);
        ID id = extractId(entity);
        provider.save(pathFor(id), data);
    }

    @Override
    public void delete(ID id) throws Exception {
        provider.delete(pathFor(id));
    }

    /** Extract the ID from the entity. Subclasses should implement. */
    protected abstract ID extractId(T entity);
}
