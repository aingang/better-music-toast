package de.bettermusictoast.track;

import de.bettermusictoast.BetterMusicToast;
import de.bettermusictoast.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.SoundCategory;

/**
 * Keeps track of the music / music-disc sounds the sound engine starts, what is playing and
 * whether the HUD panel should currently be visible.
 */
public final class NowPlayingTracker {
	/** A freshly started sound may not report as active right away. */
	private static final long START_GRACE_MS = 1500;
	private static final long PREVIEW_MS = 4000;
	private static final long SETTINGS_PREVIEW_MS = 5000;

	private ISound instance;
	private SoundCategory source;
	private TrackInfo track;
	private long startedAt;
	private boolean ended;
	/** Time the panel has actually been on screen; the timer pauses while it is blocked. */
	private long shownMs;
	private long previewUntil;
	private long settingsPreviewUntil;

	/** Called by Forge whenever the sound engine starts a sound; the file it picked is known by then. */
	public void onPlaySound(ISound sound) {
		Sound file = sound.getSound();
		if (file == null) {
			return;
		}
		SoundCategory category = sound.getCategory();
		boolean disc;
		if (category == SoundCategory.MUSIC) {
			disc = false;
		} else if (category == SoundCategory.RECORDS && BetterMusicToast.config().showMusicDiscs && isInEarshot(sound)) {
			disc = true;
		} else {
			return;
		}
		instance = sound;
		source = category;
		track = TrackResolver.resolve(file.getSoundLocation(), sound.getSoundLocation(), disc);
		startedAt = now();
		ended = false;
		shownMs = 0;
		previewUntil = 0;
	}

	/** Same range the sound engine uses: 16 blocks, more for louder sounds (jukeboxes: 64). */
	private static boolean isInEarshot(ISound sound) {
		Minecraft mc = Minecraft.getMinecraft();
		//#if MC>=11002
		if (mc.player == null || sound.getAttenuationType() == ISound.AttenuationType.NONE) {
		//#else
		//$$ if (mc.thePlayer == null || sound.getAttenuationType() == ISound.AttenuationType.NONE) {
		//#endif
			return true;
		}
		double range = 16.0 * Math.max(sound.getVolume(), 1.0f);
		//#if MC>=11002
		return mc.player.getDistanceSq(sound.getXPosF(), sound.getYPosF(), sound.getZPosF()) <= range * range;
		//#else
		//$$ return mc.thePlayer.getDistanceSq(sound.getXPosF(), sound.getYPosF(), sound.getZPosF()) <= range * range;
		//#endif
	}

	/** Called every client tick. */
	public void tick() {
		checkEnded();
	}

	/**
	 * Notices when the song has stopped. Also called while drawing: loading a world stops all sounds
	 * and may draw a loading screen without ticking, so the box would keep the old song.
	 */
	public void checkEnded() {
		if (instance != null && !ended && now() - startedAt > START_GRACE_MS
				&& !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(instance)) {
			ended = true;
		}
	}

	/** Whether the panel wants to be shown right now (ignoring HUD obstacles). */
	public boolean wantsVisible() {
		if (track == null) {
			return false;
		}
		ModConfig config = BetterMusicToast.config();
		if (isSettingsPreview()) {
			return config.enabled;
		}
		if (previewUntil != 0) {
			return now() < previewUntil;
		}
		if (!config.enabled || ended) {
			return false;
		}
		GameSettings options = Minecraft.getMinecraft().gameSettings;
		if (options.getSoundLevel(source) * options.getSoundLevel(SoundCategory.MASTER) <= 0.0f) {
			return false;
		}
		return config.displayMode == ModConfig.DisplayMode.WHOLE_SONG || shownMs < config.durationSeconds * 1000L;
	}

	public void addShownTime(long ms) {
		// Looking at the settings preview should not use up the song's display time.
		if (!isSettingsPreview()) {
			shownMs += ms;
		}
	}

	public TrackInfo track() {
		return track;
	}

	/** Clears the finished track once the panel has faded out. */
	public void clearIfDone() {
		if (!wantsVisible() && (ended || previewUntil != 0)) {
			track = null;
			instance = null;
			previewUntil = 0;
		}
	}

	/** Shows the current song again (key binding / after changing settings). */
	public void reshow() {
		if (track != null && !ended && previewUntil == 0) {
			shownMs = 0;
		}
	}

	/** Re-shows the current song, or a sample panel if nothing is playing, so settings changes are visible. */
	public void preview() {
		if (track != null && !ended && previewUntil == 0) {
			shownMs = 0;
			return;
		}
		track = sample();
		instance = null;
		ended = false;
		previewUntil = now() + PREVIEW_MS;
	}

	/**
	 * Shows the panel at its configured position for a few seconds while the settings screen is
	 * open, so changes are visible right away: the current song, or a sample if nothing plays.
	 */
	public void showSettingsPreview() {
		if (track == null || ended || previewUntil != 0) {
			track = sample();
			instance = null;
			ended = false;
			previewUntil = now() + SETTINGS_PREVIEW_MS;
		}
		settingsPreviewUntil = now() + SETTINGS_PREVIEW_MS;
	}

	public boolean isSettingsPreview() {
		return track != null && now() < settingsPreviewUntil;
	}

	private static TrackInfo sample() {
		return new TrackInfo(I18n.format("bettermusictoast.preview.title"), I18n.format("bettermusictoast.preview.artist"), null);
	}

	private static long now() {
		return System.nanoTime() / 1_000_000L;
	}
}
