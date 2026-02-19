package com.skyblockexp.ezframework.cli.generator;

import java.util.Map;

public final class TemplateProcessor {
    private TemplateProcessor() {}

    public static String process(String template, Map<String, String> vars) {
        String out = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            String key = "{{" + e.getKey() + "}}";
            out = out.replace(key, e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }
}
