package com.skyblockexp.ezframework.plugin.module;

import com.skyblockexp.ezframework.module.ModuleDescriptor;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Reads {@code ezframework.yml} from inside a plugin JAR and returns the
 * corresponding {@link ModuleDescriptor}.
 *
 * <p>Expected YAML format:
 * <pre>
 * plugin: MyPlugin
 * modules:
 *   - storage-mysql
 *   - message-minimessage
 * overrides:
 *   mysql.host: db.example.com
 *   mysql.port: "3307"
 * </pre>
 */
public final class ModuleDescriptorReader {

    private ModuleDescriptorReader() {}

    /**
     * Parse a {@code ezframework.yml} from inside the given JAR file.
     *
     * @param jarPath absolute path to the plugin JAR
     * @return parsed descriptor, or {@code null} when the file is absent
     * @throws IOException on read errors
     */
    @SuppressWarnings("unchecked")
    public static ModuleDescriptor readFromJar(String jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath)) {
            JarEntry entry = jar.getJarEntry("ezframework.yml");
            if (entry == null) return null;

            try (InputStream in = jar.getInputStream(entry)) {
                Yaml yaml = new Yaml();
                Object parsed = yaml.load(in);
                if (!(parsed instanceof Map)) return null;

                Map<String, Object> map = (Map<String, Object>) parsed;

                String pluginName = getString(map, "plugin", jarPath);

                List<String> modules = new ArrayList<>();
                Object modList = map.get("modules");
                if (modList instanceof List) {
                    for (Object m : (List<?>) modList) {
                        if (m instanceof String) modules.add((String) m);
                    }
                }

                Map<String, String> overrides = Collections.emptyMap();
                Object ov = map.get("overrides");
                if (ov instanceof Map) {
                    Map<Object, Object> raw = (Map<Object, Object>) ov;
                    overrides = new java.util.LinkedHashMap<>();
                    for (Map.Entry<Object, Object> e : raw.entrySet()) {
                        overrides.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                    }
                }

                return new ModuleDescriptor(pluginName, modules, overrides);
            }
        }
    }

    private static String getString(Map<String, Object> map, String key, String defaultValue) {
        Object v = map.get(key);
        return (v instanceof String) ? (String) v : defaultValue;
    }
}
