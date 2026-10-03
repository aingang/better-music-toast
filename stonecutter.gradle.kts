plugins {
    id("dev.kikugie.stonecutter")
}

// The version whose code is "live" in src/; code for other versions sits in comments.
stonecutter active "26.2"

// Modrinth lists versions by upload time: upload the oldest Minecraft version first, one after
// another, so the newest one ends up on top.
stonecutter.versions.map { it.project }.zipWithNext { older, newer ->
    project(":$newer").tasks.matching { it.name == "publishModrinth" }.configureEach {
        mustRunAfter(":$older:publishModrinth")
    }
}

stonecutter parameters {
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
    }
}
