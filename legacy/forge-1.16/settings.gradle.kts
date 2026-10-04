// Minecraft 1.16.x on Forge. The code is the same as for every other version: Stonecutter in the repository
// root writes it for each version (versions/<mc>-forge/build/generated/stonecutter/main), and this build
// compiles it. ModDevGradle cannot set up Forge before 1.17, Essential's Loom fork can (it needs Gradle 8).
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net")
        maven("https://maven.minecraftforge.net/")
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

rootProject.name = "bettermusictoast-1.16-forge"
