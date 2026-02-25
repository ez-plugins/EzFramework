package com.skyblockexp.ezframework.testutil;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Canonical TestPlugin used by tests across modules. Provides resource access
 * and saveResource behaviour useful for config tests.
 */
public class TestPlugin extends JavaPlugin {
	@Override
	public InputStream getResource(String filename) {
		return this.getClass().getClassLoader().getResourceAsStream(filename);
	}

	@Override
	public void saveResource(String resourcePath, boolean replace) {
		InputStream in = getResource(resourcePath);
		if (in == null) return;
		File out = new File(getDataFolder(), resourcePath);
		if (out.exists() && !replace) return;
		try (FileOutputStream fos = new FileOutputStream(out)) {
			in.transferTo(fos);
		} catch (IOException ignored) {}
	}
}
