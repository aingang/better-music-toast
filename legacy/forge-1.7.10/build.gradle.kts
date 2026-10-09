import groovy.json.JsonSlurper

plugins {
    java
    id("com.gtnewhorizons.retrofuturagradle") version "2.0.6"
    // Uploads the jar to Modrinth and CurseForge ("gradlew publishMods"), like the root project.
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

// Shared with the Stonecutter project in the repository root: the mod version and the texts.
val repoRoot = file("../..")
val rootResources = repoRoot.resolve("src/main/resources")
val modVersion = Regex("""mod\.version\s*=\s*"([^"]+)"""")
    .find(repoRoot.resolve("stonecutter.properties.toml").readText())!!.groupValues[1]

version = "$modVersion+1.7.10-forge"
group = "de.bettermusictoast"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

// Forge 10.13.4.1614 (the last 1.7.10 build) with the MCP names "stable_12", RetroFuturaGradle's defaults.
minecraft {
    mcVersion.set("1.7.10")
    username.set("Developer")
    // The small core mod (see the jar manifest below) also in the development client.
    extraRunJvmArguments.add("-Dfml.coreMods.load=de.bettermusictoast.core.BetterMusicToastCore")
}

// Classes that are the same in the 1.7.10, 1.8.9 and 1.12 builds exist only once: in legacy/shared,
// or, where the root project has the very same file, there.
val sharedJava by tasks.registering(Sync::class) {
    from("../shared/java")
    from(repoRoot.resolve("src/main/java")) {
        include("de/bettermusictoast/track/ExternalMusic.java", "de/bettermusictoast/track/OggTags.java")
    }
    into(layout.buildDirectory.dir("generated/sharedJava"))
}
sourceSets.main {
    java.srcDir(sharedJava)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

/*
 * Minecraft 1.7.10 reads ".lang" files (key=value, named like en_US) instead of the JSON files of newer
 * versions. They are generated from the root project's JSON files, so every text exists only once: the
 * mod's own texts, the "Music Frequency" texts and the names of the old numbered C418 music files. Texts
 * that would be wrong here (e.g. tooltips naming advancements or Minecraft's own music toast) are replaced
 * from lang/ in this folder.
 */
// The languages Minecraft 1.7.10 offers (from its asset index).
val legacyLanguages = ("af_ZA ar_SA ast_ES az_AZ bg_BG ca_ES cs_CZ cy_GB da_DK de_DE el_GR en_AU en_CA en_GB en_PT " +
    "en_US eo_UY es_AR es_ES es_MX es_UY es_VE et_EE eu_ES fa_IR fi_FI fil_PH fr_CA fr_FR ga_IE gl_ES gv_IM he_IL " +
    "hi_IN hr_HR hu_HU hy_AM id_ID is_IS it_IT ja_JP ka_GE ko_KR kw_GB la_LA lb_LU lt_LT lv_LV mi_NZ ms_MY mt_MT " +
    "nds_DE nl_NL nn_NO no_NO oc_FR pl_PL pt_BR pt_PT qya_AA ro_RO ru_RU sk_SK sl_SI sr_SP sv_SE th_TH tlh_AA " +
    "tr_TR uk_UA val_ES vi_VN zh_CN zh_TW").split(" ").toSet()
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
        // "de_de" -> "de_DE"; the file is named exactly like the game's own.
        val gameCodes = legacyLanguages.associateBy { it.lowercase() }
        texts.forEach { (code, keys) ->
            val fileCode = gameCodes[code.lowercase()]
            if (keys.isEmpty() || fileCode == null) return@forEach
            val lines = keys.map { (key, value) -> "$key=" + value.replace("\n", "\\n") }
            langDir.resolve("$fileCode.lang").writeText(lines.joinToString("\n", postfix = "\n"), Charsets.UTF_8)
        }
    }
}

tasks.processResources {
    inputs.property("version", modVersion)
    filesMatching("mcmod.info") {
        expand("version" to modVersion, "mcversion" to "1.7.10")
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
    from(repoRoot.resolve("LICENSE"))
    manifest.attributes(
        // A small core mod (no Mixin library needed): keeps the music playing in the pause menu.
        "FMLCorePlugin" to "de.bettermusictoast.core.BetterMusicToastCore",
        "FMLCorePluginContainsFMLMod" to "true",
    )
}

// The finished (reobfuscated) jar goes to build/libs/<mod version>/ in the repository root, next to the other versions.
val reobfJar by tasks.named<org.gradle.jvm.tasks.Jar>("reobfJar") {
    archiveClassifier.set("")
    destinationDirectory.set(repoRoot.resolve("build/libs/$modVersion"))
}

// Modrinth and CurseForge upload, the same way as in the root project (token in the personal gradle.properties,
// changelog from RELEASE_NOTES.md, "-PdryRun" to try it without uploading).
publishMods {
    file = reobfJar.archiveFile
    version = project.version.toString()
    displayName = project.version.toString()
    changelog = repoRoot.resolve("RELEASE_NOTES.md").readText().trim()
    type = STABLE
    modLoaders.add("forge")
    dryRun = providers.gradleProperty("dryRun").isPresent

    modrinth {
        accessToken = providers.gradleProperty("modrinthToken")
        projectId = "A7qcTXkk"
        minecraftVersions.add("1.7.10")
    }

    curseforge {
        accessToken = providers.gradleProperty("curseforgeToken")
        projectId = "1725223"
        minecraftVersions.add("1.7.10")
        client = true
        server = false
    }
}
