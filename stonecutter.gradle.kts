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
    val loader = when {
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

        // Math.clamp only exists since Java 21 (Minecraft 1.20.5); older versions use Minecraft's own.
        string(current.parsed >= "1.20.5") {
            replace("Mth.clamp(", "Math.clamp(")
        }

        // NeoForge kept the method name ScreenEvent.Render.getGuiGraphics() in 26.1, so undo the
        // rename above for it. Always on, so it never runs the other way round.
        string(true) {
            replace("getGuiGraphicsExtractor()", "getGuiGraphics()")
        }
    }
}
