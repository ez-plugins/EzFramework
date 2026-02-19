# EzFramework

Lightweight Java framework utilities to simplify Bukkit/Spigot plugin development.

- Name: EzFramework
- Package: `com.skyblockexp.ezframework` 
- Developed by: Shadow48402
- Maintained by: EzPlugins

Overview
--------
This small framework provides a type-safe, simple `Registry` and an abstract `Manager` lifecycle class to help move getter/setter and manager logic out of your main plugin class.

Quick start
-----------
1. Add EzFramework as a dependency (install to your local repo or include as a module).
2. In your plugin's `onEnable()`:

```java
import com.skyblockexp.ezframework.Registry;

public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Obtain the per-plugin registry and register managers
        Registry.forPlugin(this).register("economy", new MyEconomyManager());
        Registry.forPlugin(this).initAll();
    }

    @Override
    public void onDisable() {
        Registry.forPlugin(this).shutdownAll();
    }
}
```

Usage notes
-----------
- `Registry` stores arbitrary objects keyed by string or `Class`.
- `Manager` is a light lifecycle base type that plugins can extend to run `init()` and `shutdown()`.

License & Contributing
----------------------
Developed by Shadow48402 and maintained by EzPlugins. Contributions welcome via PR.
