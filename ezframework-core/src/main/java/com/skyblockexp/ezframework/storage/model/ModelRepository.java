package com.skyblockexp.ezframework.storage.model;

import com.skyblockexp.ezframework.storage.StorageProvider;
import com.skyblockexp.ezframework.storage.migration.MigrationCapable;
import com.skyblockexp.ezframework.storage.sql.JdbcStorage;
import com.skyblockexp.ezframework.storage.model.ModelTableRegistry.TableMeta;
import com.skyblockexp.ezframework.query.Query;
import com.skyblockexp.ezframework.query.QueryableStorage;

import java.util.Map;
import java.util.Optional;

/**
 * Generic repository for storing and retrieving `Model` instances using a
 * `StorageProvider`.
 *
 * @param <T> model type
 */
public class ModelRepository<T extends Model> {
    private final StorageProvider provider;
    private final String prefix;
    private final ModelFactory<T> factory;

    /**
     * Create a new repository backed by the given provider.
     *
     * @param provider storage provider to use
     * @param prefix   optional storage prefix (may be null)
     * @param factory  factory to instantiate models
     */
    public ModelRepository(StorageProvider provider, String prefix, ModelFactory<T> factory) {
        this.provider = provider;
        this.prefix = (prefix == null) ? "" : prefix;
        this.factory = factory;
    }

    private String storagePath(String id) {
        if (prefix.isEmpty()) return id;
        return prefix + "/" + id;
    }

    /**
     * Persist the given model using the configured storage provider.
     *
     * @param model model to persist
     * @throws Exception when persistence fails
     */
    public void save(T model) throws Exception {
        // If a SQL table is registered for this prefix and the provider offers
        // JDBC operations, persist into columns instead of default storage.
        TableMeta meta = ModelTableRegistry.get(prefix);
        if (meta != null && provider instanceof JdbcStorage) {
            JdbcStorage jdbc = (JdbcStorage) provider;
            Map<String, Object> values = model.toMap();
            StringBuilder cols = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();
            StringBuilder updates = new StringBuilder();
            java.util.List<Object> params = new java.util.ArrayList<>();
            // ensure id column
            cols.append("id");
            placeholders.append("?");
            params.add(model.getId());
            for (Map.Entry<String, String> col : meta.columns().entrySet()) {
                String cname = col.getKey();
                if ("id".equals(cname)) continue;
                cols.append(',').append(cname);
                placeholders.append(',').append('?');
                Object v = values.get(cname);
                params.add(v);
                updates.append(cname+"=VALUES("+cname+"),");
            }
            String upsert = String.format("INSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s", meta.tableName(), cols.toString(), placeholders.toString(), updates.length() > 0 ? updates.substring(0, updates.length()-1) : "id=id");
            jdbc.executeUpdate(upsert, params);
            return;
        }
        provider.save(model.getStoragePath(prefix), model.toMap());
    }

    /**
     * Find a model by id.
     *
     * @param id model identifier
     * @return optional model instance
     * @throws Exception on storage errors
     */
    public Optional<T> find(String id) throws Exception {
        TableMeta meta = ModelTableRegistry.get(prefix);
        if (meta != null && provider instanceof JdbcStorage) {
            JdbcStorage jdbc = (JdbcStorage) provider;
            String sql = String.format("SELECT * FROM %s WHERE id = ? LIMIT 1", meta.tableName());
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(id);
            java.util.List<java.util.Map<String, Object>> rows = jdbc.query(sql, params);
            if (rows.isEmpty()) return Optional.empty();
            java.util.Map<String, Object> row = rows.get(0);
            T m = factory.create(id, row);
            m.fromMap(row);
            return Optional.of(m);
        }
        Optional<Map<String, Object>> opt = provider.load(storagePath(id));
        if (!opt.isPresent()) return Optional.empty();
        T m = factory.create(id, opt.get());
        m.fromMap(opt.get());
        return Optional.of(m);
    }

    /**
     * Delete the model with the given id.
     *
     * @param id model identifier
     * @throws Exception on storage errors
     */
    public void delete(String id) throws Exception {
        TableMeta meta = ModelTableRegistry.get(prefix);
        if (meta != null && provider instanceof JdbcStorage) {
            JdbcStorage jdbc = (JdbcStorage) provider;
            String sql = String.format("DELETE FROM %s WHERE id = ?", meta.tableName());
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(id);
            jdbc.executeUpdate(sql, params);
            return;
        }
        provider.delete(storagePath(id));
    }

    /**
     * Check existence of a model id.
     *
     * @param id model identifier
     * @return true if the id exists
     * @throws Exception on storage errors
     */
    public boolean exists(String id) throws Exception {
        TableMeta meta = ModelTableRegistry.get(prefix);
        if (meta != null && provider instanceof JdbcStorage) {
            JdbcStorage jdbc = (JdbcStorage) provider;
            String sql = String.format("SELECT 1 FROM `%s` WHERE id = ? LIMIT 1", meta.tableName());
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(id);
            java.util.List<java.util.Map<String, Object>> rows = jdbc.query(sql, params);
            return !rows.isEmpty();
        }
        return provider.exists(storagePath(id));
    }

    /**
     * Query for models matching the given Query. Returns instantiated models.
     *
     * @param q query to execute
     * @return list of matching models (may be empty)
     * @throws Exception on storage or query errors
     */
    public java.util.List<T> query(Query q) throws Exception {
        java.util.List<T> out = new java.util.ArrayList<>();
        TableMeta meta = ModelTableRegistry.get(prefix);
        if (meta != null && provider instanceof JdbcStorage) {
            JdbcStorage jdbc = (JdbcStorage) provider;
            // Build simple WHERE clause from Query conditions (EQ, LIKE, EXISTS, GT, LT)
            StringBuilder where = new StringBuilder();
            java.util.List<Object> params = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, com.skyblockexp.ezframework.query.Condition> e : q.getConditions().entrySet()) {
                String key = e.getKey();
                com.skyblockexp.ezframework.query.Condition c = e.getValue();
                if (!meta.columns().containsKey(key)) continue; // ignore unknown columns
                if (where.length() > 0) where.append(" AND ");
                switch (c.getOperator()) {
                    case EQ:
                        where.append(key+" = ?");
                        params.add(c.getValue());
                        break;
                    case NEQ:
                        where.append(key+" <> ?");
                        params.add(c.getValue());
                        break;
                    case LIKE:
                        where.append(key+" LIKE ?");
                        params.add("%" + (c.getValue() == null ? "" : c.getValue().toString()) + "%");
                        break;
                    case EXISTS:
                        where.append(key+" IS NOT NULL");
                        break;
                    case GT:
                        where.append(key+" > ?");
                        params.add(c.getValue());
                        break;
                    case LT:
                        where.append(key+" < ?");
                        params.add(c.getValue());
                        break;
                    case IN:
                        if (c.getValue() instanceof java.util.Collection) {
                            java.util.Collection<?> col = (java.util.Collection<?>) c.getValue();
                            if (col.isEmpty()) { where.append("1=0"); break; }
                            where.append(key+" IN (");
                            int i = 0;
                            for (Object v : col) { if (i++ > 0) where.append(","); where.append("?"); params.add(v); }
                            where.append(")");
                        } else {
                            where.append("`"+key+"` = ?"); params.add(c.getValue());
                        }
                        break;
                    case BETWEEN:
                        if (c.getValue() instanceof java.util.List) {
                            java.util.List<?> pair = (java.util.List<?>) c.getValue();
                            if (pair.size() >= 2) {
                                where.append(key+" BETWEEN ? AND ?"); params.add(pair.get(0)); params.add(pair.get(1));
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            String sql = "SELECT * FROM " + meta.tableName() + (where.length() > 0 ? (" WHERE "+where.toString()) : "");
            if (q.getLimit() != null) sql += " LIMIT " + q.getLimit();
            if (q.getOffset() != null) sql += " OFFSET " + q.getOffset();
            java.util.List<java.util.Map<String, Object>> rows = jdbc.query(sql, params);
            for (java.util.Map<String, Object> row : rows) {
                Object idVal = row.get("id");
                String idStr = idVal == null ? null : idVal.toString();
                T m = factory.create(idStr, row);
                m.fromMap(row);
                out.add(m);
            }
            return out;
        }

        // Fallback: if provider supports QueryableStorage, use it to obtain paths
        if (provider instanceof QueryableStorage) {
            java.util.List<String> ids = ((QueryableStorage) provider).query(q);
            for (String id : ids) {
                Optional<T> found = find(id);
                found.ifPresent(out::add);
            }
            return out;
        }

        // No query support: return empty list
        return out;
    }

    private static String escape(String s) { if (s == null) return ""; return s.replace("\"","\\\"").replace("'","''"); }

    private static String quote(Object o) {
        if (o == null) return "NULL";
        if (o instanceof Number) return o.toString();
        if (o instanceof Boolean) return ((Boolean)o) ? "1" : "0";
        String s = o.toString().replace("'","''");
        return "'" + s + "'";
    }
}
