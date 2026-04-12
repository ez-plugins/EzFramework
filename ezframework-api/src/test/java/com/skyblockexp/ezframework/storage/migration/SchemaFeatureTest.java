package com.skyblockexp.ezframework.storage.migration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaFeatureTest {

    // -------------------------------------------------------------------------
    // table()
    // -------------------------------------------------------------------------

    @Test
    void tableReturnsSchemInstance() {
        Schema s = Schema.table("players");
        assertNotNull(s);
    }

    // -------------------------------------------------------------------------
    // id() — default primary key
    // -------------------------------------------------------------------------

    @Test
    void idColumnGeneratesPrimaryKey() {
        String sql = Schema.table("players").id().toCreateSql();
        assertTrue(sql.contains("PRIMARY KEY"), "should contain PRIMARY KEY clause");
        assertTrue(sql.contains("`id`"), "should contain id column");
        assertTrue(sql.contains("VARCHAR(255)"), "id should be VARCHAR(255)");
    }

    // -------------------------------------------------------------------------
    // string()
    // -------------------------------------------------------------------------

    @Test
    void stringColumnNotNull() {
        String sql = Schema.table("t").id().string("name", 100, true).toCreateSql();
        assertTrue(sql.contains("`name` VARCHAR(100) NOT NULL"), "string(notNull=true) should add NOT NULL");
    }

    @Test
    void stringColumnNullable() {
        String sql = Schema.table("t").id().string("name", 100, false).toCreateSql();
        assertTrue(sql.contains("`name` VARCHAR(100)"), "string(notNull=false) should NOT add NOT NULL");
        assertFalse(sql.replaceAll(".*`name` VARCHAR\\(100\\)", "").startsWith(" NOT NULL"));
    }

    @Test
    void stringColumnCustomLength() {
        String sql = Schema.table("t").id().string("bio", 500, false).toCreateSql();
        assertTrue(sql.contains("VARCHAR(500)"));
    }

    // -------------------------------------------------------------------------
    // integer()
    // -------------------------------------------------------------------------

    @Test
    void integerColumnNotNull() {
        String sql = Schema.table("t").id().integer("age", true).toCreateSql();
        assertTrue(sql.contains("`age` INT NOT NULL"));
    }

    @Test
    void integerColumnNullable() {
        String sql = Schema.table("t").id().integer("age", false).toCreateSql();
        assertTrue(sql.contains("`age` INT"));
        // should not contain NOT NULL for age column specifically
        String afterAge = sql.substring(sql.indexOf("`age` INT") + "`age` INT".length());
        assertFalse(afterAge.startsWith(" NOT NULL"), "nullable integer should not have NOT NULL");
    }

    // -------------------------------------------------------------------------
    // text()
    // -------------------------------------------------------------------------

    @Test
    void textColumnNotNull() {
        String sql = Schema.table("t").id().text("body", true).toCreateSql();
        assertTrue(sql.contains("`body` TEXT NOT NULL"));
    }

    @Test
    void textColumnNullable() {
        String sql = Schema.table("t").id().text("body", false).toCreateSql();
        assertTrue(sql.contains("`body` TEXT"));
    }

    // -------------------------------------------------------------------------
    // binary()
    // -------------------------------------------------------------------------

    @Test
    void binaryColumnIsLongblob() {
        String sql = Schema.table("t").id().binary("data").toCreateSql();
        assertTrue(sql.contains("`data` LONGBLOB"));
    }

    // -------------------------------------------------------------------------
    // bool()
    // -------------------------------------------------------------------------

    @Test
    void boolColumnNotNull() {
        String sql = Schema.table("t").id().bool("active", true).toCreateSql();
        assertTrue(sql.contains("`active` TINYINT(1) NOT NULL"));
    }

    @Test
    void boolColumnNullable() {
        String sql = Schema.table("t").id().bool("active", false).toCreateSql();
        assertTrue(sql.contains("`active` TINYINT(1)"));
    }

    // -------------------------------------------------------------------------
    // toCreateSql() — default dialect (MySQL)
    // -------------------------------------------------------------------------

    @Test
    void toCreateSqlDefaultUsesMySQL() {
        String sql = Schema.table("players").id().string("name", 60, true).toCreateSql();
        assertTrue(sql.contains("ENGINE=InnoDB"), "default dialect should use MySQL ENGINE clause");
        assertTrue(sql.contains("utf8mb4"), "default dialect should use utf8mb4 charset");
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS `players`"));
    }

    @Test
    void toCreateSqlExplicitMysql() {
        String mysql = Schema.table("t").id().toCreateSql(Schema.Dialect.MYSQL);
        assertTrue(mysql.contains("ENGINE=InnoDB"));
        assertTrue(mysql.contains("`t`"));
    }

    @Test
    void toCreateSqlH2Dialect() {
        String h2 = Schema.table("t").id().toCreateSql(Schema.Dialect.H2);
        assertFalse(h2.contains("ENGINE=InnoDB"), "H2 dialect should not include ENGINE clause");
        assertTrue(h2.contains("CREATE TABLE IF NOT EXISTS t"), "H2 should not backtick-quote table name");
        assertTrue(h2.endsWith(";"), "should end with semicolon");
    }

    // -------------------------------------------------------------------------
    // Full schema with all column types
    // -------------------------------------------------------------------------

    @Test
    void fullSchemaAllColumnTypes() {
        String sql = Schema.table("items")
                .id()
                .string("name", 255, true)
                .integer("count", false)
                .text("description", false)
                .binary("blob_data")
                .bool("enabled", true)
                .toCreateSql();
        assertTrue(sql.contains("`id`"));
        assertTrue(sql.contains("`name` VARCHAR(255) NOT NULL"));
        assertTrue(sql.contains("`count` INT"));
        assertTrue(sql.contains("`description` TEXT"));
        assertTrue(sql.contains("`blob_data` LONGBLOB"));
        assertTrue(sql.contains("`enabled` TINYINT(1) NOT NULL"));
        assertTrue(sql.contains("PRIMARY KEY (`id`)"));
    }

    @Test
    void schemaWithoutIdHasNoPrimaryKey() {
        String sql = Schema.table("log").string("msg", 255, false).toCreateSql();
        assertFalse(sql.contains("PRIMARY KEY"), "no id() call should produce no PRIMARY KEY clause");
    }
}
