plugins {
    java
    id("gg.essential.loom") version "1.15.50"
    // Uploads the jar to Modrinth and CurseForge ("gradlew publishMods"), like the root project.
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

/*
 * Forge 1.20.2 – 1.21.11 and NeoForge before 1.20.6 (Forge 26.x: legacy/loom-26).
 * One version per run: "gradlew build -Pnode=1.20.4-forge" (NeoForge: "-Pnode=1.20.4-neoforge
 * -Ploom.platform=neoforge"). The sources come from the Stonecutter node of that name in the repository
 * root, generated with "gradlew :<node>:stonecutterGenerate" there. Version, Minecraft range and loader
 * version are read from the root's stonecutter.properties.toml.
 */
val repoRoot = file("../..")
val node = providers.gradleProperty("node").orNull ?: error("Pick a version with -Pnode=<mc>-forge or -Pnode=<mc>-neoforge")
val neoforge = node.endsWith("-neoforge")
val mc = node.substringBeforeLast('-')
val generated = repoRoot.resolve("versions/$node/build/generated/stonecutter/main")
val modId = "bettermusictoast"
require(neoforge == (property("loom.platform") == "neoforge")) {
    "$node needs -Ploom.platform=${if (neoforge) "neoforge" else "forge"}"
}

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
val loaderVersion = tomlValue(if (neoforge) "deps.neoforge" else "deps.forge", node)!!

// Compares Minecraft versions like Stonecutter's "sc.current.parsed >= ...".
fun mcAtLeast(other: String): Boolean {
    val a = mc.split('.').map { it.toInt() }
    val b = other.split('.').map { it.toInt() }
    for (i in 0 until maxOf(a.size, b.size)) {
        val x = a.getOrElse(i) { 0 }
        val y = b.getOrElse(i) { 0 }
        if (x != y) return x > y
    }
    return true
}

// NeoForge before 1.20.5 still has Forge's API (and builds the Forge code, see stonecutter.gradle.kts):
// it reads META-INF/mods.toml and the mixins from the manifest, like Forge.
val oldNeoForge = neoforge && !mcAtLeast("1.20.5")
// Like build.forge.gradle.kts: the lowest Forge the jar asks for, and the major version for the loader.
// Old NeoForge: the lowest NeoForge (mod.forge_min), and its Java loader (javafml) version 1.
val forgeMin = tomlValue("mod.forge_min", node) ?: loaderVersion.substringAfter('-').substringBefore('.')
val forgeLoader = if (oldNeoForge) "1" else forgeMin.substringBefore('.')
require(!mcAtLeast("26.1")) { "Forge 26.x is built by legacy/loom-26" }
val requiredJava = if (mcAtLeast("1.20.5")) 21 else 17
// Before 1.21.6 the mod adds Minecraft's "Music Frequency" option itself, which needs an access transformer.
val accessTransformerFile = when {
    !mcAtLeast("1.21") -> "accesstransformer-1.20.cfg"
    !mcAtLeast("1.21.6") -> "accesstransformer.cfg"
    else -> null
}
// Resource pack format of the Minecraft version (pack.mcmeta), as in Forge's example mod of that version.
val packFormat = when {
    mcAtLeast("1.21.6") -> 64
    mcAtLeast("1.21.4") -> 55
    mcAtLeast("1.21.2") -> 42
    mcAtLeast("1.21") -> 34
    mcAtLeast("1.20.5") -> 32
    mcAtLeast("1.20.3") -> 22
    else -> 18
}
// Since 1.21.9 pack.mcmeta gives a range (min_format / max_format) instead of pack_format.
val packRange: Pair<String, String>? = when {
    mcAtLeast("1.21.11") -> "[94, 1]" to "94"
    mcAtLeast("1.21.9") -> "88" to "88"
    else -> null
}

version = "$modVersion+$mcLabel-${if (neoforge) "neoforge" else "forge"}"
group = "de.bettermusictoast"
base.archivesName.set("better-music-toast")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(requiredJava))
}

sourceSets.main {
    java.setSrcDirs(listOf(generated.resolve("java")))
    resources.setSrcDirs(listOf(generated.resolve("resources")))
}

loom {
    if (neoforge) {
        neoForge {
            if (accessTransformerFile != null) {
                accessTransformer(generated.resolve("resources/META-INF/$accessTransformerFile"))
            }
        }
    } else {
        forge {
            mixinConfig("$modId.mixins.json")
            if (accessTransformerFile != null) {
                accessTransformer(generated.resolve("resources/META-INF/$accessTransformerFile"))
            }
        }
    }
    runConfigs {
        remove(getByName("server"))
    }
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    minecraft("com.mojang:minecraft:$mc")
    if (neoforge) {
        "neoForge"("net.neoforged:neoforge:$loaderVersion")
    } else {
        "forge"("net.minecraftforge:forge:$loaderVersion")
    }
    mappings(loom.officialMojangMappings())

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)
    val mixinExtras = "io.github.llamalad7:mixinextras-${if (neoforge) "neoforge" else "forge"}:0.4.1"
    implementation(mixinExtras)
    include(mixinExtras)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(requiredJava)
}

tasks.processResources {
    // The same templates and exclusions as build.forge.gradle.kts / build.neoforge.gradle.kts in the root.
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft" to mcCompat,
        "java" to "JAVA_$requiredJava",
        "vanilla_toast" to mcAtLeast("1.21.6"),
        // Loom writes the loader's names straight into the mixin classes, so there is no refmap.
        "refmap" to false,
        "options_list" to false,
        // Before 1.20.5 music is decoded by OggAudioStream, which needs a fix (see OggAudioStreamMixin).
        "stb_audio" to !mcAtLeast("1.20.5"),
        "options_file" to true,
        "screen_mixin" to false,
        // From 1.20.6 to 1.21.8 not every Forge build can add HUD parts (see ForgeGuiMixin).
        "forge_hud_mixin" to (!neoforge && mcAtLeast("1.20.6") && !mcAtLeast("1.21.9")),
        "access_transformer" to (accessTransformerFile != null),
        "forge_min" to forgeMin,
        "forge_loader" to forgeLoader,
        "pack_format" to packFormat,
    )
    inputs.properties(props)
    filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "*.mixins.json", "pack.mcmeta")) { expand(props) }
    val packRange = packRange
    if (packRange != null) {
        filesMatching("pack.mcmeta") {
            filter { line ->
                if (line.contains("\"pack_format\"")) "\t\t\"min_format\": ${packRange.first},\n\t\t\"max_format\": ${packRange.second}" else line
            }
        }
    }

    exclude("fabric.mod.json", "*.accesswidener")
    if (neoforge && !oldNeoForge) {
        // NeoForge reads neoforge.mods.toml since 1.20.5 and needs no pack.mcmeta from mods.
        exclude("META-INF/mods.toml", "pack.mcmeta")
    } else {
        exclude("META-INF/neoforge.mods.toml")
    }
    if (oldNeoForge) {
        // Forge's mods.toml, depending on NeoForge instead.
        filesMatching("META-INF/mods.toml") {
            filter { line -> if (line.trim() == "modId = \"forge\"") line.replace("\"forge\"", "\"neoforge\"") else line }
        }
    }
    if (accessTransformerFile != null) {
        exclude { it.name.startsWith("accesstransformer") && it.name != accessTransformerFile }
        rename(accessTransformerFile, "accesstransformer.cfg")
    } else {
        exclude("META-INF/accesstransformer*.cfg")
    }
    // Names for C418's numbered music files, only needed before 1.20.3.
    if (mcAtLeast("1.20.3")) {
        exclude("assets/bettermusictoast_old_music/**")
    }
    // Song names and the music notes icon that Minecraft itself only ships since 1.21.6.
    if (mcAtLeast("1.21.6")) {
        exclude("assets/minecraft/lang/**", "assets/bettermusictoast/textures/gui/sprites/music_notes.png*")
    }
}

tasks.jar {
    from(repoRoot.resolve("LICENSE"))
    // Old NeoForge finds the mod's mixins through this manifest entry, like Forge (there Loom adds it).
    if (oldNeoForge) {
        manifest.attributes("MixinConfigs" to "$modId.mixins.json")
    }
}

// The finished jar goes to build/libs/<mod version>/ in the repository root, next to the other versions.
val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    destinationDirectory.set(repoRoot.resolve("build/libs/$modVersion"))
}

publishMods {
    file = remapJar.archiveFile
    version = project.version.toString()
    displayName = project.version.toString()
    changelog = repoRoot.resolve("RELEASE_NOTES.md").readText().trim()
    type = STABLE
    modLoaders.add(if (neoforge) "neoforge" else "forge")
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
