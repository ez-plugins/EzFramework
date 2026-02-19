package com.skyblockexp.ezframework.storage.migration;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight SQL splitter that splits statements on semicolons while
 * respecting quoted strings and block/line comments.
 */
public final class SqlSplitter {
    private SqlSplitter() {}

    public static List<String> split(String sql) {
        List<String> out = new ArrayList<>();
        if (sql == null || sql.isEmpty()) return out;
        StringBuilder cur = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            char next = (i + 1 < sql.length()) ? sql.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n') inLineComment = false;
                cur.append(c);
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') { inBlockComment = false; cur.append(c); cur.append(next); i++; continue; }
                cur.append(c);
                continue;
            }

            if (!inSingle && !inDouble) {
                if (c == '-' && next == '-') { inLineComment = true; cur.append(c); continue; }
                if (c == '/' && next == '*') { inBlockComment = true; cur.append(c); continue; }
            }

            if (c == '\'' && !inDouble) { inSingle = !inSingle; cur.append(c); continue; }
            if (c == '"' && !inSingle) { inDouble = !inDouble; cur.append(c); continue; }

            if (c == ';' && !inSingle && !inDouble && !inBlockComment && !inLineComment) {
                String s = cur.toString().trim();
                if (!s.isEmpty()) out.add(s);
                cur.setLength(0);
                continue;
            }

            cur.append(c);
        }
        String s = cur.toString().trim();
        if (!s.isEmpty()) out.add(s);
        return out;
    }
}
