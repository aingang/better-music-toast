package de.bettermusictoast.test;

import de.bettermusictoast.BetterMusicToastClient;
import de.bettermusictoast.config.ConfigScreen;
import de.bettermusictoast.config.ModConfig;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Walks through every position and obstacle case and takes a screenshot of each. */
public class NowPlayingClientGameTest implements FabricClientGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger("bettermusictoast-test");

	@Override
	public void runTest(ClientGameTestContext context) {
		ModConfig config = BetterMusicToastClient.config();
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getServer().runCommand("time set noon");
			singleplayer.getServer().runCommand("weather clear");
			context.waitTicks(60);

			// 1. Real game music, default position (top left).
			context.runOnClient(mc -> {
				LOGGER.info("Volumes before: master={} music={}", mc.options.getSoundSourceVolume(SoundSource.MASTER),
						mc.options.getSoundSourceVolume(SoundSource.MUSIC));
				mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(0.05);
				mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(0.05);
				config.position = ModConfig.Position.TOP_LEFT;
				config.avoidMode = ModConfig.AvoidMode.MOVE;
				config.displayMode = ModConfig.DisplayMode.WHOLE_SONG;
				mc.getMusicManager().startPlaying(Musics.GAME);
			});
			context.waitTicks(50);
			context.runOnClient(mc -> LOGGER.info("Tracked track: {} (music key {}), wantsVisible={}, active={}",
					BetterMusicToastClient.tracker().track(), mc.getMusicManager().getCurrentMusicTranslationKey(),
					BetterMusicToastClient.tracker().wantsVisible(), mc.getMusicManager().isPlayingMusic(Musics.GAME)));
			if (context.computeOnClient(mc -> !BetterMusicToastClient.tracker().wantsVisible())) {
				LOGGER.warn("No music detected (sound engine unavailable?), falling back to preview");
				context.runOnClient(mc -> BetterMusicToastClient.tracker().preview());
				context.waitTicks(20);
			}
			context.takeScreenshot("npt_01_top_left");

			// 2. Top right with an advancement-like toast and effect icons: must move below them.
			singleplayer.getServer().runCommand("effect give @a minecraft:speed 120");
			singleplayer.getServer().runCommand("effect give @a minecraft:slowness 120");
			context.runOnClient(mc -> {
				config.position = ModConfig.Position.TOP_RIGHT;
				SystemToast.add(mc.gui.toastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
						Component.literal("Advancement Made!"), Component.literal("Stone Age"));
			});
			context.waitTicks(25);
			context.takeScreenshot("npt_02_top_right_toast_effects");

			// 3. Top center under a boss bar.
			singleplayer.getServer().runCommand("effect clear @a");
			singleplayer.getServer().runCommand("bossbar add npt:test \"Ender Dragon\"");
			singleplayer.getServer().runCommand("bossbar set npt:test players @a");
			context.runOnClient(mc -> config.position = ModConfig.Position.TOP_CENTER);
			context.waitTicks(25);
			context.takeScreenshot("npt_03_top_center_bossbar");
			singleplayer.getServer().runCommand("bossbar remove npt:test");

			// 4. Above the hotbar while an action bar message is shown, then after it is gone.
			context.runOnClient(mc -> {
				config.position = ModConfig.Position.ABOVE_HOTBAR;
				mc.gui.hud.setOverlayMessage(Component.literal("Action bar message"), false);
			});
			context.waitTicks(20);
			context.takeScreenshot("npt_04_hotbar_actionbar");
			context.waitTicks(80);
			context.takeScreenshot("npt_05_hotbar_clear");

			// 5. "Hide briefly" mode: a toast covers the top right, panel fades out.
			context.runOnClient(mc -> {
				config.position = ModConfig.Position.TOP_RIGHT;
				config.avoidMode = ModConfig.AvoidMode.HIDE;
				SystemToast.add(mc.gui.toastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
						Component.literal("Recipe Unlocked"), Component.literal("Hidden test"));
			});
			context.waitTicks(25);
			context.takeScreenshot("npt_06_hide_mode");

			// 6. Settings screen.
			context.runOnClient(mc -> config.avoidMode = ModConfig.AvoidMode.MOVE);
			context.setScreen(() -> new ConfigScreen(null));
			context.waitTicks(5);
			context.takeScreenshot("npt_07_config_screen");
			context.setScreen(() -> null);

			// 7. Large size, top left, after closing settings (preview path).
			context.runOnClient(mc -> {
				config.position = ModConfig.Position.TOP_LEFT;
				config.size = ModConfig.Size.LARGE;
			});
			context.waitTicks(20);
			context.takeScreenshot("npt_08_top_left_large");
			context.runOnClient(mc -> config.size = ModConfig.Size.NORMAL);

			// 7b. A music disc playing in a nearby jukebox (shows the disc item as icon).
			context.runOnClient(mc -> {
				mc.options.getSoundSourceOptionInstance(SoundSource.RECORDS).set(0.05);
				mc.getSoundManager().play(SimpleSoundInstance.forJukeboxSong(
						SoundEvents.MUSIC_DISC_CAT.value(), mc.player.position().add(2, 0, 0)));
			});
			context.waitTicks(30);
			context.takeScreenshot("npt_08b_music_disc");

			// 8. German settings screen.
			context.runOnClient(mc -> {
				mc.options.languageCode = "de_de";
				mc.getLanguageManager().setSelected("de_de");
				mc.reloadResourcePacks();
			});
			context.waitTicks(100);
			context.setScreen(() -> new ConfigScreen(null));
			context.waitTicks(5);
			context.takeScreenshot("npt_09_config_screen_de");
			context.setScreen(() -> null);
		}
	}
}
