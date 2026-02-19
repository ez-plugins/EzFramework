package com.skyblockexp.ezframework.storage.migration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SqlSplitterTest {
    @Test
    public void splitSimple() {
        String sql = "CREATE TABLE a (id INT); INSERT INTO a (id) VALUES (1);";
        List<String> parts = SqlSplitter.split(sql);
        assertEquals(2, parts.size());
        assertTrue(parts.get(0).startsWith("CREATE TABLE"));
    }

    @Test
    public void ignoreSemicolonInString() {
        String sql = "INSERT INTO t (txt) VALUES ('semicolon;inside'); UPDATE t SET txt='ok';";
        List<String> parts = SqlSplitter.split(sql);
        assertEquals(2, parts.size());
    }
}
