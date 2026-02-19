package com.skyblockexp.ezframework.storage.provider.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * Small helper to build a HikariCP DataSource from plugin config or explicit params.
 */
public final class DataSourceFactory {
    private DataSourceFactory() {}

    public static DataSource fromConfig(JavaPlugin plugin) {
        String host = plugin.getConfig().getString("mysql.host", "localhost");
        int port = plugin.getConfig().getInt("mysql.port", 3306);
        String database = Objects.requireNonNull(plugin.getConfig().getString("mysql.database", ""));
        String user = Objects.requireNonNull(plugin.getConfig().getString("mysql.user", ""));
        String password = plugin.getConfig().getString("mysql.password", "");

        int maxPool = plugin.getConfig().getInt("mysql.pool.maximumPoolSize", 10);
        long connTimeout = plugin.getConfig().getLong("mysql.pool.connectionTimeoutMs", 30000L);

        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", host, port, database);
        return fromJdbc(jdbcUrl, user, password, maxPool, connTimeout);
    }

    public static DataSource fromJdbc(String jdbcUrl, String user, String password, int maximumPoolSize, long connectionTimeoutMs) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setUsername(user);
        cfg.setPassword(password == null ? "" : password);
        cfg.setMaximumPoolSize(Math.max(1, maximumPoolSize));
        cfg.setConnectionTimeout(Math.max(1000L, connectionTimeoutMs));
        cfg.setPoolName("ezframework-mysql-pool");
        // sensible defaults for plugin environments
        cfg.setLeakDetectionThreshold(60_000L);
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(cfg);
    }

    public static DataSource fromJdbc(String jdbcUrl, String user, String password) {
        return fromJdbc(jdbcUrl, user, password, 10, 30_000L);
    }
}
