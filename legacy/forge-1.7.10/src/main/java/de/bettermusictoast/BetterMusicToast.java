package de.bettermusictoast;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
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
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

/** The client side of the mod, started by {@link BetterMusicToastMod}. */
public final class BetterMusicToast {
	public static final String MOD_ID = "bettermusictoast";

	private static ModConfig config;
	private static final NowPlayingTracker TRACKER = new NowPlayingTracker();

	private final NowPlayingHud hud = new NowPlayingHud();
	private final KeyBinding showAgain;
	private final KeyBinding openSettings;

	private BetterMusicToast() {
		String category = "key.category." + MOD_ID + ".main";
		showAgain = new KeyBinding("key.bettermusictoast.show", Keyboard.KEY_NONE, category);
		openSettings = new KeyBinding("key.bettermusictoast.settings", Keyboard.KEY_NONE, category);
		ClientRegistry.registerKeyBinding(showAgain);
		ClientRegistry.registerKeyBinding(openSettings);
	}

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

	static void init() {
		config();
		BetterMusicToast mod = new BetterMusicToast();
		// Sounds, HUD and screens come from Forge's event bus, ticks from FML's.
		MinecraftForge.EVENT_BUS.register(mod);
		FMLCommonHandler.instance().bus().register(mod);
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
			SoundAccess.musicTicker(mc).update();
		}
		TRACKER.tick();
		while (showAgain.isPressed()) {
			TRACKER.reshow();
		}
		while (openSettings.isPressed()) {
			mc.displayGuiScreen(new ConfigScreen(mc.currentScreen));
		}
	}

	// Both events hide SoundEvent.manager behind an old field of the same name, which the streaming
	// event (music, music discs) always leaves null; so the manager is read through SoundEvent.
	@SubscribeEvent
	public void onStreamingSound(PlayStreamingSourceEvent event) {
		TRACKER.onPlaySound(((SoundEvent) event).manager, event.sound);
	}

	@SubscribeEvent
	public void onSound(PlaySoundSourceEvent event) {
		TRACKER.onPlaySound(((SoundEvent) event).manager, event.sound);
	}

	// On top of the whole HUD.
	@SubscribeEvent
	public void onHud(RenderGameOverlayEvent.Post event) {
		if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
			hud.renderHud();
		}
	}

	@SubscribeEvent
	public void onScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
		hud.renderOverScreen();
	}
}
