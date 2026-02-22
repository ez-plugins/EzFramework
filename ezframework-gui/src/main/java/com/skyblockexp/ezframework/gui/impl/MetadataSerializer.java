package com.skyblockexp.ezframework.gui.impl;

import java.util.Collections;

/**
 * Helper for serializing and parsing small metadata maps into a compact
 * escaped string stored in item PersistentDataContainer.
 */
final class MetadataSerializer {
    private MetadataSerializer() {}

    static String serializeMetadata(java.util.Map<String, String> meta) {
        if (meta == null || meta.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (java.util.Map.Entry<String, String> e : meta.entrySet()) {
            if (!first) sb.append(';');
            first = false;
            sb.append(escape(e.getKey()));
            sb.append('=');
            sb.append(escape(e.getValue()));
        }
        return sb.toString();
    }

    static java.util.Map<String, String> parseMetadata(String s) {
        if (s == null || s.isEmpty()) return Collections.emptyMap();
        java.util.Map<String, String> map = new java.util.HashMap<>();
        StringBuilder cur = new StringBuilder();
        String curKey = null;
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) {
                cur.append(c);
                esc = false;
                continue;
            }
            if (c == '\\') {
                esc = true;
                continue;
            }
            if (curKey == null && c == '=') {
                curKey = cur.toString();
                cur.setLength(0);
                continue;
            }
            if (curKey != null && c == ';') {
                map.put(curKey, cur.toString());
                curKey = null;
                cur.setLength(0);
                continue;
            }
            cur.append(c);
        }
        if (curKey != null) {
            map.put(curKey, cur.toString());
        }
        return java.util.Collections.unmodifiableMap(map);
    }

    private static String escape(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (char c : s.toCharArray()) {
            if (c == '\\') sb.append("\\\\");
            else if (c == '=') sb.append("\\=");
            else if (c == ';') sb.append("\\;");
            else sb.append(c);
        }
        return sb.toString();
    }
}
