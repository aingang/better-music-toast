import groovy.json.JsonSlurper

plugins {
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    // Uploads the jar to Modrinth and CurseForge ("gradlew publishMods"), like the root project.
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

// Shared with the Stonecutter project in the repository root: the mod version and the texts.
val repoRoot = file("../..")
val rootResources = repoRoot.resolve("src/main/resources")
val modVersion = Regex("""mod\.version\s*=\s*"([^"]+)"""")
    .find(repoRoot.resolve("stonecutter.properties.toml").readText())!!.groupValues[1]
val modId = "bettermusictoast"

/*
 * One code base for Minecraft 1.9.4 to 1.12.2; "-Pmc=<version>" picks the version (default 1.12.2).
 * versions: the Minecraft versions the jar runs on. 1.11 and 1.11.2 give byte-identical code, and so do
 * 1.12, 1.12.1 and 1.12.2, so one jar covers each line: the released jars are -Pmc=1.9.4, 1.10.2,
 * 1.11.2 (1.11-1.11.2) and 1.12.2 (1.12-1.12.2). -Pmc=1.11, 1.12 and 1.12.1 build against that exact
 * version, only to check that the code is still the same.
 * forge: the Forge build compiled against (only builds that still have a "userdev" file work here).
 * languages: the languages that Minecraft version has (from its asset index); only these get a .lang file.
 */
data class Target(val versions: List<String>, val forge: String, val mappings: String, val languages: String)

val languages19 = "af_ZA ar_SA ast_ES az_AZ be_BY bg_BG br_FR ca_ES cs_CZ cy_GB da_DK de_DE el_GR en_AU en_CA en_GB en_NZ " +
    "en_PT en_UD en_US eo_UY es_AR es_ES es_MX es_UY es_VE et_EE eu_ES fa_IR fi_FI fil_PH fo_FO fr_CA fr_FR fy_NL ga_IE " +
    "gd_GB gl_ES gv_IM he_IL hi_IN hr_HR hu_HU hy_AM id_ID is_IS it_IT ja_JP jbo_EN ka_GE ko_KR ksh_DE kw_GB la_LA lb_LU " +
    "li_LI lol_US lt_LT lv_LV mi_NZ mk_MK ms_MY mt_MT nds_DE nl_NL nn_NO no_NO oc_FR pl_PL pt_BR pt_PT qya_AA ro_RO ru_RU " +
    "se_NO sk_SK sl_SI so_SO sq_AL sr_SP sv_SE th_TH tlh_AA tr_TR tzl_TZL uk_UA val_ES vi_VN zh_CN zh_TW"
val languages110 = "$languages19 de_AT haw_US mn_MN swg_de"
val languages111 = "${languages110.lowercase()} io_ido"
val languages112 = "$languages111 bs_ba de_alg de_ch en_ws es_cl ig_ng io_en kab_kab kn_in nl_be oj_ca ta_in vec_it yo_ng"

val targets = mapOf(
    "1.9.4" to Target(listOf("1.9.4"), "1.9.4-12.17.0.2317-1.9.4", "26-1.9.4", languages19),
    "1.10.2" to Target(listOf("1.10.2"), "1.10.2-12.18.3.2511", "29-1.10.2", languages110),
    "1.11" to Target(listOf("1.11"), "1.11-13.19.1.2199", "32-1.11", languages111),
    "1.11.2" to Target(listOf("1.11", "1.11.2"), "1.11.2-13.20.1.2588", "32-1.11", languages111),
    "1.12" to Target(listOf("1.12"), "1.12-14.21.1.2443", "39-1.12", languages112),
    "1.12.1" to Target(listOf("1.12.1"), "1.12.1-14.22.1.2485", "39-1.12", languages112),
    "1.12.2" to Target(listOf("1.12", "1.12.1", "1.12.2"), "1.12.2-14.23.5.2847", "39-1.12", languages112),
)
val mcVersion = (findProperty("mc") as String?) ?: "1.12.2"
val target = targets[mcVersion] ?: error("Unknown Minecraft version $mcVersion, known: ${targets.keys}")
val versionLabel = if (target.versions.size == 1) target.versions[0] else "${target.versions.first()}-${target.versions.last()}"

/** "1.12.2" -> 11202, "1.9.4" -> 10904; used by the //#if MC... lines in the code. */
fun mcNumber(version: String): Int {
    val parts = version.split('.').map { it.toInt() } + listOf(0, 0)
    return parts[0] * 10000 + parts[1] * 100 + parts[2]
}
val mc = mcNumber(mcVersion)

version = "$modVersion+$versionLabel-forge"
group = "de.bettermusictoast"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    runConfigs {
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("de.oceanlabs.mcp:mcp_stable:${target.mappings}")
    forge("net.minecraftforge:forge:${target.forge}")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

/*
 * The code is written for 1.12.2. Lines that differ between the versions are marked like this:
 *     //#if MC>=11200
 *     code for 1.12 and newer
 *     //#else
 *     //$$ code for older versions
 *     //#endif
 * Before compiling, a copy is made in which the lines of the other versions are commented out with
 * "//$$ " and those of the chosen version are active. Conditions: MC with ==, !=, <, <=, >, >=,
 * combined with && or ||.
 */
fun condition(text: String): Boolean = text.split("||").any { alternative ->
    alternative.split("&&").all { part ->
        val match = Regex("""^\s*MC\s*(==|!=|<=|>=|<|>)\s*(\d+)\s*$""").find(part) ?: error("Bad condition: $text")
        val value = match.groupValues[2].toInt()
        when (match.groupValues[1]) {
            "==" -> mc == value
            "!=" -> mc != value
            "<=" -> mc <= value
            ">=" -> mc >= value
            "<" -> mc < value
            else -> mc > value
        }
    }
}

fun preprocess(lines: List<String>, fileName: String): List<String> {
    // Per open //#if: whether the current branch is active, and whether one branch was taken already.
    val branches = ArrayDeque<BooleanArray>()
    return lines.mapIndexed { index, line ->
        val trimmed = line.trim()
        val indent = line.substring(0, line.length - line.trimStart().length)
        when {
            trimmed.startsWith("//#if ") -> {
                val active = condition(trimmed.removePrefix("//#if "))
                branches.addLast(booleanArrayOf(active, active))
                line
            }
            trimmed.startsWith("//#elseif ") -> {
                val branch = branches.lastOrNull() ?: error("$fileName:${index + 1}: //#elseif without //#if")
                val active = !branch[1] && condition(trimmed.removePrefix("//#elseif "))
                branch[0] = active
                if (active) branch[1] = true
                line
            }
            trimmed == "//#else" -> {
                val branch = branches.lastOrNull() ?: error("$fileName:${index + 1}: //#else without //#if")
                branch[0] = !branch[1]
                branch[1] = true
                line
            }
            trimmed == "//#endif" -> {
                branches.removeLastOrNull() ?: error("$fileName:${index + 1}: //#endif without //#if")
                line
            }
            branches.all { it[0] } ->
                if (trimmed.startsWith("//$$")) indent + trimmed.removePrefix("//$$").removePrefix(" ") else line
            trimmed.isEmpty() || trimmed.startsWith("//$$") -> line
            else -> "$indent//$$ $trimmed"
        }
    }.also { if (branches.isNotEmpty()) error("$fileName: //#if without //#endif") }
}

val preprocessedJava = layout.buildDirectory.dir("preprocessed/$mcVersion/java")
val preprocessJava by tasks.registering {
    val source = file("src/main/java")
    inputs.dir(source)
    inputs.property("mc", mc)
    inputs.property("versions", target.versions)
    outputs.dir(preprocessedJava)
    doLast {
        val output = preprocessedJava.get().asFile
        output.deleteRecursively()
        // The Minecraft versions Forge may load the jar on, for @Mod(acceptedMinecraftVersions).
        val range = if (target.versions.size == 1) "[${target.versions[0]}]" else "[${target.versions.first()},${target.versions.last()}]"
        output.resolve("de/bettermusictoast").mkdirs()
        output.resolve("de/bettermusictoast/BuildInfo.java").writeText(
            "package de.bettermusictoast;\n\n/** Written by the build (build.gradle.kts). */\npublic final class BuildInfo {\n" +
                "\tpublic static final String MINECRAFT_VERSIONS = \"$range\";\n\n\tprivate BuildInfo() {\n\t}\n}\n",
            Charsets.UTF_8,
        )
        source.walkTopDown().filter { it.isFile }.forEach { file ->
            val relative = file.relativeTo(source)
            val target = output.resolve(relative.path)
            target.parentFile.mkdirs()
            if (file.extension == "java") {
                val lines = preprocess(file.readLines(Charsets.UTF_8), relative.path)
                target.writeText(lines.joinToString("\n", postfix = "\n"), Charsets.UTF_8)
            } else {
                file.copyTo(target, overwrite = true)
            }
        }
    }
}

sourceSets.main {
    java.setSrcDirs(listOf(preprocessedJava))
}
tasks.compileJava { dependsOn(preprocessJava) }

/*
 * Minecraft before 1.13 reads ".lang" files (key=value) instead of the JSON files of newer versions.
 * They are generated from the root project's JSON files, so every text exists only once: the mod's
 * own texts, the "Music Frequency" texts and the names of the old numbered C418 music files. Texts
 * that would be wrong here (e.g. tooltips naming Minecraft's own music toast) are replaced from
 * lang/ in this folder. Up to 1.10 the files are named like "en_US", from 1.11 on like "en_us".
 */
val generateLang by tasks.registering {
    inputs.property("mc", mc)
    inputs.property("languages", target.languages)
    val sources = listOf(
        rootResources.resolve("assets/bettermusictoast/lang"),
        rootResources.resolve("assets/minecraft/lang"),
        rootResources.resolve("assets/bettermusictoast_old_music/lang"),
        // Last, so their texts win: texts for all these versions, then those for achievements (before 1.12)
        // or advancement toasts (1.12).
        file("lang"),
        file(if (mc >= 11200) "lang-advancements" else "lang-achievements"),
    )
    sources.forEach { inputs.dir(it) }
    val output = layout.buildDirectory.dir("generated/lang/$mcVersion")
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
        // From 1.11 on, Forge treats a mod without pack.mcmeta as an old resource pack (format 2) and looks
        // for "en_US.lang" instead of "en_us.lang", so none of the texts would be found.
        val packMeta = output.get().asFile.resolve("pack.mcmeta")
        packMeta.delete()
        if (mc >= 11100) {
            packMeta.writeText("{\n  \"pack\": {\n    \"pack_format\": 3,\n    \"description\": \"Better Music Toast\"\n  }\n}\n", Charsets.UTF_8)
        }
        // "de_de" -> "de_DE" up to 1.10; the file is named exactly like the game's own.
        val gameCodes = target.languages.split(" ").associateBy { it.lowercase() }
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
    inputs.property("mcversion", target.versions.joinToString(", "))
    filesMatching("mcmod.info") {
        expand("version" to modVersion, "mcversion" to target.versions.joinToString(", "))
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

tasks.jar {
    archiveClassifier.set("dev")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates/$mcVersion"))
}

// The finished jar goes to build/libs/<mod version>/ in the repository root, next to the other versions.
val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    destinationDirectory.set(repoRoot.resolve("build/libs/$modVersion"))
}

tasks.assemble.get().dependsOn(tasks.remapJar)

// Modrinth and CurseForge upload, the same way as in the root project (token in the personal gradle.properties,
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
        minecraftVersions.addAll(target.versions)
    }

    curseforge {
        accessToken = providers.gradleProperty("curseforgeToken")
        projectId = "1725223"
        minecraftVersions.addAll(target.versions)
        client = true
        server = false
    }
}
