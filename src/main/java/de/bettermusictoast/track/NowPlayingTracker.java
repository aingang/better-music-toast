package de.bettermusictoast.track;

import de.bettermusictoast.BetterMusicToastClient;
import de.bettermusictoast.compat.McCompat;
import de.bettermusictoast.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
//? if >=1.19
import net.minecraft.network.chat.contents.TranslatableContents;
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
	private static final long SETTINGS_PREVIEW_MS = 5000;
	/** How far apart a disc and its "Now Playing" message may start to belong together. */
	private static final long NOW_PLAYING_MS = 3000;

	private SoundInstance instance;
	private SoundSource source;
	private TrackInfo track;
	private long startedAt;
	private boolean ended;
	/** Time the panel has actually been on screen; the timer pauses while it is blocked. */
	private long shownMs;
	private long previewUntil;
	private long settingsPreviewUntil;
	private long vanillaToastShownAt = Long.MIN_VALUE;
	/** Whether the song shown comes from a mod with its own music player (see {@link ExternalMusic}). */
	private boolean external;
	private String externalKey;
	private Component lastOverlayMessage;
	private String nowPlayingText;
	private long nowPlayingAt;

	//? if <1.20.3 {
	/*// Before 1.20.3 listeners are not told how far a sound carries; this is how the sound engine
	// works it out for them in 1.20.3+.
	@Override
	public void onPlaySound(SoundInstance sound, WeighedSoundEvents events) {
		onPlaySound(sound, events, Math.max(sound.getVolume(), 1.0f) * (float) sound.getSound().getAttenuationDistance());
	}

	private void onPlaySound(SoundInstance sound, WeighedSoundEvents events, float range) {
	*///?} else {
	@Override
	public void onPlaySound(SoundInstance sound, WeighedSoundEvents events, float range) {
	//?}
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
		if (disc && info.guessed() && nowPlayingText != null && now() - nowPlayingAt < NOW_PLAYING_MS) {
			info = named(nowPlayingText, info.icon());
		}
		instance = sound;
		external = false;
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
		checkEnded();
		readNowPlayingMessage();
		readExternalMusic();
	}

	/** Follows the songs of mods that play music past Minecraft's sound engine (e.g. Reactive Music). */
	private void readExternalMusic() {
		ExternalMusic.Song song = ExternalMusic.current();
		if (song == null || !song.playing) {
			if (external && !ended && previewUntil == 0) {
				ended = true;
			}
			if (song == null) {
				// The same song starting again later is a new start.
				externalKey = null;
			}
			return;
		}
		boolean newSong = !song.key.equals(externalKey);
		externalKey = song.key;
		// A music disc keeps the box while the other mod plays quietly underneath; a resumed song comes back.
		boolean discPlaying = !external && track != null && !ended && previewUntil == 0 && source == SoundSource.RECORDS;
		if ((newSong && !discPlaying) || (external && ended && previewUntil == 0)) {
			instance = null;
			external = true;
			source = song.musicVolume ? SoundSource.MUSIC : SoundSource.MASTER;
			track = new TrackInfo(song.title, song.artist, ItemStack.EMPTY);
			startedAt = now();
			ended = false;
			shownMs = 0;
			previewUntil = 0;
		}
	}

	/**
	 * Mods that stream music discs (e.g. Net Music) only name the song in the jukebox's "Now Playing: …"
	 * message. That name replaces one guessed from the file, whether the message comes before or after the sound.
	 */
	private void readNowPlayingMessage() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gui == null) {
			return;
		}
		Component message = McCompat.hud(mc).bettermusictoast$getOverlayMessage();
		if (message == lastOverlayMessage) {
			return;
		}
		lastOverlayMessage = message;
		String name = nowPlayingName(message);
		if (name == null) {
			return;
		}
		nowPlayingText = name;
		nowPlayingAt = now();
		if (track != null && source == SoundSource.RECORDS && track.guessed() && !ended && previewUntil == 0
				&& now() - startedAt < NOW_PLAYING_MS) {
			track = named(name, track.icon());
		}
	}

	/** The song in a "Now Playing: …" message, or null if the action bar shows something else. */
	private static String nowPlayingName(Component message) {
		if (message == null) {
			return null;
		}
		//? if >=1.19 {
		if (!(message.getContents() instanceof TranslatableContents)) {
			return null;
		}
		TranslatableContents contents = (TranslatableContents) message.getContents();
		//?} else {
		/*if (!(message instanceof net.minecraft.network.chat.TranslatableComponent)) {
			return null;
		}
		net.minecraft.network.chat.TranslatableComponent contents = (net.minecraft.network.chat.TranslatableComponent) message;
		*///?}
		if (!contents.getKey().equals("record.nowPlaying") || contents.getArgs().length == 0) {
			return null;
		}
		Object song = contents.getArgs()[0];
		String name = song instanceof Component ? ((Component) song).getString() : String.valueOf(song);
		return name.trim().isEmpty() ? null : name.trim();
	}

	private static TrackInfo named(String text, ItemStack icon) {
		String[] parts = TrackResolver.splitArtist(text);
		return parts != null ? new TrackInfo(parts[1], parts[0], icon) : new TrackInfo(text, null, icon);
	}

	/**
	 * Notices when the song has stopped. Also called while drawing: loading a world stops all sounds
	 * and then only draws the loading screen without ticking, so the box would keep the old song.
	 */
	public void checkEnded() {
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
		ModConfig config = BetterMusicToastClient.config();
		if (isSettingsPreview()) {
			return config.enabled;
		}
		if (previewUntil != 0) {
			return now() < previewUntil;
		}
		if (!config.enabled || ended) {
			return false;
		}
		//? if >=1.21.6 {
		if (Minecraft.getInstance().options.getFinalSoundSourceVolume(source) <= 0.0f) {
		//?} else {
		/*// Same as getFinalSoundSourceVolume, which 1.21.6 added: the source volume times master.
		net.minecraft.client.Options options = Minecraft.getInstance().options;
		if (options.getSoundSourceVolume(source) * options.getSoundSourceVolume(SoundSource.MASTER) <= 0.0f) {
		*///?}
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
		track = new TrackInfo(I18n.get("bettermusictoast.preview.title"), I18n.get("bettermusictoast.preview.artist"), ItemStack.EMPTY);
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
			track = new TrackInfo(I18n.get("bettermusictoast.preview.title"), I18n.get("bettermusictoast.preview.artist"), ItemStack.EMPTY);
			instance = null;
			ended = false;
			previewUntil = now() + SETTINGS_PREVIEW_MS;
		}
		settingsPreviewUntil = now() + SETTINGS_PREVIEW_MS;
	}

	public boolean isSettingsPreview() {
		return track != null && now() < settingsPreviewUntil;
	}

	public void onVanillaToastShown() {
		vanillaToastShownAt = now();
	}

	/** Vanilla's music toast stays up for 5 s scaled by the "notification time" option. */
	public boolean isVanillaToastVisible() {
		if (vanillaToastShownAt == Long.MIN_VALUE) {
			return false;
		}
		//? if >=1.19.4 {
		double multiplier = Minecraft.getInstance().options.notificationDisplayTime().get();
		//?} else
		/*double multiplier = 1.0;*/
		return now() - vanillaToastShownAt < 5000 * multiplier + 700;
	}

	private static long now() {
		return System.nanoTime() / 1_000_000L;
	}
}
