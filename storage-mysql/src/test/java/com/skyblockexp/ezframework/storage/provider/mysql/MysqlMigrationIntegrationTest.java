package com.skyblockexp.ezframework.storage.provider.mysql;

import com.skyblockexp.ezframework.storage.migration.SqlSplitter;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MysqlMigrationIntegrationTest {
    @Test
    public void applySqlStatementsTransaction() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:migration_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "")) {
            MysqlStorageProvider p = new MysqlStorageProvider(conn);
            String sql = "CREATE TABLE test_mig (id INT PRIMARY KEY); INSERT INTO test_mig (id) VALUES (1);";
            List<String> stmts = SqlSplitter.split(sql);
            p.executeSqlStatements(stmts);

            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM test_mig")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        }
    }
}
