// Minecraft 1.7.10 (Forge) is built separately from the Stonecutter project in the repository root, like
// 1.8.9 and 1.9.4 - 1.12.2: almost nothing of the modern code fits, and Forge 1.7.10 needs its own tooling
// (RetroFuturaGradle by GT New Horizons, the maintained Forge 1.7.10 setup for current Gradle).
pluginManagement {
    repositories {
        maven {
            name = "GTNH Maven"
            url = uri("https://nexus.gtnewhorizons.com/repository/public/")
            mavenContent {
                includeGroupByRegex("com\\.gtnewhorizons\\..+")
                includeGroup("com.gtnewhorizons")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "bettermusictoast-1.7.10-forge"
