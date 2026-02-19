package com.skyblockexp.ezframework.storage.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Lightweight Eloquent-like base model. Stores attributes in a map and
 * provides convenience getters/setters plus simple (de)serialization to a
 * Map which the framework `ModelRepository` understands.
 *
 * Usage:
 * public class PlayerData extends EzModel {
 *   public PlayerData(String id) { super(id); }
 *   public int getCoins() { return getAs("coins", Integer.class, 0); }
 *   public void setCoins(int c) { set("coins", c); }
 * }
 */
public abstract class EzModel extends Model {
    private final Map<String, Object> attributes = new HashMap<>();
    private Set<String> fillable = new HashSet<>();
    private Set<String> guarded = new HashSet<>();

    protected EzModel(String id) {
        super(id);
    }

    protected EzModel(String id, Map<String, Object> attrs) {
        super(id);
        if (attrs != null) fill(attrs);
    }

    /**
     * Specify which attributes are mass-assignable via `fill()`.
     * If `fillable` is empty, all attributes except those in `guarded`
     * are assignable.
     */
    public EzModel setFillable(String... keys) {
        fillable.clear();
        if (keys != null) for (String k : keys) if (k != null) fillable.add(k);
        return this;
    }

    /** Specify guarded attributes which cannot be mass-assigned. */
    public EzModel setGuarded(String... keys) {
        guarded.clear();
        if (keys != null) for (String k : keys) if (k != null) guarded.add(k);
        return this;
    }

    public Set<String> getFillable() { return Collections.unmodifiableSet(fillable); }
    public Set<String> getGuarded() { return Collections.unmodifiableSet(guarded); }

    protected boolean isFillable(String key) {
        if ("id".equals(key)) return false;
        if (!fillable.isEmpty()) return fillable.contains(key);
        return !guarded.contains(key);
    }

    public EzModel set(String key, Object value) {
        if ("id".equals(key)) {
            setId(value == null ? null : value.toString());
        } else {
            attributes.put(key, value);
        }
        return this;
    }

    public Object get(String key) {
        if ("id".equals(key)) return getId();
        return attributes.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(String key, Class<T> cls) {
        Object v = get(key);
        if (v == null) return null;
        if (cls.isInstance(v)) return (T) v;
        // try simple conversions
        if (cls == Integer.class && v instanceof Number) return (T) Integer.valueOf(((Number) v).intValue());
        if (cls == Long.class && v instanceof Number) return (T) Long.valueOf(((Number) v).longValue());
        if (cls == String.class) return (T) v.toString();
        return null;
    }

    public <T> T getAs(String key, Class<T> cls, T def) {
        T v = getAs(key, cls);
        return v == null ? def : v;
    }

    public Map<String, Object> toMap() {
        return new HashMap<>(attributes);
    }

    public void fromMap(Map<String, Object> map) {
        attributes.clear();
        if (map == null) return;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if ("id".equals(e.getKey())) {
                // if id present in map, set id as well
                if (e.getValue() != null) setId(e.getValue().toString());
            } else {
                attributes.put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Mass-assign attributes. Only keys allowed by `fillable`/`guarded` will be set.
     */
    public EzModel fill(Map<String, Object> attrs) {
        if (attrs == null) return this;
        for (Map.Entry<String, Object> e : attrs.entrySet()) {
            String k = e.getKey();
            if (k == null) continue;
            if (!isFillable(k)) continue;
            set(k, e.getValue());
        }
        return this;
    }

    public Map<String, Object> attributes() { return Collections.unmodifiableMap(attributes); }

    /** Convenience: start a `QueryBuilder`. */
    public static com.skyblockexp.ezframework.query.QueryBuilder queryBuilder() {
        return new com.skyblockexp.ezframework.query.QueryBuilder();
    }

    /**
     * Persist this model using the given repository. Returns the model for chaining.
     */
    @SuppressWarnings("unchecked")
    public <T extends EzModel> T save(ModelRepository<T> repo) throws Exception {
        repo.save((T) this);
        return (T) this;
    }

    /**
     * Delete this model using the given repository.
     */
    public <T extends EzModel> void delete(ModelRepository<T> repo) throws Exception {
        repo.delete(getId());
    }

    /**
     * Find a model by id using the repository (convenience static helper).
     */
    public static <T extends EzModel> T find(ModelRepository<T> repo, String id) throws Exception {
        java.util.Optional<T> opt = repo.find(id);
        return opt.orElse(null);
    }
}
