package com.skyblockexp.ezframework.cli.generator;

import java.util.Map;

/**
 * Simple templating helper that replaces {{KEY}} placeholders with provided
 * variable values.
 */
public final class TemplateProcessor {
    private TemplateProcessor() {}

    /**
     * Replace placeholders in the template with values from the vars map.
     *
     * @param template template text containing placeholders like {{KEY}}
     * @param vars map of variable names to replacement values
     * @return processed template text
     */
    public static String process(String template, Map<String, String> vars) {
        String out = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            String key = "{{" + e.getKey() + "}}";
            out = out.replace(key, e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }
}
