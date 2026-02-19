package com.skyblockexp.ezframework.storage.provider.mysql;

import com.skyblockexp.ezframework.storage.model.EzModel;
import com.skyblockexp.ezframework.storage.model.ModelRepository;
import com.skyblockexp.ezframework.storage.model.ModelTableRegistry;
import com.skyblockexp.ezframework.storage.migration.Schema;
import com.skyblockexp.ezframework.query.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MysqlProviderModelTest {
    private Connection conn;
    private MysqlStorageProvider provider;

    @BeforeEach
    public void setup() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        provider = new MysqlStorageProvider(conn);

        // create players table (adjust DDL for H2 compatibility)
        String ddl = "CREATE TABLE IF NOT EXISTS players (id VARCHAR(255) PRIMARY KEY, name VARCHAR(191) NOT NULL, coins INT NOT NULL);";
        provider.executeSql(ddl);

        // register mapping
        Map<String, String> cols = new HashMap<>();
        cols.put("id", "VARCHAR(255)");
        cols.put("name", "VARCHAR(191)");
        cols.put("coins", "INT");
        ModelTableRegistry.register("players", "players", cols);
    }

    @AfterEach
    public void teardown() throws Exception {
        if (provider != null) provider.close();
        if (conn != null && !conn.isClosed()) conn.close();
    }

    public static class PlayerData extends EzModel {
        public PlayerData(String id) { super(id); }
        public PlayerData() { super(null); setFillable("name", "coins"); }
        public String getName() { return getAs("name", String.class); }
        public void setName(String n) { set("name", n); }
        public int getCoins() { return getAs("coins", Integer.class, 0); }
        public void setCoins(int c) { set("coins", c); }
    }

    @Test
    public void testSaveFindQuery() throws Exception {
        ModelRepository<PlayerData> repo = new ModelRepository<>(provider, "players", (id, data) -> new PlayerData(id));

        PlayerData p = new PlayerData("p1");
        p.setName("Alice");
        p.setCoins(100);
        p.save(repo);

        

        Assertions.assertTrue(repo.exists("p1"));

        PlayerData loaded = repo.find("p1").orElse(null);
        Assertions.assertNotNull(loaded);
        Assertions.assertEquals("Alice", loaded.getName());
        Assertions.assertEquals(100, loaded.getCoins());

        Query q = PlayerData.queryBuilder().whereEquals("coins", 100).limit(10).build();
        List<PlayerData> res = repo.query(q);
        Assertions.assertEquals(1, res.size());
        Assertions.assertEquals("p1", res.get(0).getId());
    }
}
