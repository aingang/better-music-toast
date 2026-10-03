plugins {
    // Applies the right Loom variant for the Minecraft version being built.
    id("dev.kikugie.loom-back-compat")
}

// Read once here: inside task blocks property() would look at the task instead of the project.
val modVersion = property("mod.version") as String
val mcCompat = property("mod.mc_compat").toString()
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
        )
        inputs.properties(props)
        filesMatching(listOf("fabric.mod.json", "*.mixins.json")) { expand(props) }
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
