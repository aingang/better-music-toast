// Forge after 1.20.1 and NeoForge before 1.20.6. The code is the same as for every other version: Stonecutter
// in the repository root writes it for each version (versions/<node>/build/generated/stonecutter/main), and
// this build compiles it. ModDevGradle cannot set these versions up, Essential's Loom fork can (it needs Gradle 8).
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://repo.essential.gg/repository/maven-releases/")
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "gg.essential.loom" -> useModule("gg.essential:architectury-loom:${requested.version}")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "bettermusictoast-loom"
