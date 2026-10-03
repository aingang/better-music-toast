package de.bettermusictoast.compat;

// Mod Menu only exists on Fabric; NeoForge's mod list gets the settings in BetterMusicToastClient.
//? if fabric {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.bettermusictoast.config.ConfigScreen;

public final class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigScreen::new;
	}
}
//?}
