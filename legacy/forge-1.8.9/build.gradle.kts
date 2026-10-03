import groovy.json.JsonSlurper

plugins {
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    // Uploads the jar to Modrinth ("gradlew publishMods"), like the root project.
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

// Shared with the Stonecutter project in the repository root: the mod version and the texts.
val repoRoot = file("../..")
val rootResources = repoRoot.resolve("src/main/resources")
val modVersion = Regex("""mod\.version\s*=\s*"([^"]+)"""")
    .find(repoRoot.resolve("stonecutter.properties.toml").readText())!!.groupValues[1]
val modId = "bettermusictoast"
val mcVersion = "1.8.9"

version = "$modVersion+$mcVersion-forge"
group = "de.bettermusictoast"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    launchConfigs {
        "client" {
            property("mixin.debug", "true")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
        }
    }
    runConfigs {
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("$modId.mixins.json")
    }
    mixin {
        defaultRefmapName.set("$modId.refmap.json")
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
}

// Libraries that are copied into the mod jar (Forge 1.8.9 has no Mixin of its own).
val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    shadowImpl("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        isTransitive = false
    }
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

/*
 * Minecraft 1.8.9 reads ".lang" files (key=value, named like en_US) instead of the JSON files of
 * newer versions. They are generated from the root project's JSON files, so every text exists only
 * once: the mod's own texts, the "Music Frequency" texts (shipped for all versions before 1.21.6)
 * and the names of the old numbered C418 music files. Texts that would be wrong in 1.8.9 (e.g.
 * tooltips naming advancements or Minecraft's own music toast) are replaced from lang/ in this folder.
 */
// The languages Minecraft 1.8.9 offers (from its asset index).
val legacyLanguages = ("af_ZA ar_SA ast_ES az_AZ bg_BG ca_ES cs_CZ cy_GB da_DK de_DE el_GR en_AU en_CA en_GB en_PT " +
    "eo_UY es_AR es_ES es_MX es_UY es_VE et_EE eu_ES fa_IR fi_FI fil_PH fr_CA fr_FR ga_IE gl_ES gv_IM he_IL hi_IN " +
    "hr_HR hu_HU hy_AM id_ID is_IS it_IT ja_JP ka_GE ko_KR kw_GB la_LA lb_LU lt_LT lv_LV mi_NZ ms_MY mt_MT nds_DE " +
    "nl_NL nn_NO no_NO oc_FR pl_PL pt_BR pt_PT qya_AA ro_RO ru_RU se_NO sk_SK sl_SI sr_SP sv_SE th_TH tlh_AA tr_TR " +
    "uk_UA val_ES vi_VN zh_CN zh_TW en_US").split(" ").toSet()

val generateLang by tasks.registering {
    inputs.property("languages", legacyLanguages.sorted())
    val sources = listOf(
        rootResources.resolve("assets/bettermusictoast/lang"),
        rootResources.resolve("assets/minecraft/lang"),
        rootResources.resolve("assets/bettermusictoast_old_music/lang"),
        // Last, so its texts win.
        file("lang"),
    )
    sources.forEach { inputs.dir(it) }
    val output = layout.buildDirectory.dir("generated/lang")
    outputs.dir(output)
    doLast {
        val langDir = output.get().asFile.resolve("assets/bettermusictoast/lang")
        langDir.deleteRecursively()
        langDir.mkdirs()
        val texts = sortedMapOf<String, MutableMap<String, String>>()
        sources.forEach { dir ->
            dir.listFiles { f -> f.name.endsWith(".json") }!!.forEach { file ->
                @Suppress("UNCHECKED_CAST")
                val json = JsonSlurper().parse(file, "UTF-8") as Map<String, String>
                val code = file.nameWithoutExtension
                val keys = json.filterKeys { key ->
                    // From Minecraft's namespace only what the mod itself adds before 1.21.6.
                    dir.parentFile.name != "minecraft" || key.startsWith("options.music_frequency") || key == "music.game.end.boss"
                }
                texts.getOrPut(code) { linkedMapOf() }.putAll(keys)
            }
        }
        texts.forEach { (code, keys) ->
            // "de_de" -> "de_DE"; 1.8.9 language codes keep the region in capitals.
            val legacyCode = code.substringBefore('_') + "_" + code.substringAfter('_').uppercase()
            if (keys.isEmpty() || legacyCode !in legacyLanguages) return@forEach
            val lines = keys.map { (key, value) -> "$key=" + value.replace("\n", "\\n") }
            langDir.resolve("$legacyCode.lang").writeText(lines.joinToString("\n", postfix = "\n"), Charsets.UTF_8)
        }
    }
}

tasks.processResources {
    inputs.property("version", modVersion)
    inputs.property("mcversion", mcVersion)
    filesMatching("mcmod.info") {
        expand("version" to modVersion, "mcversion" to mcVersion)
    }
    from(generateLang)
    // Icon and music notes picture from the root project.
    from(rootResources.resolve("assets/bettermusictoast")) {
        include("icon.png", "textures/gui/sprites/music_notes.png")
        into("assets/bettermusictoast")
    }
}

tasks.withType<org.gradle.jvm.tasks.Jar> {
    archiveBaseName.set("better-music-toast")
    manifest.attributes(
        "FMLCorePluginContainsFMLMod" to "true",
        "ForceLoadAsMod" to "true",
        "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
        "MixinConfigs" to "$modId.mixins.json",
    )
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    archiveClassifier.set("non-obfuscated-with-deps")
    configurations = listOf(shadowImpl)
}

// The finished jar goes to build/libs/<mod version>/ in the repository root, next to the other versions.
val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
    destinationDirectory.set(repoRoot.resolve("build/libs/$modVersion"))
}

tasks.assemble.get().dependsOn(tasks.remapJar)

// Modrinth upload, the same way as in the root project (token in the personal gradle.properties,
// changelog from RELEASE_NOTES.md, "-PdryRun" to try it without uploading).
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
        minecraftVersions.add(mcVersion)
    }
}
