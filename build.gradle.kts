plugins {
    // Applies the right Loom variant for the Minecraft version being built.
    id("dev.kikugie.loom-back-compat")
    // Uploads the jars to Modrinth ("gradlew publishMods").
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

// Read once here: inside task blocks property() would look at the task instead of the project.
val modVersion = property("mod.version") as String
val mcCompat = property("mod.mc_compat").toString()
val mcReleases = property("mod.mc_releases").toString().split(",").map { it.trim() }
version = "$modVersion+${property("mod.mc_label")}"
base.archivesName = property("mod.archive") as String

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

repositories {
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Mojang's names on obfuscated versions; does nothing on 26.x.
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    // Mod Menu is optional at runtime; we only compile against its API.
    modCompileOnly("com.terraformersmc:modmenu:${property("deps.modmenu")}")
}

loom {
    runConfigs.all {
        runDirectory = rootProject.file("run")
    }

    // Before 1.21.6 the mod adds Minecraft's "Music Frequency" option itself (see MusicFrequency).
    if (sc.current.parsed < "1.21.6") {
        accessWidenerPath = rootProject.file("src/main/resources/bettermusictoast.accesswidener")
    }
}

// Modrinth upload. The access token lives outside the project in ~/.gradle/gradle.properties
// (modrinthToken=...). "gradlew publishMods -PdryRun" only shows what would be uploaded.
publishMods {
    file = loomx.modJar.flatMap { it.archiveFile }
    version = project.version.toString()
    displayName = project.version.toString()
    changelog = rootProject.file("RELEASE_NOTES.md").readText().trim()
    type = STABLE
    modLoaders.add("fabric")
    dryRun = providers.gradleProperty("dryRun").isPresent

    modrinth {
        accessToken = providers.gradleProperty("modrinthToken")
        projectId = "A7qcTXkk"
        minecraftVersions.addAll(mcReleases)
        requires("fabric-api")
        optional("modmenu")
    }
}

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    processResources {
        val props = mapOf(
            "version" to project.version.toString(),
            "minecraft" to mcCompat,
            "java" to "JAVA_${requiredJava.majorVersion}",
            "java_version" to requiredJava.majorVersion,
            // Vanilla's own music toast (and its mixins) only exists since 1.21.6.
            "vanilla_toast" to (sc.current.parsed >= "1.21.6"),
        )
        inputs.properties(props)
        filesMatching(listOf("fabric.mod.json", "*.mixins.json")) { expand(props) }

        // Song names, the music notes icon and the Music Frequency option that Minecraft itself
        // only has since 1.21.6.
        if (sc.current.parsed >= "1.21.6") {
            exclude(
                "assets/minecraft/lang/**",
                "assets/bettermusictoast/textures/gui/sprites/**",
                "bettermusictoast.accesswidener",
            )
        } else {
            // Registers the access widener in fabric.mod.json, right before "mixins".
            filesMatching("fabric.mod.json") {
                filter { line ->
                    if (line.trim() == "\"mixins\": [") "\t\"accessWidener\": \"bettermusictoast.accesswidener\",\n$line" else line
                }
            }
        }
    }

    withType<Jar>().configureEach {
        from(rootProject.file("LICENSE"))
    }

    // Builds the jar and copies it to build/libs/<mod version>/ in the project root.
    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar and copies it to build/libs/{mod version}/"
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/$modVersion"))
    }
}
