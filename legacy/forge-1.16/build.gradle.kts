import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
    java
    id("gg.essential.loom") version "1.15.50"
    // Uploads the jar to Modrinth and CurseForge ("gradlew publishMods"), like the root project.
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

/*
 * One Forge 1.16 version per run: "gradlew build -Pmc=1.16.5". The sources come from the Stonecutter
 * node "<mc>-forge" in the repository root, generated with "gradlew :<mc>-forge:stonecutterGenerate" there.
 * Version, Minecraft range and Forge version are read from the root's stonecutter.properties.toml.
 */
val repoRoot = file("../..")
val mc = providers.gradleProperty("mc").orNull ?: "1.16.5"
val node = "$mc-forge"
val generated = repoRoot.resolve("versions/$node/build/generated/stonecutter/main")
val modId = "bettermusictoast"

val toml = repoRoot.resolve("stonecutter.properties.toml").readText()
fun tomlValue(key: String, section: String?): String? {
    val text = if (section == null) toml.substringBefore("\n[") else toml.substringAfter("[\"$section\"]", "").substringBefore("\n[")
    return Regex("""(?m)^${Regex.escape(key)}\s*=\s*"([^"]*)"""").find(text)?.groupValues?.get(1)
}
require(toml.contains("[\"$node\"]")) { "No section [\"$node\"] in stonecutter.properties.toml (use -Pmc=1.16.x)" }
val modVersion = tomlValue("mod.version", null)!!
val mcLabel = tomlValue("mod.mc_label", node)!!
val mcCompat = tomlValue("mod.mc_compat", node)!!
val mcReleases = tomlValue("mod.mc_releases", node)!!.split(",").map { it.trim() }
val forgeVersion = tomlValue("deps.forge", node)!!
// Like build.forge.gradle.kts: the lowest Forge the jar asks for, and the major version for the loader.
val forgeMin = tomlValue("mod.forge_min", node) ?: forgeVersion.substringAfter('-').substringBefore('.')
val forgeLoader = forgeMin.substringBefore('.')
// Resource pack format of 1.16 – 1.16.1 and 1.16.2 – 1.16.5.
val packFormat = if (mc == "1.16" || mc == "1.16.1") 5 else 6
val after1162 = mc != "1.16" && mc != "1.16.1"

/*
 * Forge 1.16.1 adds its own AbstractWidget.getHeight() next to Mojang's (fixed in 1.16.2), so Mojang's names
 * give that class two methods of the same name and Loom cannot set Minecraft up. This copies Loom's Mojang
 * mappings with Mojang's method renamed to getHeightVanilla. Only the development environment sees that name;
 * the jar uses Forge's own names. The copy is made from Loom's cache: if it is missing, run once with
 * "-Pmc=1.16.1 -PfetchMappings" (that run fails, but leaves the mappings in the cache).
 */
fun forge1161Mappings(): File {
    val fixed = layout.buildDirectory.file("mappings/mojmap-1.16.1-forge.jar").get().asFile
    if (fixed.exists()) return fixed
    val source = gradle.gradleUserHomeDir.resolve("caches/essential-loom/1.16.1/layered").listFiles()
        ?.firstOrNull { it.name.startsWith("loom.mappings-layered+hash.") && it.name.endsWith(".jar") }
        ?: error("Mojang's 1.16.1 mappings are not in Loom's cache yet: run once with -Pmc=1.16.1 -PfetchMappings")
    val tiny = ZipFile(source).use { zip ->
        zip.getInputStream(zip.getEntry("mappings/mappings.tiny")).readBytes().toString(Charsets.UTF_8)
    }
    val line = "\tm\t()I\tmethod_25364\tgetHeight\te"
    require(tiny.contains(line)) { "Unexpected 1.16.1 mappings in $source" }
    fixed.parentFile.mkdirs()
    ZipOutputStream(fixed.outputStream()).use { zip ->
        zip.putNextEntry(ZipEntry("mappings/mappings.tiny"))
        zip.write(tiny.replace(line, "\tm\t()I\tmethod_25364\tgetHeightVanilla\te").toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
    return fixed
}

version = "$modVersion+$mcLabel-forge"
group = "de.bettermusictoast"
base.archivesName.set("better-music-toast")

// Minecraft 1.16 runs on Java 8. Like the root project, the code is compiled with JDK 17 and Jabel turns
// the newer syntax into Java 8 bytecode.
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

sourceSets.main {
    java.setSrcDirs(listOf(generated.resolve("java")))
    resources.setSrcDirs(listOf(generated.resolve("resources")))
}

loom {
    forge {
        mixinConfig("$modId.mixins.json")
        accessTransformer(generated.resolve("resources/META-INF/accesstransformer-1.16.cfg"))
    }
    mixin {
        defaultRefmapName.set("$modId.refmap.json")
    }
    runConfigs {
        remove(getByName("server"))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$mc")
    // Before the mappings: layered mappings need to know they are for Forge.
    forge("net.minecraftforge:forge:$forgeVersion")
    if (mc == "1.16.1" && !providers.gradleProperty("fetchMappings").isPresent) {
        mappings(files(forge1161Mappings()))
    } else {
        mappings(loom.officialMojangMappings())
    }

    annotationProcessor("com.github.bsideup.jabel:jabel-javac-plugin:1.0.0")
    compileOnly("com.github.bsideup.jabel:jabel-javac-plugin:1.0.0")
    // Only for imports in code that is not used on this version (Forge 1.16 cannot load MixinExtras).
    compileOnly("io.github.llamalad7:mixinextras-common:0.4.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(8)
}

tasks.processResources {
    // The same templates and exclusions as build.forge.gradle.kts in the repository root.
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft" to mcCompat,
        "java" to "JAVA_8",
        "vanilla_toast" to false,
        // Loom writes Forge's names straight into the mixin classes, so there is no refmap.
        "refmap" to false,
        "options_list" to after1162,
        "stb_audio" to true,
        "options_file" to false,
        "screen_mixin" to false,
        "forge_min" to forgeMin,
        "forge_loader" to forgeLoader,
        "pack_format" to packFormat,
    )
    inputs.properties(props)
    filesMatching(listOf("META-INF/mods.toml", "*.mixins.json", "pack.mcmeta")) { expand(props) }

    exclude("fabric.mod.json", "*.accesswidener", "META-INF/neoforge.mods.toml", "META-INF/accesstransformer.cfg")
    exclude { it.name.startsWith("accesstransformer-") && it.name != "accesstransformer-1.16.cfg" }
    rename("accesstransformer-1.16.cfg", "accesstransformer.cfg")
}

tasks.jar {
    from(repoRoot.resolve("LICENSE"))
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
