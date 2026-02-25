// This class was moved to core. Keep a small shim to maintain binary compatibility
package com.skyblockexp.ezframework.config;

import com.skyblockexp.ezframework.bootstrap.Component;
import com.skyblockexp.ezframework.EzPlugin;

/**
 * Shim: delegates to core's ConfigBootstrapComponent when present on the classpath.
 */
public class ConfigBootstrapComponent implements Component {
    private final Component delegate;

    public ConfigBootstrapComponent(Object plugin) {
        Component d = null;
        try {
            Class<?> coreCls = Class.forName("com.skyblockexp.ezframework.config.ConfigBootstrapComponent", true, getClass().getClassLoader());
            for (java.lang.reflect.Constructor<?> ctor : coreCls.getConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();
                if (params.length == 1) {
                    Object inst = ctor.newInstance(plugin);
                    if (inst instanceof Component) {
                        d = (Component) inst;
                        break;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        this.delegate = d;
    }

    @Override
    public void start() throws Exception {
        if (delegate != null) delegate.start();
    }

    @Override
    public void stop() throws Exception {
        if (delegate != null) delegate.stop();
    }
}
