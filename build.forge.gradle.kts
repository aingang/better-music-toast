plugins {
    // NeoForge's Gradle plugin in its mode for (Minecraft)Forge up to 1.20.1. Forge still runs on
    // obfuscated (SRG) names there, so the jar and the mixins are remapped ("reobf").
    id("net.neoforged.moddev.legacyforge") version "2.0.148"
    // Uploads the jars to Modrinth ("gradlew publishMods").
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

// Read once here: inside task blocks property() would look at the task instead of the project.
val modVersion = property("mod.version") as String
val mcCompat = property("mod.mc_compat").toString()
val mcReleases = property("mod.mc_releases").toString().split(",").map { it.trim() }
version = "$modVersion+${property("mod.mc_label")}-forge"
base.archivesName = property("mod.archive") as String

val requiredJava = JavaVersion.VERSION_17
// Read straight from src/ instead of the default location, which Stonecutter only generates later.
val accessTransformer = rootProject.file("src/main/resources/META-INF/accesstransformer-1.20.cfg")

legacyForge {
    version = property("deps.forge").toString()
    accessTransformers.files.setFrom(accessTransformer)

    runs {
        register("client") {
            client()
            gameDirectory = rootProject.file("run-forge")
        }
    }

    mods {
        register("bettermusictoast") {
            sourceSet(sourceSets.main.get())
        }
    }
}

mixin {
    add(sourceSets.main.get(), "bettermusictoast.refmap.json")
    config("bettermusictoast.mixins.json")
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    // Forge 1.20.1 does not ship MixinExtras, so it goes inside the jar.
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)
    implementation("io.github.llamalad7:mixinextras-forge:0.4.1")
    jarJar("io.github.llamalad7:mixinextras-forge:0.4.1")
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
    file = tasks.named<Jar>("reobfJar").flatMap { it.archiveFile }
    version = project.version.toString()
    displayName = project.version.toString()
    changelog = rootProject.file("RELEASE_NOTES.md").readText().trim()
    type = STABLE
    modLoaders.add("forge")
    // NeoForge for 1.20.1 is a fork of Forge 1.20.1 and runs this jar too (tested).
    if (sc.current.version == "1.20.1") {
        modLoaders.add("neoforge")
    }
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
            "vanilla_toast" to false,
            "refmap" to true,
        )
        inputs.properties(props)
        filesMatching(listOf("META-INF/mods.toml", "*.mixins.json")) { expand(props) }

        // Files of the other loaders.
        exclude("fabric.mod.json", "*.accesswidener", "META-INF/neoforge.mods.toml", "META-INF/accesstransformer.cfg")
        // pack.mcmeta (only Forge needs it, to load the mod's resources) is pack format 15 = 1.20.1.
        // Forge reads META-INF/accesstransformer.cfg.
        rename("accesstransformer-1.20.cfg", "accesstransformer.cfg")
        // Names for C418's numbered music files, only needed before 1.20.3.
        if (sc.current.parsed >= "1.20.3") {
            exclude("assets/bettermusictoast_old_music/**")
        }
    }

    jar {
        from(rootProject.file("LICENSE"))
        // Forge 1.20.1 finds the mod's mixins through this manifest entry.
        manifest.attributes("MixinConfigs" to "bettermusictoast.mixins.json")
    }

    // Builds the jar and copies it to build/libs/<mod version>/ in the project root.
    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar and copies it to build/libs/{mod version}/"
        from(named<Jar>("reobfJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/$modVersion"))
    }
}
