package com.skyblockexp.ezframework.storage.migration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SqlSplitterFeatureTest {

    @Test
    void nullInputReturnsEmptyList() {
        List<String> result = SqlSplitter.split(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void emptyStringReturnsEmptyList() {
        assertTrue(SqlSplitter.split("").isEmpty());
    }

    @Test
    void singleStatementNoPunctuation() {
        List<String> result = SqlSplitter.split("SELECT 1");
        assertEquals(1, result.size());
        assertEquals("SELECT 1", result.get(0));
    }

    @Test
    void singleStatementWithSemicolon() {
        List<String> result = SqlSplitter.split("SELECT 1;");
        assertEquals(1, result.size());
        assertEquals("SELECT 1", result.get(0));
    }

    @Test
    void multipleStatements() {
        List<String> result = SqlSplitter.split("SELECT 1; SELECT 2; SELECT 3;");
        assertEquals(3, result.size());
        assertEquals("SELECT 1", result.get(0));
        assertEquals("SELECT 2", result.get(1));
        assertEquals("SELECT 3", result.get(2));
    }

    @Test
    void semicolonInsideSingleQuoteIsNotSplit() {
        List<String> result = SqlSplitter.split("SELECT 'hello;world'");
        assertEquals(1, result.size());
        assertEquals("SELECT 'hello;world'", result.get(0));
    }

    @Test
    void semicolonInsideDoubleQuoteIsNotSplit() {
        List<String> result = SqlSplitter.split("SELECT \"hello;world\"");
        assertEquals(1, result.size());
        assertEquals("SELECT \"hello;world\"", result.get(0));
    }

    @Test
    void lineCommentIsPreservedInStatement() {
        String sql = "SELECT 1 -- comment\n; SELECT 2;";
        List<String> result = SqlSplitter.split(sql);
        assertEquals(2, result.size());
        assertTrue(result.get(0).contains("-- comment"));
        assertEquals("SELECT 2", result.get(1));
    }

    @Test
    void blockCommentIsPreservedInStatement() {
        String sql = "SELECT /* inline */ 1; SELECT 2;";
        List<String> result = SqlSplitter.split(sql);
        assertEquals(2, result.size());
        assertTrue(result.get(0).contains("/* inline */"));
    }

    @Test
    void onlyWhitespaceAndSemicolonsProducesEmptyList() {
        assertTrue(SqlSplitter.split("  ;  ;  ").isEmpty());
    }

    @Test
    void resultIsTrimmable() {
        List<String> result = SqlSplitter.split("  SELECT 1  ;  SELECT 2  ;");
        assertEquals(2, result.size());
        assertEquals("SELECT 1", result.get(0));
        assertEquals("SELECT 2", result.get(1));
    }

    @Test
    void createTableStatement() {
        String sql = "CREATE TABLE IF NOT EXISTS `users` (`id` VARCHAR(255) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        List<String> result = SqlSplitter.split(sql);
        assertEquals(1, result.size());
        assertTrue(result.get(0).startsWith("CREATE TABLE"));
    }

    @Test
    void multipleCreateTableStatements() {
        String sql = "CREATE TABLE `a` (`id` VARCHAR(255) NOT NULL);\nCREATE TABLE `b` (`id` VARCHAR(255) NOT NULL);";
        List<String> result = SqlSplitter.split(sql);
        assertEquals(2, result.size());
        assertTrue(result.get(0).startsWith("CREATE TABLE `a`"));
        assertTrue(result.get(1).startsWith("CREATE TABLE `b`"));
    }
}
