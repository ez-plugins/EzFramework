package com.skyblockexp.ezframework.storage.provider.mysql;

import com.skyblockexp.ezframework.query.Condition;
import com.skyblockexp.ezframework.query.Query;
import com.skyblockexp.ezframework.query.QueryableStorage;
import com.skyblockexp.ezframework.storage.StorageProvider;
import com.skyblockexp.ezframework.storage.migration.MigrationCapable;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal MySQL-backed StorageProvider.
 *
 * Configuration (via plugin config):
 *  - mysql.host (default: localhost)
 *  - mysql.port (default: 3306)
 *  - mysql.database (required)
 *  - mysql.user (required)
 *  - mysql.password (optional)
 */
public class MysqlStorageProvider implements StorageProvider, MigrationCapable, QueryableStorage, com.skyblockexp.ezframework.storage.sql.JdbcStorage {
    private JavaPlugin plugin;
    private Connection conn;
    private String table = "ezframework_storage";

    // Optional programmatic overrides (if set these are used instead of plugin config)
    private String hostOverride;
    private Integer portOverride;
    private String databaseOverride;
    private String userOverride;
    private String passwordOverride;

    /**
     * Public no-arg constructor. The provider will read connection parameters
     * from the plugin config when {@link #init(Object)} is called.
     */
    public MysqlStorageProvider() {}

    // Test helper constructor: supply an existing Connection (e.g. H2 in-memory)
    /**
     * Construct a provider bound to an existing {@link Connection}. Useful for
     * testing with in-memory databases.
     *
     * @param connection pre-established JDBC connection
     */
    public MysqlStorageProvider(Connection connection) {
        this.conn = connection;
    }

    /**
     * Set the MySQL host to use when `init` is called. If not set, the plugin
     * config value `mysql.host` will be used.
     *
     * @param host host name or IP
     * @return this provider for chaining
     */
    public MysqlStorageProvider setHost(String host) {
        this.hostOverride = host;
        return this;
    }

    /**
     * Set the MySQL port to use when `init` is called. If not set, the plugin
     * config value `mysql.port` will be used.
     *
     * @param port TCP port number
     * @return this provider for chaining
     */
    public MysqlStorageProvider setPort(int port) {
        this.portOverride = port;
        return this;
    }

    /**
     * Set the database name to use; overrides `mysql.database` config.
     *
     * @param database database name
     * @return this provider for chaining
     */
    public MysqlStorageProvider setDatabase(String database) {
        this.databaseOverride = database;
        return this;
    }

    /**
     * Set the DB user to use; overrides `mysql.user` config.
     *
     * @param user username
     * @return this provider for chaining
     */
    public MysqlStorageProvider setUser(String user) {
        this.userOverride = user;
        return this;
    }

    /**
     * Set the DB password to use; overrides `mysql.password` config.
     *
     * @param password password (may be empty)
     * @return this provider for chaining
     */
    public MysqlStorageProvider setPassword(String password) {
        this.passwordOverride = password;
        return this;
    }

    /**
     * Optionally set the underlying table name used for storage. Default:
     * `ezframework_storage`.
     *
     * @param table table name
     * @return this provider for chaining
     */
    public MysqlStorageProvider setTable(String table) {
        if (table != null && !table.isEmpty()) this.table = table;
        return this;
    }

    @Override
    public String name() {
        return "mysql";
    }

    @Override
    public void executeSql(String sql) throws Exception {
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    @Override
    public void executeSqlStatements(java.util.List<String> statements) throws Exception {
        boolean origAuto = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                for (String s : statements) st.execute(s);
            }
            conn.commit();
        } catch (Exception ex) {
            try { conn.rollback(); } catch (Exception ignored) {}
            throw ex;
        } finally {
            try { conn.setAutoCommit(origAuto); } catch (Exception ignored) {}
        }
    }

    @Override
    public void init(Object plugin) throws Exception {
        JavaPlugin jp = (JavaPlugin) plugin;
        this.plugin = jp;
        // If a Connection was provided via constructor, reuse it. Otherwise
        // build connection parameters from programmatic overrides or plugin
        // config values (overrides win).
        if (this.conn == null) {
            String host = (hostOverride != null) ? hostOverride : jp.getConfig().getString("mysql.host", "localhost");
            int port = (portOverride != null) ? portOverride : jp.getConfig().getInt("mysql.port", 3306);
            String database = (databaseOverride != null) ? databaseOverride : jp.getConfig().getString("mysql.database", "");
            String user = (userOverride != null) ? userOverride : jp.getConfig().getString("mysql.user", "");
            String password = (passwordOverride != null) ? passwordOverride : jp.getConfig().getString("mysql.password", "");

            if (database == null || database.isEmpty() || user == null || user.isEmpty()) {
                throw new IllegalStateException("mysql.database and mysql.user must be set either via config or programmatic setters");
            }

            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", host, port, database);
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conn = DriverManager.getConnection(url, user, password);
        }

        // Ensure storage table exists (safe to call on existing connections)
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `" + table + "` (path VARCHAR(255) PRIMARY KEY, data LONGBLOB)");
        }
    }

    @Override
    public void close() throws Exception {
        if (conn != null && !conn.isClosed()) conn.close();
    }

    @Override
    public void save(String path, Map<String, Object> data) throws Exception {
        byte[] blob = serialize(data);
        String sql = "REPLACE INTO `" + table + "` (path, data) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, path);
            ps.setBytes(2, blob);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Map<String, Object>> load(String path) throws Exception {
        String sql = "SELECT data FROM `" + table + "` WHERE path = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, path);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                byte[] blob = rs.getBytes(1);
                Object obj = deserialize(blob);
                if (obj instanceof Map) {
                    //noinspection unchecked
                    return Optional.of(new HashMap<>((Map<String, Object>) obj));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public void delete(String path) throws Exception {
        String sql = "DELETE FROM `" + table + "` WHERE path = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, path);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean exists(String path) throws Exception {
        String sql = "SELECT 1 FROM `" + table + "` WHERE path = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, path);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> query(String sql, java.util.List<Object> params) throws Exception {
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) setPreparedParam(ps, i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                java.sql.ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                while (rs.next()) {
                    java.util.Map<String, Object> row = new java.util.HashMap<>();
                    for (int i = 1; i <= cols; i++) {
                            String name = md.getColumnLabel(i);
                            Object val = rs.getObject(i);
                            // normalize column labels to lower-case for portability (H2 returns upper-case)
                            row.put(name == null ? null : name.toLowerCase(), val);
                        }
                    out.add(row);
                }
            }
        }
        return out;
    }

    @Override
    public int executeUpdate(String sql, java.util.List<Object> params) throws Exception {
        // Some test DBs (H2) don't support MySQL's `ON DUPLICATE KEY UPDATE` syntax
        // Translate to a H2-friendly MERGE INTO ... KEY(id) VALUES(...) form when needed.
        String execSql = sql;
        try {
            String product = conn.getMetaData().getDatabaseProductName();
            if (product != null && product.toLowerCase().contains("h2") && sql != null && sql.toUpperCase().contains("ON DUPLICATE KEY UPDATE")) {
                // Try to parse: INSERT INTO `table` (col1,col2,...) VALUES (?,?,...)
                String up = sql;
                int intoIdx = up.toUpperCase().indexOf("INTO");
                int valuesIdx = up.toUpperCase().indexOf("VALUES");
                if (intoIdx >= 0 && valuesIdx > intoIdx) {
                    String between = up.substring(intoIdx + 4, valuesIdx).trim(); // `table` (cols)
                    // extract table name and columns
                    int firstParen = between.indexOf('(');
                    String tablePart;
                    String colsPart;
                    if (firstParen >= 0) {
                        tablePart = between.substring(0, firstParen).trim();
                        colsPart = between.substring(firstParen + 1, between.lastIndexOf(')')).trim();
                    } else {
                        // fallback, use original SQL
                        tablePart = between.trim();
                        colsPart = "";
                    }
                    // clean backticks and whitespace
                    String tableName = tablePart.replace("`", "").trim();
                    String colsClean = colsPart.replace("`", "").trim();
                    execSql = "MERGE INTO " + tableName + " (" + colsClean + ") KEY(id) VALUES (";
                    // determine placeholders count from original VALUES(...) segment
                    int vOpen = up.indexOf('(', valuesIdx);
                    int vClose = up.indexOf(')', vOpen);
                    if (vOpen >= 0 && vClose > vOpen) {
                        String placeholders = up.substring(vOpen + 1, vClose).trim();
                        execSql += placeholders + ")";
                    } else {
                        // fallback: keep original
                        execSql = sql;
                    }
                }
            }
        } catch (Exception ignore) {
            // if anything goes wrong, fall back to original SQL
            execSql = sql;
        }

        try (java.sql.PreparedStatement ps = conn.prepareStatement(execSql)) {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) setPreparedParam(ps, i + 1, params.get(i));
            }
            return ps.executeUpdate();
        }
    }

    private static void setPreparedParam(java.sql.PreparedStatement ps, int idx, Object v) throws java.sql.SQLException {
        if (v == null) { ps.setObject(idx, null); return; }
        if (v instanceof Integer) ps.setInt(idx, (Integer) v);
        else if (v instanceof Long) ps.setLong(idx, (Long) v);
        else if (v instanceof Boolean) ps.setBoolean(idx, (Boolean) v);
        else if (v instanceof byte[]) ps.setBytes(idx, (byte[]) v);
        else if (v instanceof Float) ps.setFloat(idx, (Float) v);
        else if (v instanceof Double) ps.setDouble(idx, (Double) v);
        else ps.setString(idx, v.toString());
    }

    @Override
    public List<String> query(Query q) throws Exception {
        List<String> out = new ArrayList<>();
        String sql = "SELECT path, data FROM `" + table + "`";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String path = rs.getString(1);
                byte[] blob = rs.getBytes(2);
                Object obj = deserialize(blob);
                if (!(obj instanceof Map)) continue;
                @SuppressWarnings("unchecked") Map<String, Object> map = new HashMap<>((Map<String, Object>) obj);
                boolean ok = true;
                for (Map.Entry<String, com.skyblockexp.ezframework.query.Condition> e : q.getConditions().entrySet()) {
                    String key = e.getKey();
                    Condition c = e.getValue();
                    if (!c.matches(map, key)) { ok = false; break; }
                }
                if (ok) out.add(path);
                if (q.getLimit() != null && out.size() >= q.getLimit()) break;
            }
        }
        return out;
    }

    private static byte[] serialize(Object o) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
            oos.flush();
            return bos.toByteArray();
        }
    }

    private static Object deserialize(byte[] data) throws Exception {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data); ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        }
    }
}
