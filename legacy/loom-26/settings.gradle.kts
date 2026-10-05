// Forge for Minecraft 26.x. The code is the same as for every other version: Stonecutter in the repository
// root writes it for each version (versions/<node>/build/generated/stonecutter/main), and this build compiles
// it. ModDevGradle cannot set Forge after 1.20.1 up; Architectury Loom can (26.x needs Gradle 9 on Java 25).
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net")
        maven("https://maven.minecraftforge.net/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "bettermusictoast-loom-26"
