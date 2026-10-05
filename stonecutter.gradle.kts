plugins {
    id("dev.kikugie.stonecutter")
}

// The version whose code is "live" in src/; code for other versions sits in comments.
stonecutter active "26.2"

// Modrinth and CurseForge list versions by upload time: upload the oldest Minecraft version first,
// one after another (Fabric, then NeoForge of the same version), so the newest one ends up on top.
fun minecraftOrder(version: String) = version.split('.').map { it.toIntOrNull() ?: 0 }
    .let { parts -> parts.getOrElse(0) { 0 } * 1_000_000 + parts.getOrElse(1) { 0 } * 1_000 + parts.getOrElse(2) { 0 } }
stonecutter.versions
    // Forge before 1.17 and after 1.20.1 is uploaded from the builds in legacy/; these nodes have no upload tasks.
    .filter { project(":${it.project}").buildFile.name != "build.forge-legacy.gradle.kts" }
    .sortedWith(compareBy({ minecraftOrder(it.version) }, { it.project.endsWith("-neoforge") }, { it.project.endsWith("-forge") }))
    .map { it.project }
    .zipWithNext { older, newer ->
    for (task in listOf("publishModrinth", "publishCurseforge")) {
        project(":$newer").tasks.matching { it.name == task }.configureEach {
            mustRunAfter(":$older:$task")
        }
    }
}

stonecutter parameters {
    // "//? if fabric {" / "//? if neoforge {" / "//? if forge {" pick the code for the mod loader being built.
    // NeoForge before 1.20.5 still has Forge's API under NeoForge's names, so those versions build the
    // Forge code, with the names rewritten below.
    val oldNeoForge = current.project.endsWith("-neoforge") && current.parsed < "1.20.5"
    val loader = when {
        oldNeoForge -> "forge"
        current.project.endsWith("-neoforge") -> "neoforge"
        current.project.endsWith("-forge") -> "forge"
        else -> "fabric"
    }
    constants.match(loader, "fabric", "neoforge", "forge")

    // Names Mojang changed in 26.1 (render... -> extract...). The source uses the new names;
    // older versions get the old ones written back automatically.
    replacements {
        string(current.parsed >= "26.1") {
            replace("GuiGraphics", "GuiGraphicsExtractor")
            replace("renderContents", "extractContents")
            replace("renderToast", "extractToast")
            replace(".drawString(font,", ".text(font,")
            replace(".renderItem(track", ".item(track")
            replace("ScreenEvents.afterRender", "ScreenEvents.afterExtract")
            replace("keybinding.v1.KeyBindingHelper", "keymapping.v1.KeyMappingHelper")
            replace("KeyBindingHelper.registerKeyBinding", "KeyMappingHelper.registerKeyMapping")
        }

        // Mojang renamed ResourceLocation to Identifier in 1.21.11.
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }

        // Before 1.21 ids are created with the constructor. A regex, so it neither depends on nor
        // blocks the Identifier rename above. withDefaultNamespace is handled with plain conditions.
        regex(current.parsed >= "1.21") {
            replace(
                "new (ResourceLocation|Identifier)\\(", "\$1.fromNamespaceAndPath(",
                "(?:ResourceLocation|Identifier)\\.fromNamespaceAndPath\\(", "new ResourceLocation(",
            )
        }

        // Before 1.19 texts are created with their classes' constructors, and the "Caption: value" label
        // helper only exists on the option classes (see McCompat.genericValueLabel).
        string(current.parsed < "1.19") {
            replace("Component.translatable(", "new net.minecraft.network.chat.TranslatableComponent(")
            replace("Component.literal(", "new net.minecraft.network.chat.TextComponent(")
            replace("Component.empty()", "new net.minecraft.network.chat.TextComponent(\"\")")
            replace("Options.genericValueLabel(", "de.bettermusictoast.compat.McCompat.genericValueLabel(")
        }

        // Before 1.16.2 tooltips and wrapped text are lists of FormattedText (FormattedCharSequence came later).
        string(current.parsed < "1.16.2") {
            replace("net.minecraft.util.FormattedCharSequence", "net.minecraft.network.chat.FormattedText")
        }

        // Math.clamp only exists since Java 21 (Minecraft 1.20.5); older versions use Minecraft's own.
        string(current.parsed >= "1.20.5") {
            replace("Mth.clamp(", "Math.clamp(")
        }

        // Forge's names as NeoForge before 1.20.5 has them (see oldNeoForge above). One way only: the way
        // back ("(?!)" never matches) must not touch the real NeoForge code of every other version.
        // So never make one of these nodes the active version.
        regex(oldNeoForge) {
            replace("net\\.minecraftforge\\.(fml|api)\\.", "net.neoforged.\$1.", "(?!)1", "-")
            replace("net\\.minecraftforge\\.eventbus\\.api\\.", "net.neoforged.bus.api.", "(?!)2", "-")
            replace("net\\.minecraftforge\\.(client|common|event)\\.", "net.neoforged.neoforge.\$1.", "(?!)3", "-")
            replace("\\bMinecraftForge\\b", "NeoForge", "(?!)4", "-")
        }

        // NeoForge kept the method name ScreenEvent.Render.getGuiGraphics() in 26.1, so undo the
        // rename above for it. Always on, so it never runs the other way round.
        string(true) {
            replace("getGuiGraphicsExtractor()", "getGuiGraphics()")
        }
    }
}
