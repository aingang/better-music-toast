// Forge versions ModDevGradle cannot set up: before 1.17 (their MCP has no official Mojang names) and after
// 1.20.1. This node only lets Stonecutter write the version's sources to build/generated/stonecutter/main;
// a separate Gradle build with Loom compiles them:
//   gradlew :<mc>-forge:stonecutterGenerate
//   Forge 1.16:          cd legacy/forge-1.16 && gradlew build -Pmc=1.16.5
//   Forge 1.20.2–1.21.x: cd legacy/loom && gradlew build -Pnode=1.21.1-forge
//   Forge 26.x:          cd legacy/loom-26 && gradlew build -Pnode=26.2-forge
plugins {
    java
}

tasks.named("compileJava") { enabled = false }
tasks.named("processResources") { enabled = false }
tasks.named("jar") { enabled = false }
