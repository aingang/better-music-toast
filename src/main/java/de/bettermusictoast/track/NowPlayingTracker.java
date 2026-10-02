package de.bettermusictoast.track;

import de.bettermusictoast.BetterMusicToastClient;
import de.bettermusictoast.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

/**
 * Listens to the sound engine for music / music-disc sounds and keeps track of what is
 * playing and whether the HUD panel should currently be visible.
 */
public final class NowPlayingTracker implements SoundEventListener {
	/** A freshly started sound may not report as active right away. */
	private static final long START_GRACE_MS = 1500;
	private static final long PREVIEW_MS = 4000;

	private SoundInstance instance;
	private SoundSource source;
	private TrackInfo track;
	private long startedAt;
	private boolean ended;
	/** Time the panel has actually been on screen; the timer pauses while it is blocked. */
	private long shownMs;
	private long previewUntil;
	private long vanillaToastShownAt = Long.MIN_VALUE;

	@Override
	public void onPlaySound(SoundInstance sound, WeighedSoundEvents events, float range) {
		SoundSource src = sound.getSource();
		if (src == SoundSource.MUSIC) {
			start(sound, false);
		} else if (src == SoundSource.RECORDS && BetterMusicToastClient.config().showMusicDiscs && isInEarshot(sound, range)) {
			start(sound, true);
		}
	}

	private void start(SoundInstance sound, boolean disc) {
		TrackInfo info = TrackResolver.resolve(sound, disc);
		if (info == null) {
			return;
		}
		instance = sound;
		source = sound.getSource();
		track = info;
		startedAt = now();
		ended = false;
		shownMs = 0;
		previewUntil = 0;
	}

	private static boolean isInEarshot(SoundInstance sound, float range) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || sound.isRelative() || range == Float.POSITIVE_INFINITY) {
			return true;
		}
		return mc.player.distanceToSqr(sound.getX(), sound.getY(), sound.getZ()) <= (double) range * range;
	}

	/** Called every client tick. */
	public void tick() {
		if (instance != null && !ended && now() - startedAt > START_GRACE_MS
				&& !Minecraft.getInstance().getSoundManager().isActive(instance)) {
			ended = true;
		}
	}

	/** Whether the panel wants to be shown right now (ignoring HUD obstacles). */
	public boolean wantsVisible() {
		if (track == null) {
			return false;
		}
		if (previewUntil != 0) {
			return now() < previewUntil;
		}
		ModConfig config = BetterMusicToastClient.config();
		if (!config.enabled || ended) {
			return false;
		}
		if (Minecraft.getInstance().options.getFinalSoundSourceVolume(source) <= 0.0f) {
			return false;
		}
		return config.displayMode == ModConfig.DisplayMode.WHOLE_SONG || shownMs < config.durationSeconds * 1000L;
	}

	public void addShownTime(long ms) {
		shownMs += ms;
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
		track = new TrackInfo(I18n.get("bettermusictoast.preview.title"), I18n.get("bettermusictoast.preview.artist"), ItemStack.EMPTY);
		instance = null;
		ended = false;
		previewUntil = now() + PREVIEW_MS;
	}

	public void onVanillaToastShown() {
		vanillaToastShownAt = now();
	}

	/** Vanilla's music toast stays up for 5 s scaled by the "notification time" option. */
	public boolean isVanillaToastVisible() {
		if (vanillaToastShownAt == Long.MIN_VALUE) {
			return false;
		}
		double multiplier = Minecraft.getInstance().options.notificationDisplayTime().get();
		return now() - vanillaToastShownAt < 5000 * multiplier + 700;
	}

	private static long now() {
		return System.nanoTime() / 1_000_000L;
	}
}
