package com.skyblockexp.ezframework.plugin.module;

import com.skyblockexp.ezframework.module.ModuleDescriptor;
import com.skyblockexp.ezframework.plugin.spi.EzModule;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * Loads EzFramework module JARs from the modules cache directory and
 * initialises the {@link EzModule} implementations they contain.
 *
 * <p>Each module JAR is loaded in its own {@link URLClassLoader} whose
 * parent is the manager plugin's class loader so that API types (from
 * {@code ezframework-api}) are shared.
 *
 * <p>Module implementations are discovered via {@link ServiceLoader} using the
 * SPI file {@code META-INF/services/com.skyblockexp.ezframework.plugin.spi.EzModule}
 * inside each JAR.
 */
public final class ModuleLoader {

    private final Logger logger;
    private final Path   modulesDir;

    /** Loaded modules keyed by module name, in load order. */
    private final Map<String, List<EzModule>> loaded = new LinkedHashMap<>();

    /**
     * @param logger     logger for progress and error messages
     * @param modulesDir directory where module JARs reside
     */
    public ModuleLoader(Logger logger, Path modulesDir) {
        this.logger     = logger;
        this.modulesDir = modulesDir;
    }

    /**
     * Load and initialize all modules referenced by the given descriptors.
     *
     * <p>Already-loaded modules are skipped (idempotent).
     *
     * @param descriptors collection of plugin descriptors declaring required modules
     * @param hostPlugin  platform-specific host-plugin instance passed to
     *                    {@link EzModule#initialize}
     * @throws Exception if any module fails to load or initialize
     */
    public void loadAll(Collection<ModuleDescriptor> descriptors, Object hostPlugin) throws Exception {
        // Aggregate overrides per module across all descriptors
        Map<String, Map<String, String>> overridesPerModule = new LinkedHashMap<>();
        for (ModuleDescriptor d : descriptors) {
            for (String moduleName : d.modules()) {
                overridesPerModule.computeIfAbsent(moduleName, k -> new LinkedHashMap<>())
                        .putAll(d.overrides());
            }
        }

        for (Map.Entry<String, Map<String, String>> entry : overridesPerModule.entrySet()) {
            String moduleName = entry.getKey();
            if (loaded.containsKey(moduleName)) {
                logger.fine("Module already loaded: " + moduleName);
                continue;
            }

            File jarFile = modulesDir.resolve(moduleName + ".jar").toFile();
            if (!jarFile.exists()) {
                logger.severe("Module JAR not found (download failed?): " + jarFile.getAbsolutePath());
                continue;
            }

            List<EzModule> instances = loadModuleJar(moduleName, jarFile, hostPlugin, entry.getValue());
            loaded.put(moduleName, instances);
        }
    }

    private List<EzModule> loadModuleJar(String moduleName, File jarFile,
                                          Object hostPlugin, Map<String, String> overrides)
            throws Exception {

        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader cl = new URLClassLoader(
                new URL[]{jarUrl},
                getClass().getClassLoader()   // parent = manager plugin class loader
        );

        ServiceLoader<EzModule> serviceLoader = ServiceLoader.load(EzModule.class, cl);
        List<EzModule> instances = new ArrayList<>();

        for (EzModule module : serviceLoader) {
            logger.info("Initializing module '" + moduleName + "' via "
                    + module.getClass().getName());
            try {
                module.initialize(hostPlugin, Collections.unmodifiableMap(overrides));
                instances.add(module);
            } catch (Exception ex) {
                logger.severe("Module '" + moduleName + "' initialization failed: " + ex.getMessage());
                throw ex;
            }
        }

        if (instances.isEmpty()) {
            logger.warning("Module JAR '" + moduleName + "' contains no EzModule service implementations");
        }

        return instances;
    }

    /**
     * Shut down all loaded modules in reverse load order and release their
     * class loaders.
     */
    public void unloadAll() {
        List<String> order = new ArrayList<>(loaded.keySet());
        Collections.reverse(order);

        for (String moduleName : order) {
            for (EzModule module : loaded.get(moduleName)) {
                try {
                    module.shutdown();
                } catch (Exception ex) {
                    logger.warning("Error shutting down module '" + moduleName + "': " + ex.getMessage());
                }
            }
        }

        loaded.clear();
    }

    /**
     * @return an immutable view of currently loaded module names
     */
    public Collection<String> getLoadedModuleNames() {
        return Collections.unmodifiableSet(loaded.keySet());
    }
}
