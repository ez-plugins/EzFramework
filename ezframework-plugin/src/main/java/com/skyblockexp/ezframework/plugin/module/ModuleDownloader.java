package com.skyblockexp.ezframework.plugin.module;

import com.skyblockexp.ezframework.loader.GitHubReleaseDownloader;
import com.skyblockexp.ezframework.module.ModuleDescriptor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Downloads missing EzFramework module JARs from GitHub Releases.
 *
 * <p>For each module name referenced by a {@link ModuleDescriptor} the
 * downloader checks whether the corresponding JAR already exists in the
 * modules cache directory.  If not, it fetches it from:
 * <pre>
 * https://github.com/{owner}/{repo}/releases/latest/download/{module}.jar
 * </pre>
 * with SHA-256 checksum verification via {@link GitHubReleaseDownloader}.
 */
public final class ModuleDownloader {

    private final Logger logger;
    private final String githubOwner;
    private final String githubRepo;
    private final Path   modulesDir;

    /**
     * @param logger      logger to use for progress/error messages
     * @param githubOwner GitHub organisation or user
     * @param githubRepo  repository name
     * @param modulesDir  directory where module JARs are cached
     */
    public ModuleDownloader(Logger logger, String githubOwner, String githubRepo, Path modulesDir) {
        this.logger      = logger;
        this.githubOwner = githubOwner;
        this.githubRepo  = githubRepo;
        this.modulesDir  = modulesDir;
    }

    /**
     * Ensure all modules listed in the given descriptors are present in the
     * modules cache directory.  Downloads any that are missing.
     *
     * @param descriptors collection of parsed plugin descriptors
     * @throws IOException if any download fails
     */
    public void ensureModulesPresent(Collection<ModuleDescriptor> descriptors) throws IOException {
        File dir = modulesDir.toFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create modules directory: " + dir.getAbsolutePath());
        }

        GitHubReleaseDownloader downloader = new GitHubReleaseDownloader(logger);

        for (ModuleDescriptor descriptor : descriptors) {
            for (String moduleName : descriptor.modules()) {
                Path target = modulesDir.resolve(moduleName + ".jar");
                if (target.toFile().exists()) {
                    logger.fine("Module already cached: " + moduleName);
                    continue;
                }

                String url = "https://github.com/" + githubOwner + "/" + githubRepo
                        + "/releases/latest/download/" + moduleName + ".jar";

                logger.info("Downloading module '" + moduleName + "' from " + url);
                try {
                    downloader.download(url, target);
                    logger.info("Module downloaded: " + moduleName);
                } catch (IOException ex) {
                    logger.severe("Failed to download module '" + moduleName + "': " + ex.getMessage());
                    throw ex;
                }
            }
        }
    }
}
