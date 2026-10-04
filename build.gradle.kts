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
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_17
    // Minecraft 1.16 runs on Java 8. The code is still written in today's Java and compiled with JDK 17;
    // Jabel turns the newer syntax into Java 8 bytecode (see the java block below).
    else -> JavaVersion.VERSION_1_8
}
val legacyJava = requiredJava == JavaVersion.VERSION_1_8

// Before 1.21.6 the mod adds Minecraft's "Music Frequency" option itself (see MusicFrequency);
// before 1.21 it also puts its own buttons into vanilla option lists (see WidgetOption).
val accessWidener: String? = when {
    sc.current.parsed < "1.17" -> "bettermusictoast-1.16.accesswidener"
    sc.current.parsed < "1.19" -> "bettermusictoast-1.18.accesswidener"
    sc.current.parsed < "1.19.3" -> "bettermusictoast-1.19.accesswidener"
    sc.current.parsed < "1.20.1" -> "bettermusictoast-1.20.0.accesswidener"
    sc.current.parsed < "1.21" -> "bettermusictoast-1.20.accesswidener"
    sc.current.parsed < "1.21.6" -> "bettermusictoast.accesswidener"
    else -> null
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
    // Without its own dependencies: only its API is needed, and some versions pull in libraries
    // from repositories we do not use.
    modCompileOnly("com.terraformersmc:modmenu:${property("deps.modmenu")}") { isTransitive = false }

    if (legacyJava) {
        annotationProcessor("com.github.bsideup.jabel:jabel-javac-plugin:1.0.0")
        // For its @Desugar annotation on records.
        compileOnly("com.github.bsideup.jabel:jabel-javac-plugin:1.0.0")
    }
}

loom {
    runConfigs.all {
        runDirectory = rootProject.file("run")
    }

    if (accessWidener != null) {
        accessWidenerPath = rootProject.file("src/main/resources/$accessWidener")
    }
}

// Modrinth and CurseForge upload. The access tokens live outside the project in
// ~/.gradle/gradle.properties (modrinthToken=..., curseforgeToken=...).
// "gradlew publishMods -PdryRun" only shows what would be uploaded; "publishModrinth" or
// "publishCurseforge" instead of "publishMods" uploads to one site only.
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

    curseforge {
        accessToken = providers.gradleProperty("curseforgeToken")
        projectId = "1725223"
        minecraftVersions.addAll(mcReleases)
        client = true
        server = false
        requires("fabric-api")
        optional("modmenu")
    }
}

// "-Ponly=1.21.1,1.21.3" uploads just those version nodes (e.g. when adding new Minecraft versions).
val publishOnly = providers.gradleProperty("only").orNull?.split(",")?.map { it.trim() }
if (publishOnly != null && project.name !in publishOnly) {
    tasks.matching { it.name == "publishModrinth" || it.name == "publishCurseforge" }.configureEach { enabled = false }
}

java {
    if (legacyJava) {
        // Today's syntax with JDK 17; javac's --release 8 below keeps the API and bytecode at Java 8.
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    } else {
        targetCompatibility = requiredJava
        sourceCompatibility = requiredJava

        toolchain {
            languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (legacyJava) {
            options.release = 8
        }
    }

    processResources {
        val props = mapOf(
            "version" to project.version.toString(),
            "minecraft" to mcCompat,
            "java" to "JAVA_${requiredJava.majorVersion}",
            "java_version" to requiredJava.majorVersion,
            // Vanilla's own music toast (and its mixins) only exists since 1.21.6.
            "vanilla_toast" to (sc.current.parsed >= "1.21.6"),
            // Only Forge needs a mixin refmap.
            "refmap" to false,
            // Before 1.19.3 the settings screen reaches the option list through an accessor.
            "options_list" to (sc.current.parsed >= "1.16.2" && sc.current.parsed < "1.19.3"),
            // Before 1.20.5 music is decoded by OggAudioStream, which needs a fix (see OggAudioStreamMixin).
            "stb_audio" to (sc.current.parsed < "1.20.5"),
            // Fabric API's mod id was "fabric" up to its 1.19.1 builds; later builds are "fabric-api" and
            // still provide "fabric", so the old id fits every Fabric API of these Minecraft versions.
            "fabric_api_id" to (if (sc.current.parsed < "1.19.3") "fabric" else "fabric-api"),
            // Before 1.17 the Music Frequency is not saved in options.txt (see MusicFrequency).
            "options_file" to (sc.current.parsed >= "1.17"),
            // Before 1.17 not every Fabric API has screen events; GameRendererMixin draws over menus instead.
            "screen_mixin" to (sc.current.parsed < "1.17"),
        )
        inputs.properties(props)
        filesMatching(listOf("fabric.mod.json", "*.mixins.json")) { expand(props) }

        // Local copy: the filters below must not reference the build script itself.
        val accessWidener = accessWidener

        // NeoForge and Forge files, and the access wideners this version does not use.
        exclude("META-INF/neoforge.mods.toml", "META-INF/mods.toml", "META-INF/accesstransformer*.cfg", "pack.mcmeta")
        exclude { it.name.endsWith(".accesswidener") && it.name != accessWidener }

        // Song names, the music notes icon and the Music Frequency option that Minecraft itself
        // only has since 1.21.6.
        if (sc.current.parsed >= "1.21.6") {
            exclude("assets/minecraft/lang/**", "assets/bettermusictoast/textures/gui/sprites/**")
        }
        // Names for C418's numbered music files (calm1, hal1, ...), renamed by Minecraft in 1.20.3.
        if (sc.current.parsed >= "1.20.3") {
            exclude("assets/bettermusictoast_old_music/**")
        }
        // Fabric API before 1.16.2 lets files in mod jars replace Minecraft's own (our song names
        // would replace its English texts), so these texts move to a folder of their own. Translation
        // keys work the same from every folder.
        if (sc.current.parsed < "1.16.2") {
            filesMatching("assets/minecraft/lang/**") {
                path = path.replace("assets/minecraft/", "assets/bettermusictoast_vanilla/")
            }
            includeEmptyDirs = false
        }

        // Registers the access widener in fabric.mod.json, right before "mixins".
        if (accessWidener != null) {
            filesMatching("fabric.mod.json") {
                filter { line ->
                    if (line.trim() == "\"mixins\": [") "\t\"accessWidener\": \"$accessWidener\",\n$line" else line
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
