package com.skyblockexp.ezframework.plugin.config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * Reads the manager plugin's own global config from
 * {@code plugins/EzFramework/config.yml}.
 *
 * <p>Supported keys:
 * <ul>
 *   <li>{@code github-owner} — GitHub organisation/user that owns the
 *       release repository (default: {@code ez-plugins})</li>
 *   <li>{@code github-repo} — repository name (default: {@code EzFramework})</li>
 *   <li>{@code modules-dir} — directory where module JARs are cached
 *       (default: {@code plugins/EzFramework/modules})</li>
 * </ul>
 */
public final class GlobalConfig {

    private static final String DEFAULT_OWNER = "ez-plugins";
    private static final String DEFAULT_REPO  = "EzFramework";

    private final String  githubOwner;
    private final String  githubRepo;
    private final String  modulesDirPath;

    private GlobalConfig(String githubOwner, String githubRepo, String modulesDirPath) {
        this.githubOwner    = githubOwner;
        this.githubRepo     = githubRepo;
        this.modulesDirPath = modulesDirPath;
    }

    /**
     * Load the global config from {@code dataFolder/config.yml}.
     * Falls back to defaults when the file does not exist or a key is absent.
     *
     * @param dataFolder plugin data folder
     * @return parsed config
     * @throws IOException on read errors (file existence errors are silently
     *                     ignored and defaults substituted)
     */
    @SuppressWarnings("unchecked")
    public static GlobalConfig load(File dataFolder) throws IOException {
        File configFile = new File(dataFolder, "config.yml");
        Map<String, Object> values = Collections.emptyMap();

        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                Yaml yaml = new Yaml();
                Object parsed = yaml.load(in);
                if (parsed instanceof Map) {
                    values = (Map<String, Object>) parsed;
                }
            }
        }

        String owner      = getString(values, "github-owner", DEFAULT_OWNER);
        String repo       = getString(values, "github-repo",  DEFAULT_REPO);
        String modulesDir = getString(values, "modules-dir",
                new File(dataFolder, "modules").getAbsolutePath());

        return new GlobalConfig(owner, repo, modulesDir);
    }

    private static String getString(Map<String, Object> map, String key, String defaultValue) {
        Object v = map.get(key);
        return (v instanceof String) ? (String) v : defaultValue;
    }

    /** GitHub owner/organisation. */
    public String getGithubOwner() { return githubOwner; }

    /** GitHub repository name. */
    public String getGithubRepo() { return githubRepo; }

    /** Absolute path of the directory where module JARs are cached. */
    public String getModulesDirPath() { return modulesDirPath; }
}
