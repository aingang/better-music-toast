pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

plugins {
    // Builds one jar per Minecraft version from a single codebase.
    id("dev.kikugie.stonecutter") version "0.9.8"
    // Picks the right Loom variant per version (unobfuscated 26.x or obfuscated older ones).
    id("dev.kikugie.loom-back-compat") version "0.4.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        // Each entry is the Minecraft version a jar is compiled against;
        // the Minecraft range it supports is set in stonecutter.properties.toml.
        // Fabric jars are named after the version, NeoForge jars get a "-neoforge" suffix
        // and their own build script.
        versions("1.21.1", "1.21.3", "1.21.5", "1.21.8", "1.21.10", "1.21.11", "26.1.2", "26.2")
        fun neoforge(minecraft: String) = version("$minecraft-neoforge", minecraft).buildscript("build.neoforge.gradle.kts")
        neoforge("1.21.1")
        vcsVersion = "26.2"
    }
}

rootProject.name = "better-music-toast"
