package de.bettermusictoast;

import com.mojang.blaze3d.platform.InputConstants;
import de.bettermusictoast.config.ConfigScreen;
import de.bettermusictoast.config.ModConfig;
import de.bettermusictoast.hud.NowPlayingHud;
import de.bettermusictoast.track.NowPlayingTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class BetterMusicToastClient implements ClientModInitializer {
	public static final String MOD_ID = "bettermusictoast";

	private static ModConfig config;
	private static final NowPlayingTracker TRACKER = new NowPlayingTracker();

	public static ModConfig config() {
		return config;
	}

	public static NowPlayingTracker tracker() {
		return TRACKER;
	}

	@Override
	public void onInitializeClient() {
		config = ModConfig.load();

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		KeyMapping showAgain = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.bettermusictoast.show", InputConstants.UNKNOWN.getValue(), category));
		KeyMapping openSettings = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.bettermusictoast.settings", InputConstants.UNKNOWN.getValue(), category));

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> client.getSoundManager().addListener(TRACKER));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			TRACKER.tick();
			while (showAgain.consumeClick()) {
				TRACKER.reshow();
			}
			while (openSettings.consumeClick()) {
				client.gui.setScreen(new ConfigScreen(client.gui.screen()));
			}
		});

		NowPlayingHud hud = new NowPlayingHud();
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "now_playing"), hud);
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
				ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, delta) -> hud.extractOverScreen(graphics)));
	}
}
