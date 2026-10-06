package de.bettermusictoast;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.relauncher.Side;

/**
 * Better Music Toast for Minecraft 1.7.10 (Forge). Client only: on a server the mod does nothing, and
 * servers do not need it. Forge 1.7.10 cannot mark a mod as client only yet, so this class stays free of
 * client classes and only starts {@link BetterMusicToast} on the client.
 */
@Mod(modid = BetterMusicToast.MOD_ID, useMetadata = true, acceptedMinecraftVersions = "[1.7.10]",
		acceptableRemoteVersions = "*", guiFactory = "de.bettermusictoast.config.GuiFactory")
public final class BetterMusicToastMod {
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if (event.getSide() == Side.CLIENT) {
			BetterMusicToast.init();
		}
	}
}
