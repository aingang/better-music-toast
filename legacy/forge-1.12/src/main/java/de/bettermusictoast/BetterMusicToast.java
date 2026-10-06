package de.bettermusictoast;

import de.bettermusictoast.compat.MusicFrequency;
import de.bettermusictoast.compat.SoundAccess;
import de.bettermusictoast.compat.SoundOptionsButtons;
import de.bettermusictoast.config.ConfigScreen;
import de.bettermusictoast.config.ModConfig;
import de.bettermusictoast.hud.NowPlayingHud;
import de.bettermusictoast.track.NowPlayingTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

/**
 * Better Music Toast for Minecraft 1.9.4 to 1.12.2 (Forge). Client only: on a server the mod is not
 * loaded and does nothing, and servers do not need it.
 */
@Mod(modid = BetterMusicToast.MOD_ID, useMetadata = true, clientSideOnly = true,
		acceptedMinecraftVersions = BuildInfo.MINECRAFT_VERSIONS, acceptableRemoteVersions = "*",
		guiFactory = "de.bettermusictoast.config.GuiFactory")
public final class BetterMusicToast {
	public static final String MOD_ID = "bettermusictoast";

	private static ModConfig config;
	private static final NowPlayingTracker TRACKER = new NowPlayingTracker();

	private final NowPlayingHud hud = new NowPlayingHud();
	private KeyBinding showAgain;
	private KeyBinding openSettings;

	public static ModConfig config() {
		// The core mod's hook can run even if Forge never started the mod (e.g. when another mod
		// failed to load), so never hand it a missing config.
		if (config == null) {
			config = ModConfig.load();
		}
		return config;
	}

	public static NowPlayingTracker tracker() {
		return TRACKER;
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		config();

		String category = "key.category." + MOD_ID + ".main";
		showAgain = new KeyBinding("key.bettermusictoast.show", Keyboard.KEY_NONE, category);
		openSettings = new KeyBinding("key.bettermusictoast.settings", Keyboard.KEY_NONE, category);
		ClientRegistry.registerKeyBinding(showAgain);
		ClientRegistry.registerKeyBinding(openSettings);

		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new SoundOptionsButtons());
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (event.phase == TickEvent.Phase.START) {
			// Before Minecraft updates the music in this tick.
			MusicFrequency.capPause(mc);
			return;
		}
		// Minecraft stops managing the music while the game is paused; since 1.21.6 it goes on
		// (next song, cleanup of ended songs), and so it does here. See PauseMenuTransformer.
		if (mc.isGamePaused()) {
			SoundAccess.cleanUpEndedMusic(mc.getSoundHandler());
			mc.getMusicTicker().update();
		}
		TRACKER.tick();
		while (showAgain.isPressed()) {
			TRACKER.reshow();
		}
		while (openSettings.isPressed()) {
			mc.displayGuiScreen(new ConfigScreen(mc.currentScreen));
		}
	}

	@SubscribeEvent
	public void onStreamingSound(PlayStreamingSourceEvent event) {
		TRACKER.onPlaySound(event.getSound());
	}

	@SubscribeEvent
	public void onSound(PlaySoundSourceEvent event) {
		TRACKER.onPlaySound(event.getSound());
	}

	// On top of the whole HUD.
	@SubscribeEvent
	public void onHud(RenderGameOverlayEvent.Post event) {
		if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
			hud.renderHud();
		}
	}

	@SubscribeEvent
	public void onScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
		hud.renderOverScreen();
	}
}
