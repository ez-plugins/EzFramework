package com.skyblockexp.ezframework.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Checks whether the EzFramework manager plugin is present on the classpath,
 * and if not, downloads it from the latest GitHub release.
 *
 * <h3>Typical plugin developer usage</h3>
 * <pre>{@code
 * // Call once from your plugin's onLoad() or onEnable() before any framework usage
 * EzFrameworkLoader.ensurePresent(
 *     getLogger(),
 *     "ez-plugins",        // GitHub repo owner
 *     "EzFramework",       // GitHub repo name
 *     getDataFolder().toPath().resolve("../../")  // i.e. the /plugins/ directory
 * );
 * }</pre>
 *
 * <p>If the manager plugin JAR is already installed nothing happens.
 * If it is absent the user is prompted in the log to restart the server after
 * the download completes.
 *
 * <p>This class has no platform dependencies — it only uses {@link java.util.logging.Logger}
 * and the Java standard library.
 */
public final class EzFrameworkLoader {

    private static final String MANAGER_CLASS = "com.skyblockexp.ezframework.plugin.EzFrameworkPlugin";
    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String ASSET_NAME = "ezframework-plugin.jar";

    private EzFrameworkLoader() {}

    /**
     * Ensure the EzFramework manager plugin is present in the {@code pluginsDir}.
     *
     * <p>If the manager plugin class {@code com.skyblockexp.ezframework.plugin.EzFrameworkPlugin}
     * is already loadable, this method returns immediately without downloading anything.
     *
     * <p>Otherwise it fetches the latest GitHub release for the given repository,
     * locates the {@code ezframework-plugin.jar} asset, downloads it (with SHA-256
     * checksum verification) into {@code pluginsDir}, and logs a server-restart notice.
     *
     * @param logger        logger for progress and status messages
     * @param repoOwner     GitHub repository owner (e.g. {@code "ez-plugins"})
     * @param repoName      GitHub repository name (e.g. {@code "EzFramework"})
     * @param pluginsDir    server plugins directory where the JAR should be placed
     */
    public static void ensurePresent(Logger logger, String repoOwner, String repoName, Path pluginsDir) {
        Objects.requireNonNull(logger, "logger");
        Objects.requireNonNull(repoOwner, "repoOwner");
        Objects.requireNonNull(repoName, "repoName");
        Objects.requireNonNull(pluginsDir, "pluginsDir");

        // If the manager is already loaded on this classloader, nothing to do.
        if (isManagerPresent()) {
            return;
        }

        logger.info("[EzFramework] Manager plugin not found — attempting download from GitHub...");

        try {
            String assetUrl = resolveAssetUrl(logger, repoOwner, repoName, ASSET_NAME);
            Path dest = pluginsDir.resolve(ASSET_NAME);
            Files.createDirectories(pluginsDir);

            new GitHubReleaseDownloader(logger).download(assetUrl, dest);

            logger.warning("[EzFramework] Manager plugin downloaded to " + dest
                    + ". Please RESTART the server to load it.");
        } catch (IOException e) {
            logger.severe("[EzFramework] Failed to download manager plugin: " + e.getMessage()
                    + ". Please download ezframework-plugin.jar manually from "
                    + "https://github.com/" + repoOwner + "/" + repoName + "/releases");
        }
    }

    /**
     * Returns {@code true} if the EzFramework manager plugin class is present on the
     * current thread's context classloader (i.e. the manager is already loaded).
     *
     * @return true if the manager plugin is loaded
     */
    public static boolean isManagerPresent() {
        try {
            Class.forName(MANAGER_CLASS, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Fetch the latest release from the GitHub API and return the download URL for
     * the named asset.
     */
    private static String resolveAssetUrl(Logger logger, String owner, String repo, String assetName)
            throws IOException {
        String apiUrl = GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/releases/latest";
        logger.fine("[EzFramework] Querying GitHub API: " + apiUrl);

        GitHubReleaseDownloader helper = new GitHubReleaseDownloader(logger);
        String json = helper.fetchText(apiUrl);

        JsonObject release = JsonParser.parseString(json).getAsJsonObject();
        String tagName = release.has("tag_name") ? release.get("tag_name").getAsString() : "unknown";
        logger.fine("[EzFramework] Latest release: " + tagName);

        JsonArray assets = release.getAsJsonArray("assets");
        if (assets == null) {
            throw new IOException("No assets found in latest release " + tagName);
        }

        for (JsonElement elem : assets) {
            JsonObject asset = elem.getAsJsonObject();
            if (assetName.equals(asset.get("name").getAsString())) {
                String browserUrl = asset.get("browser_download_url").getAsString();
                if (!browserUrl.startsWith("https://")) {
                    throw new IOException("Asset download URL is not HTTPS: " + browserUrl);
                }
                return browserUrl;
            }
        }

        throw new IOException("Asset '" + assetName + "' not found in release " + tagName);
    }
}
