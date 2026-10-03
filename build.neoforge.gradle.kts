plugins {
    // NeoForge's own Gradle plugin. NeoForge runs on Mojang's names, so nothing is remapped.
    id("net.neoforged.moddev") version "2.0.148"
    // Uploads the jars to Modrinth ("gradlew publishMods").
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

// Read once here: inside task blocks property() would look at the task instead of the project.
val modVersion = property("mod.version") as String
val mcCompat = property("mod.mc_compat").toString()
val mcReleases = property("mod.mc_releases").toString().split(",").map { it.trim() }
version = "$modVersion+${property("mod.mc_label")}-neoforge"
base.archivesName = property("mod.archive") as String

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}
// Before 1.21.6 the mod adds Minecraft's "Music Frequency" option itself, which needs an access transformer;
// before 1.21 it also puts its own buttons into vanilla option lists (see WidgetOption).
val needsAccessTransformer = sc.current.parsed < "1.21.6"
val accessTransformerFile = if (sc.current.parsed < "1.21") "accesstransformer-1.20.cfg" else "accesstransformer.cfg"

neoForge {
    version = property("deps.neoforge").toString()
    // Read straight from src/ instead of the default location, which Stonecutter only generates later.
    if (needsAccessTransformer) {
        accessTransformers.files.setFrom(rootProject.file("src/main/resources/META-INF/$accessTransformerFile"))
    } else {
        accessTransformers.files.setFrom()
    }

    runs {
        register("client") {
            client()
            gameDirectory = rootProject.file("run-neoforge")
        }
    }

    mods {
        register("bettermusictoast") {
            sourceSet(sourceSets.main.get())
        }
    }
}

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

// Modrinth and CurseForge upload, see build.gradle.kts.
publishMods {
    file = tasks.jar.flatMap { it.archiveFile }
    version = project.version.toString()
    displayName = project.version.toString()
    changelog = rootProject.file("RELEASE_NOTES.md").readText().trim()
    type = STABLE
    modLoaders.add("neoforge")
    dryRun = providers.gradleProperty("dryRun").isPresent

    modrinth {
        accessToken = providers.gradleProperty("modrinthToken")
        projectId = "A7qcTXkk"
        minecraftVersions.addAll(mcReleases)
    }

    curseforge {
        accessToken = providers.gradleProperty("curseforgeToken")
        projectId = "1725223"
        minecraftVersions.addAll(mcReleases)
        client = true
        server = false
    }
}

val publishOnly = providers.gradleProperty("only").orNull?.split(",")?.map { it.trim() }
if (publishOnly != null && project.name !in publishOnly) {
    tasks.matching { it.name == "publishModrinth" || it.name == "publishCurseforge" }.configureEach { enabled = false }
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
            "vanilla_toast" to (sc.current.parsed >= "1.21.6"),
            "access_transformer" to needsAccessTransformer,
            // Only Forge needs a mixin refmap.
            "refmap" to false,
        )
        inputs.properties(props)
        filesMatching(listOf("META-INF/neoforge.mods.toml", "*.mixins.json")) { expand(props) }

        // Fabric-only files.
        exclude("fabric.mod.json", "*.accesswidener", "META-INF/mods.toml", "pack.mcmeta")
        // Names for C418's numbered music files, only needed before 1.20.3.
        if (sc.current.parsed >= "1.20.3") {
            exclude("assets/bettermusictoast_old_music/**")
        }
        // Only the access transformer this version uses, always as META-INF/accesstransformer.cfg.
        val accessTransformerFile = accessTransformerFile
        if (needsAccessTransformer) {
            exclude { it.name.startsWith("accesstransformer") && it.name != accessTransformerFile }
            rename(accessTransformerFile, "accesstransformer.cfg")
        } else {
            exclude("META-INF/accesstransformer*.cfg")
        }
        // Song names and the music notes icon that Minecraft itself only ships since 1.21.6.
        if (sc.current.parsed >= "1.21.6") {
            exclude("assets/minecraft/lang/**", "assets/bettermusictoast/textures/gui/sprites/**")
        }
    }

    jar {
        from(rootProject.file("LICENSE"))
    }

    // Builds the jar and copies it to build/libs/<mod version>/ in the project root.
    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar and copies it to build/libs/{mod version}/"
        from(jar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/$modVersion"))
    }
}
