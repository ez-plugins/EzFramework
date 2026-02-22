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

    /**
     * Construct a model with the given id.
     *
     * @param id model identifier
     */
    protected EzModel(String id) {
        super(id);
    }

    /**
     * Construct a model with the given id and initial attributes.
     *
     * @param id    model identifier
     * @param attrs initial attributes to populate (may be null)
     */
    protected EzModel(String id, Map<String, Object> attrs) {
        super(id);
        if (attrs != null) fill(attrs);
    }

    /**
     * Specify which attributes are mass-assignable via `fill()`.
     * If `fillable` is empty, all attributes except those in `guarded`
     * are assignable.
     */
    /**
     * Set the list of mass-assignable attribute keys.
     * @param keys attribute names to mark as fillable
     * @return this model for chaining
     */
    /**
     * Set the list of mass-assignable attribute keys.
     *
     * @param keys attribute names to mark as fillable
     * @return this model for chaining
     */
    public EzModel setFillable(String... keys) {
        fillable.clear();
        if (keys != null) for (String k : keys) if (k != null) fillable.add(k);
        return this;
    }

    /** Specify guarded attributes which cannot be mass-assigned. */
    /**
     * Set the list of guarded attribute keys.
     * @param keys attribute names to mark as guarded
     * @return this model for chaining
     */
    /**
     * Set the list of guarded attribute keys.
     *
     * @param keys attribute names to mark as guarded
     * @return this model for chaining
     */
    public EzModel setGuarded(String... keys) {
        guarded.clear();
        if (keys != null) for (String k : keys) if (k != null) guarded.add(k);
        return this;
    }

    /**
     * Get the set of fillable attribute keys.
     *
     * @return unmodifiable set of fillable keys
     */
    public Set<String> getFillable() { return Collections.unmodifiableSet(fillable); }

    /**
     * Get the set of guarded attribute keys.
     *
     * @return unmodifiable set of guarded keys
     */
    public Set<String> getGuarded() { return Collections.unmodifiableSet(guarded); }

    /**
     * Check whether a key may be mass-assigned via {@link #fill(Map)}.
     *
     * @param key attribute name to check
     * @return true if fillable
     */
    protected boolean isFillable(String key) {
        if ("id".equals(key)) return false;
        if (!fillable.isEmpty()) return fillable.contains(key);
        return !guarded.contains(key);
    }
    /**
     * Set a single attribute on the model.
     * If the {@code key} is {@code "id"} the model id will be changed.
     *
     * @param key   attribute name
     * @param value attribute value
     * @return this model for chaining
     */
    public EzModel set(String key, Object value) {
        if ("id".equals(key)) {
            setId(value == null ? null : value.toString());
        } else {
            attributes.put(key, value);
        }
        return this;
    }
    /**
     * Get the raw value for an attribute.
     *
     * @param key attribute name
     * @return attribute value or null
     */
    public Object get(String key) {
        if ("id".equals(key)) return getId();
        return attributes.get(key);
    }

    /**
     * Get the attribute converted to the requested type where possible.
     *
     * @param key attribute name
     * @param cls expected class
     * @param <T> expected type
     * @return value converted to {@code T}, or {@code null} if not present or not convertible
     */
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

    /**
     * Get the attribute converted to the requested type, returning a default
     * value if not present or not convertible.
     *
     * @param key attribute name
     * @param cls expected class
     * @param def default value to return when missing
     * @param <T> expected type
     * @return value converted to {@code T} or {@code def}
     */
    public <T> T getAs(String key, Class<T> cls, T def) {
        T v = getAs(key, cls);
        return v == null ? def : v;
    }

    public Map<String, Object> toMap() {
        return new HashMap<>(attributes);
    }

    /**
     * Populate attributes from a map. If the map contains an "id" key it
     * will be applied via {@link #setId(String)}.
     * @param map source attributes (may be null)
     */
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
     * @param attrs source attributes to apply
     * @return this model for chaining
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

    /**
     * Return an unmodifiable view of the model's attributes map.
     *
     * @return an unmodifiable view of the model's attributes
     */
    public Map<String, Object> attributes() { return Collections.unmodifiableMap(attributes); }

    /**
     * Convenience: start a `QueryBuilder` for storage queries.
     * @return a new `QueryBuilder` instance
     */
    public static com.skyblockexp.ezframework.query.QueryBuilder queryBuilder() {
        return new com.skyblockexp.ezframework.query.QueryBuilder();
    }

    /**
     * Persist this model using the given repository.
     * @param repo repository to save to
     * @param <T> model type
     * @return this model
     * @throws Exception when repository save fails
     */
    @SuppressWarnings("unchecked")
    public <T extends EzModel> T save(ModelRepository<T> repo) throws Exception {
        repo.save((T) this);
        return (T) this;
    }

    /**
     * Delete this model using the given repository.
     * @param repo repository to delete from
     * @param <T> model type
     * @throws Exception when repository delete fails
     */
    public <T extends EzModel> void delete(ModelRepository<T> repo) throws Exception {
        repo.delete(getId());
    }

    /**
     * Find a model by id using the repository (convenience static helper).
     * @param repo repository to query
     * @param id model id
     * @param <T> model type
     * @return found model or null
     * @throws Exception when repository lookup fails
     */
    public static <T extends EzModel> T find(ModelRepository<T> repo, String id) throws Exception {
        java.util.Optional<T> opt = repo.find(id);
        return opt.orElse(null);
    }
}
