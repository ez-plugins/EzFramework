package com.skyblockexp.ezframework.storage.model;

import com.skyblockexp.ezframework.storage.StorageProvider;
import com.skyblockexp.ezframework.storage.sql.JdbcStorage;
import com.skyblockexp.ezframework.query.Query;
import com.skyblockexp.ezframework.query.QueryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ModelRepositoryJdbcIntegrationTest {

    static class TestDbModel extends EzModel {
        TestDbModel(String id) { super(id); }
        TestDbModel(String id, Map<String, Object> attrs) { super(id, attrs); }
        public void setName(String n) { set("name", n); }
        public String getName() { return getAs("name", String.class); }
        public void setScore(int s) { set("score", s); }
        public Integer getScore() { return getAs("score", Integer.class); }
    }

    static class H2Provider implements StorageProvider, JdbcStorage {
        private final Connection conn;

        H2Provider(String dbName) throws Exception {
            String url = "jdbc:h2:mem:" + dbName + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
            this.conn = DriverManager.getConnection(url);
        }

        Connection conn() { return conn; }

        @Override
        public String name() { return "h2-jdbc-provider"; }

        @Override
        public void init(org.bukkit.plugin.java.JavaPlugin plugin) { }

        @Override
        public void close() throws Exception { conn.close(); }

        @Override
        public void save(String path, Map<String, Object> data) throws Exception { /* not used in JDBC path */ }

        @Override
        public Optional<Map<String, Object>> load(String path) throws Exception { return Optional.empty(); }

        @Override
        public void delete(String path) throws Exception { /* not used */ }

        @Override
        public boolean exists(String path) throws Exception { return false; }

        @Override
        public List<Map<String, Object>> query(String sql, List<Object> params) throws Exception {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                setParams(ps, params);
                ResultSet rs = ps.executeQuery();
                List<Map<String, Object>> out = new ArrayList<>();
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= cols; i++) {
                            String col = md.getColumnLabel(i);
                            Object v = rs.getObject(i);
                            // normalize to lower-case keys so EzModel/fromMap can find them
                            row.put(col == null ? null : col.toLowerCase(), v);
                        }
                    out.add(row);
                }
                return out;
            }
        }

        @Override
        public int executeUpdate(String sql, List<Object> params) throws Exception {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                setParams(ps, params);
                return ps.executeUpdate();
            }
        }

        private void setParams(PreparedStatement ps, List<Object> params) throws Exception {
            if (params == null) return;
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i+1, params.get(i));
            }
        }
    }

    private H2Provider provider;
    private String prefix;

    @BeforeEach
    public void setup() throws Exception {
        // unique DB per test run
        String db = "db" + UUID.randomUUID().toString().replace('-', '_');
        provider = new H2Provider(db);
        prefix = "p" + UUID.randomUUID().toString().replace('-', '_');

        // register table metadata
        Map<String, String> cols = new HashMap<>();
        cols.put("id", "VARCHAR(255)");
        cols.put("name", "VARCHAR(255)");
        cols.put("score", "INT");
        ModelTableRegistry.register(prefix, "test_table_" + prefix, cols);

        // create table
        String ddl = String.format("CREATE TABLE %s (id VARCHAR(255) PRIMARY KEY, name VARCHAR(255), score INT)", ModelTableRegistry.get(prefix).tableName());
        provider.conn().createStatement().execute(ddl);
    }

    @AfterEach
    public void tearDown() throws Exception {
        provider.close();
    }

    @Test
    public void jdbcCrud_flow_through_model_and_repository() throws Exception {
        ModelFactory<TestDbModel> factory = (id, data) -> new TestDbModel(id);
        ModelRepository<TestDbModel> repo = new ModelRepository<>(provider, prefix, factory);

        TestDbModel m = new TestDbModel("u1");
        m.setName("Alice");
        m.setScore(42);

        repo.save(m);
        assertTrue(repo.exists("u1"));

        Optional<TestDbModel> opt = repo.find("u1");
        assertTrue(opt.isPresent());
        TestDbModel got = opt.get();
        assertEquals("Alice", got.getName());
        assertEquals(Integer.valueOf(42), got.getScore());

        repo.delete("u1");
        assertFalse(repo.exists("u1"));
    }

    @Test
    public void queryBuilder_conditions_supported_in_repository() throws Exception {
        ModelFactory<TestDbModel> factory = (id, data) -> new TestDbModel(id);
        ModelRepository<TestDbModel> repo = new ModelRepository<>(provider, prefix, factory);

        TestDbModel a = new TestDbModel("a"); a.setName("Alice"); a.setScore(10); repo.save(a);
        TestDbModel b = new TestDbModel("b"); b.setName("Bob"); b.setScore(20); repo.save(b);
        TestDbModel c = new TestDbModel("c"); c.setName("Charlie"); c.setScore(30); repo.save(c);

        // EQ
        Query q1 = new QueryBuilder().whereEquals("name","Bob").build();
        assertEquals(1, repo.query(q1).size());

        // LIKE
        Query q2 = new QueryBuilder().whereLike("name","li").build();
        assertEquals(2, repo.query(q2).size()); // Alice, Charlie

        // GT
        Query q3 = new QueryBuilder().whereEquals("score", 20).build();
        assertEquals(1, repo.query(q3).size());

        // IN
        Query q4 = new QueryBuilder().whereIn("id", Arrays.asList("a","c")).build();
        assertEquals(2, repo.query(q4).size());

        // BETWEEN
        Query q5 = new QueryBuilder().whereBetween("score", 15, 30).build();
        assertEquals(2, repo.query(q5).size());

        // limit/offset
        Query q6 = new QueryBuilder().limit(1).offset(1).build();
        assertEquals(1, repo.query(q6).size());
    }
}
