package com.skyblockexp.ezframework.storage.provider.mysql;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

public class DataSourceFactoryTest {

    @Test
    public void fromJdbc_returnsDataSource() throws Exception {
        // use H2 in-memory URL to validate DataSource creation
        String url = "jdbc:h2:mem:dsf;DB_CLOSE_DELAY=-1";
        DataSource ds = DataSourceFactory.fromJdbc(url, "sa", "", 2, 1000L);
        try (Connection c = ds.getConnection()) {
            assertNotNull(c);
            assertFalse(c.isClosed());
        }
    }
}
