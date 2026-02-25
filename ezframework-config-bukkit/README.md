# EzFramework Config - Bukkit adapters

This module contains Bukkit-specific implementations for the EzFramework config system, such as `YamlEzConfig` and `ConfigBootstrap`.

Build note
- The module depends on the Paper/Spigot API and snapshot repositories. Building the multi-module project requires network access to the Paper and Spigot repositories (for example, `https://repo.papermc.io/repository/maven-public/` and `https://hub.spigotmc.org/nexus/content/repositories/snapshots/`).
- If you cannot access these repositories, exclude or skip this module during local builds (for example: `mvn -pl '!ezframework-config-bukkit' -DskipTests package`).
