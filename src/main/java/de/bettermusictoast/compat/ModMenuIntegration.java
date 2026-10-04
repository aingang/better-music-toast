package de.bettermusictoast.compat;

// Mod Menu only exists on Fabric; NeoForge's mod list gets the settings in BetterMusicToastClient.
//? if fabric {
//? if >=1.17 {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
//?} else {
/*// Mod Menu for 1.16.0 – 1.16.3 only has its original API; its later 1.16 builds still support it too.
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
*///?}
import de.bettermusictoast.config.ConfigScreen;

public final class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigScreen::new;
	}
}
//?}
