package de.bettermusictoast.track;

import de.bettermusictoast.BetterMusicToast;
import de.bettermusictoast.compat.SoundManagerAccess;
import de.bettermusictoast.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

/**
 * Keeps track of the music / music-disc sounds the sound engine starts, what is playing and
 * whether the HUD panel should currently be visible.
 */
public final class NowPlayingTracker {
	/** A freshly started sound may not report as active right away. */
	private static final long START_GRACE_MS = 1500;
	private static final long PREVIEW_MS = 4000;
	private static final long SETTINGS_PREVIEW_MS = 5000;

	/** Started, but the file it plays is only known once the sound engine has set it up. */
	private ISound pending;
	private SoundManager pendingManager;
	private boolean pendingDisc;

	private ISound instance;
	private SoundCategory source;
	private TrackInfo track;
	private long startedAt;
	private boolean ended;
	/** Time the panel has actually been on screen; the timer pauses while it is blocked. */
	private long shownMs;
	private long previewUntil;
	private long settingsPreviewUntil;

	/** Called by Forge whenever the sound engine starts a sound. */
	public void onPlaySound(SoundManager manager, ISound sound) {
		SoundEventAccessorComposite event = manager.sndHandler.getSound(sound.getSoundLocation());
		if (event == null) {
			return;
		}
		SoundCategory category = event.getSoundCategory();
		if (category == SoundCategory.MUSIC) {
			setPending(manager, sound, false);
		} else if (category == SoundCategory.RECORDS && BetterMusicToast.config().showMusicDiscs && isInEarshot(sound)) {
			setPending(manager, sound, true);
		}
	}

	private void setPending(SoundManager manager, ISound sound, boolean disc) {
		pending = sound;
		pendingManager = manager;
		pendingDisc = disc;
	}

	/** Forge reports the sound a moment before the sound engine records which file it picked. */
	private void resolvePending() {
		if (pending == null) {
			return;
		}
		ISound sound = pending;
		boolean disc = pendingDisc;
		SoundPoolEntry entry = ((SoundManagerAccess) pendingManager).bettermusictoast$getEntry(sound);
		pending = null;
		pendingManager = null;
		if (entry == null) {
			return;
		}
		instance = sound;
		source = disc ? SoundCategory.RECORDS : SoundCategory.MUSIC;
		track = TrackResolver.resolve(entry.getSoundPoolEntryLocation(), disc);
		startedAt = now();
		ended = false;
		shownMs = 0;
		previewUntil = 0;
	}

	/** Same range the sound engine uses: 16 blocks, more for louder sounds (jukeboxes: 64). */
	private static boolean isInEarshot(ISound sound) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer == null || sound.getAttenuationType() == ISound.AttenuationType.NONE) {
			return true;
		}
		double range = 16.0 * Math.max(sound.getVolume(), 1.0f);
		return mc.thePlayer.getDistanceSq(sound.getXPosF(), sound.getYPosF(), sound.getZPosF()) <= range * range;
	}

	/** Called every client tick. */
	public void tick() {
		resolvePending();
		if (instance != null && !ended && now() - startedAt > START_GRACE_MS
				&& !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(instance)) {
			ended = true;
		}
	}

	/** Whether the panel wants to be shown right now (ignoring HUD obstacles). */
	public boolean wantsVisible() {
		resolvePending();
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
		resolvePending();
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
