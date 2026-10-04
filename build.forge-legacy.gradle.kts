// Forge before 1.17. ModDevGradle cannot set these versions up (their MCP has no official Mojang names), so
// this node only lets Stonecutter write the version's sources to build/generated/stonecutter/main.
// The separate Gradle build in legacy/forge-1.16 compiles them with Essential's Loom:
//   gradlew :1.16.5-forge:stonecutterGenerate
//   cd legacy/forge-1.16 && gradlew build -Pmc=1.16.5
plugins {
    java
}

tasks.named("compileJava") { enabled = false }
tasks.named("processResources") { enabled = false }
tasks.named("jar") { enabled = false }
