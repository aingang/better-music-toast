package de.bettermusictoast;

import com.mojang.blaze3d.platform.InputConstants;
import de.bettermusictoast.compat.McCompat;
import de.bettermusictoast.config.ConfigScreen;
import de.bettermusictoast.config.ModConfig;
import de.bettermusictoast.hud.NowPlayingHud;
import de.bettermusictoast.track.NowPlayingTracker;
//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//? if >=1.21.6 {
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
//?} else if >=1.21.4 {
/*import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
*///?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
*///?}
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
//?} else {
/*import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
*///?}
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

//? if fabric {
public final class BetterMusicToastClient implements ClientModInitializer {
//?} else {
/*@Mod(value = BetterMusicToastClient.MOD_ID, dist = Dist.CLIENT)
public final class BetterMusicToastClient {
*///?}
	public static final String MOD_ID = "bettermusictoast";

	private static ModConfig config;
	private static final NowPlayingTracker TRACKER = new NowPlayingTracker();

	public static ModConfig config() {
		return config;
	}

	public static NowPlayingTracker tracker() {
		return TRACKER;
	}

	//? if fabric {
	@Override
	public void onInitializeClient() {
		config = ModConfig.load();

		// Key categories became objects in 1.21.9; before that a category is its translation key.
		//? if >=1.21.9 {
		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		//?} else {
		/*String category = "key.category." + MOD_ID + ".main";
		*///?}
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
				McCompat.setScreen(client, new ConfigScreen(McCompat.screen(client)));
			}
		});

		NowPlayingHud hud = new NowPlayingHud();
		//? if >=1.21.6 {
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "now_playing"), hud);
		//?} else if >=1.21.4 {
		/*// Before 1.21.6 Fabric adds HUD parts as layers; addLayer puts ours on top, like addLast.
		HudLayerRegistrationCallback.EVENT.register(layers ->
				layers.addLayer(IdentifiedLayer.of(Identifier.fromNamespaceAndPath(MOD_ID, "now_playing"), hud)));
		*///?} else {
		/*// Before 1.21.4 Fabric has no HUD layers yet; this callback draws after the whole HUD.
		HudRenderCallback.EVENT.register(hud::render);
		*///?}
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
				ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, delta) -> hud.extractOverScreen(graphics)));
	}
	//?} else {
	/*// NeoForge: the same setup as on Fabric, through NeoForge's events.
	public BetterMusicToastClient(IEventBus modBus, ModContainer container) {
		config = ModConfig.load();

		String category = "key.category." + MOD_ID + ".main";
		KeyMapping showAgain = new KeyMapping("key.bettermusictoast.show", InputConstants.UNKNOWN.getValue(), category);
		KeyMapping openSettings = new KeyMapping("key.bettermusictoast.settings", InputConstants.UNKNOWN.getValue(), category);
		modBus.addListener((RegisterKeyMappingsEvent event) -> {
			event.register(showAgain);
			event.register(openSettings);
		});

		modBus.addListener((FMLClientSetupEvent event) ->
				event.enqueueWork(() -> Minecraft.getInstance().getSoundManager().addListener(TRACKER)));

		NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
			Minecraft client = Minecraft.getInstance();
			TRACKER.tick();
			while (showAgain.consumeClick()) {
				TRACKER.reshow();
			}
			while (openSettings.consumeClick()) {
				McCompat.setScreen(client, new ConfigScreen(McCompat.screen(client)));
			}
		});

		// On top of every other HUD part, like addLast on Fabric.
		NowPlayingHud hud = new NowPlayingHud();
		modBus.addListener((RegisterGuiLayersEvent event) ->
				event.registerAboveAll(Identifier.fromNamespaceAndPath(MOD_ID, "now_playing"), hud));
		NeoForge.EVENT_BUS.addListener((ScreenEvent.Render.Post event) -> hud.extractOverScreen(event.getGuiGraphics()));

		// Settings button in NeoForge's own mod list (Mod Menu does this on Fabric).
		container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new ConfigScreen(parent));
	}
	*///?}
}
