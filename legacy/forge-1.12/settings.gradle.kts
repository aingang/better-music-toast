// Minecraft 1.9.4 to 1.12.2 (Forge) are built separately from the Stonecutter project in the repository root,
// like 1.8.9: almost nothing of the modern code fits these versions, and their tooling (Essential's Loom fork)
// needs Gradle 8. One code base for all of them; "-Pmc=<version>" picks the Minecraft version.
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

rootProject.name = "bettermusictoast-1.12-forge"
