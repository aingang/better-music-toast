plugins {
    java
    // Minecraft 26.x is not obfuscated any more; this variant of Architectury Loom sets Forge up without remapping.
    id("dev.architectury.loom-no-remap") version "1.17.493"
    // Uploads the jar to Modrinth and CurseForge ("gradlew publishMods"), like the root project.
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

/*
 * Forge for Minecraft 26.x, one version per run: "gradlew build -Pnode=26.2-forge". The sources come from the
 * Stonecutter node of that name in the repository root, generated with "gradlew :<node>:stonecutterGenerate"
 * there. Version, Minecraft range and Forge version are read from the root's stonecutter.properties.toml.
 * Older Forge versions are built by legacy/loom (Gradle 8 cannot run on Java 25, which 26.x needs).
 */
val repoRoot = file("../..")
val node = providers.gradleProperty("node").orNull ?: error("Pick a version with -Pnode=<mc>-forge")
require(node.endsWith("-forge")) { "legacy/loom-26 only builds Forge" }
val mc = node.substringBeforeLast('-')
val generated = repoRoot.resolve("versions/$node/build/generated/stonecutter/main")
val modId = "bettermusictoast"

val toml = repoRoot.resolve("stonecutter.properties.toml").readText()
fun tomlValue(key: String, section: String?): String? {
    val text = if (section == null) toml.substringBefore("\n[") else toml.substringAfter("[\"$section\"]", "").substringBefore("\n[")
    return Regex("""(?m)^${Regex.escape(key)}\s*=\s*"([^"]*)"""").find(text)?.groupValues?.get(1)
}
require(toml.contains("[\"$node\"]")) { "No section [\"$node\"] in stonecutter.properties.toml" }
val modVersion = tomlValue("mod.version", null)!!
val mcLabel = tomlValue("mod.mc_label", node)!!
val mcCompat = tomlValue("mod.mc_compat", node)!!
val mcReleases = tomlValue("mod.mc_releases", node)!!.split(",").map { it.trim() }
val forgeVersion = tomlValue("deps.forge", node)!!
// Like build.forge.gradle.kts: the lowest Forge the jar asks for, and the major version for the loader.
val forgeMin = tomlValue("mod.forge_min", node) ?: forgeVersion.substringAfter('-').substringBefore('.')
val forgeLoader = forgeMin.substringBefore('.')
// pack.mcmeta range, as in Forge's example mod of that version.
val packRange = when {
    mc.startsWith("26.3") -> "121" to "121"
    mc.startsWith("26.2") -> "[107, 1]" to "107"
    else -> "[101, 1]" to "101"
}

version = "$modVersion+$mcLabel-forge"
group = "de.bettermusictoast"
base.archivesName.set("better-music-toast")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

sourceSets.main {
    java.setSrcDirs(listOf(generated.resolve("java")))
    resources.setSrcDirs(listOf(generated.resolve("resources")))
}

loom {
    forge {
        mixinConfig("$modId.mixins.json")
    }
    runConfigs {
        remove(getByName("server"))
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mc")
    forge("net.minecraftforge:forge:$forgeVersion")

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)
    implementation("io.github.llamalad7:mixinextras-forge:0.4.1")
    include("io.github.llamalad7:mixinextras-forge:0.4.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    // The same templates and exclusions as legacy/loom, for the 26.x versions.
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft" to mcCompat,
        // Forge 26.x still ships Mixin 0.8.7, which knows levels only up to JAVA_21 and refuses to
        // start on an unknown one. Our Java 25 classes still load; Mixin just logs the newer version.
        "java" to "JAVA_21",
        "vanilla_toast" to true,
        "refmap" to false,
        "options_list" to false,
        "stb_audio" to false,
        "options_file" to true,
        "screen_mixin" to false,
        "forge_hud_mixin" to false,
        "access_transformer" to false,
        "forge_min" to forgeMin,
        "forge_loader" to forgeLoader,
        "pack_format" to 0,
    )
    inputs.properties(props)
    filesMatching(listOf("META-INF/mods.toml", "*.mixins.json", "pack.mcmeta")) { expand(props) }
    val packRange = packRange
    filesMatching("pack.mcmeta") {
        filter { line ->
            if (line.contains("\"pack_format\"")) "\t\t\"min_format\": ${packRange.first},\n\t\t\"max_format\": ${packRange.second}" else line
        }
    }

    exclude("fabric.mod.json", "*.accesswidener", "META-INF/neoforge.mods.toml", "META-INF/accesstransformer*.cfg")
    // Names for C418's numbered music files and the texts / icon Minecraft itself ships since 1.21.6.
    exclude("assets/bettermusictoast_old_music/**", "assets/minecraft/lang/**", "assets/bettermusictoast/textures/gui/sprites/**")
}

// The finished jar goes to build/libs/<mod version>/ in the repository root, next to the other versions.
val modJar = tasks.jar.get().apply {
    from(repoRoot.resolve("LICENSE"))
    destinationDirectory.set(repoRoot.resolve("build/libs/$modVersion"))
}

publishMods {
    file = modJar.archiveFile
    version = project.version.toString()
    displayName = project.version.toString()
    changelog = repoRoot.resolve("RELEASE_NOTES.md").readText().trim()
    type = STABLE
    modLoaders.add("forge")
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
