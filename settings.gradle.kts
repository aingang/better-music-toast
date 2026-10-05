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
        val minecraft = listOf("1.21.1", "1.21.3", "1.21.5", "1.21.8", "1.21.10", "1.21.11", "26.1.2", "26.2", "26.3")
        // Fabric only (NeoForge and Forge for 1.19.x / 1.20.x are set up separately).
        versions("1.16.1", "1.16.5", "1.18.2", "1.19", "1.19.2", "1.19.3", "1.19.4", "1.20", "1.20.1", "1.20.2", "1.20.4", "1.20.6")
        versions(minecraft)
        (listOf("1.20.6") + minecraft).forEach { version("$it-neoforge", it).buildscript("build.neoforge.gradle.kts") }
        // NeoForge before 1.20.6 is compiled by legacy/loom (ModDevGradle cannot set it up).
        listOf("1.20.2", "1.20.4", "1.20.5").forEach {
            version("$it-neoforge", it).buildscript("build.forge-legacy.gradle.kts")
        }
        // Forge only for the versions where it still matters.
        // Forge before 1.17 only gets its sources generated here; legacy/forge-1.16 builds them.
        listOf("1.16.1", "1.16.5").forEach {
            version("$it-forge", it).buildscript("build.forge-legacy.gradle.kts")
        }
        listOf("1.18.1", "1.18.2", "1.19", "1.19.2", "1.19.3", "1.19.4", "1.20", "1.20.1").forEach { version("$it-forge", it).buildscript("build.forge.gradle.kts") }
        // Forge after 1.20.1 is compiled by legacy/loom, like Forge 1.16 (ModDevGradle cannot set it up).
        listOf("1.20.2", "1.20.4", "1.20.6", "1.21.1", "1.21.3", "1.21.5", "1.21.8", "1.21.10", "1.21.11", "26.1.2", "26.2", "26.3").forEach {
            version("$it-forge", it).buildscript("build.forge-legacy.gradle.kts")
        }
        vcsVersion = "26.2"
    }
}

rootProject.name = "better-music-toast"
