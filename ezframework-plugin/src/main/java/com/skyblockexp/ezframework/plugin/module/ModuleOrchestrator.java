package com.skyblockexp.ezframework.plugin.module;

import com.skyblockexp.ezframework.module.ModuleDescriptor;
import com.skyblockexp.ezframework.plugin.config.GlobalConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Orchestrates plugin-descriptor discovery, module downloading, and module
 * loading for one platform invocation.
 *
 * <p>Usage flow:
 * <ol>
 *   <li>Call {@link #scanDescriptors(File)} to collect {@code ezframework.yml}
 *       entries from every JAR found in the plugins directory.</li>
 *   <li>Call {@link #downloadMissing()} to fetch absent module JARs.</li>
 *   <li>Call {@link #initializeModules(Object)} to load and boot each module.</li>
 *   <li>Call {@link #shutdown()} on server stop.</li>
 * </ol>
 */
public final class ModuleOrchestrator {

    private final Logger       logger;
    private final GlobalConfig config;
    private final Path         modulesDir;

    private final List<ModuleDescriptor> descriptors = new ArrayList<>();
    private ModuleDownloader downloader;
    private ModuleLoader     loader;

    /**
     * @param logger platform logger
     * @param config resolved global config
     */
    public ModuleOrchestrator(Logger logger, GlobalConfig config) {
        this.logger     = logger;
        this.config     = config;
        this.modulesDir = Path.of(config.getModulesDirPath());
    }

    /**
     * Scan the given plugins directory for JARs that contain
     * {@code ezframework.yml} and collect their descriptors.
     *
     * @param pluginsDir platform plugins directory (e.g. {@code ./plugins})
     */
    public void scanDescriptors(File pluginsDir) {
        descriptors.clear();
        if (!pluginsDir.isDirectory()) return;

        File[] jars = pluginsDir.listFiles((d, name) -> name.endsWith(".jar"));
        if (jars == null) return;

        for (File jar : jars) {
            try {
                ModuleDescriptor d = ModuleDescriptorReader.readFromJar(jar.getAbsolutePath());
                if (d != null) {
                    logger.fine("Found ezframework.yml in " + jar.getName()
                            + " — modules: " + d.modules());
                    descriptors.add(d);
                }
            } catch (IOException ex) {
                logger.warning("Could not read " + jar.getName()
                        + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Download any missing module JARs required by collected descriptors.
     *
     * @throws IOException if a download fails
     */
    public void downloadMissing() throws IOException {
        downloader = new ModuleDownloader(logger, config.getGithubOwner(),
                config.getGithubRepo(), modulesDir);
        downloader.ensureModulesPresent(descriptors);
    }

    /**
     * Load and initialize all modules.
     *
     * @param hostPlugin platform-specific plugin instance
     * @throws Exception if any module fails
     */
    public void initializeModules(Object hostPlugin) throws Exception {
        loader = new ModuleLoader(logger, modulesDir);
        loader.loadAll(descriptors, hostPlugin);
    }

    /**
     * Shut down all loaded modules.
     */
    public void shutdown() {
        if (loader != null) loader.unloadAll();
    }

    /**
     * @return collected descriptors (populated after {@link #scanDescriptors})
     */
    public List<ModuleDescriptor> getDescriptors() {
        return new ArrayList<>(descriptors);
    }
}
