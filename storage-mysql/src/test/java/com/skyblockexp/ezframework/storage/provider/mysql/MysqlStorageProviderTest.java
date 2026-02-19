package com.skyblockexp.ezframework.storage.provider.mysql;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MysqlStorageProviderTest {

    private Connection conn;

    private Connection getConn() throws Exception {
        if (conn == null) conn = DriverManager.getConnection("jdbc:h2:mem:msp;DB_CLOSE_DELAY=-1;MODE=MySQL");
        return conn;
    }

    @AfterEach
    public void close() throws Exception {
        if (conn != null && !conn.isClosed()) conn.close();
        conn = null;
    }

    @Test
    public void save_load_delete_exists_flow() throws Exception {
        MysqlStorageProvider p = new MysqlStorageProvider(getConn());
        p.setTable("msp_test");

        // no init required when connection provided; ensure table exists via init by passing a dummy plugin
        p.executeSql("CREATE TABLE IF NOT EXISTS msp_test (path VARCHAR(255) PRIMARY KEY, data BLOB)");

        p.save("k1", Map.of("v", "x"));
        assertTrue(p.exists("k1"));

        var got = p.load("k1");
        assertTrue(got.isPresent());
        assertEquals("x", got.get().get("v"));

        p.delete("k1");
        assertFalse(p.exists("k1"));
    }
}
