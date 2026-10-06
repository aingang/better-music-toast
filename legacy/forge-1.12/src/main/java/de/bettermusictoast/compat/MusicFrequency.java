package de.bettermusictoast.compat;

import de.bettermusictoast.BetterMusicToast;
import java.util.Locale;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;

/**
 * Minecraft's "Music Frequency" option from 1.21.6+, brought to these versions by the mod: same
 * values, timings and texts.
 */
public enum MusicFrequency {
	DEFAULT(20),
	FREQUENT(10),
	CONSTANT(0);

	// Longest pause between two songs, in ticks.
	private final int maxDelay;
	private final String key;

	MusicFrequency(int maxMinutes) {
		this.maxDelay = maxMinutes * 1200;
		this.key = "options.music_frequency." + name().toLowerCase(Locale.ROOT);
	}

	public String translationKey() {
		return key;
	}

	// Ticks until the next song, like MusicManager.MusicFrequency.getNextSongDelay in 1.21.6+.
	public int nextSongDelay(MusicTicker.MusicType music, Random random) {
		if (music == null) {
			return maxDelay;
		}
		if (this == CONSTANT) {
			return 100;
		}
		int min = Math.min(music.getMinDelay(), maxDelay);
		int max = Math.min(music.getMaxDelay(), maxDelay);
		return min >= max ? min : min + random.nextInt(max - min + 1);
	}

	public static MusicFrequency current() {
		return BetterMusicToast.config().musicFrequency;
	}

	/**
	 * Called every tick before Minecraft updates the music. The remaining pause is capped, like
	 * 1.21.6+ does, so a new choice or a change of music (e.g. entering the Nether) never waits
	 * longer than the frequency allows. "Default" leaves Minecraft's own timing untouched.
	 */
	public static void capPause(Minecraft mc) {
		MusicFrequency frequency = current();
		MusicTicker ticker = mc.getMusicTicker();
		if (frequency != DEFAULT && ticker != null && !SoundAccess.isMusicPlaying(ticker)) {
			int delay = frequency.nextSongDelay(mc.getAmbientMusicType(), SoundAccess.musicRandom(ticker));
			if (delay < SoundAccess.timeUntilNextMusic(ticker)) {
				SoundAccess.setTimeUntilNextMusic(ticker, delay);
			}
		}
	}

	// Like MusicManager.setMinutesBetweenSongs in 1.21.6+: a new choice applies to the current pause.
	public static void set(MusicFrequency frequency) {
		BetterMusicToast.config().musicFrequency = frequency;
		BetterMusicToast.config().save();
		Minecraft mc = Minecraft.getMinecraft();
		MusicTicker ticker = mc.getMusicTicker();
		SoundAccess.setTimeUntilNextMusic(ticker, frequency.nextSongDelay(mc.getAmbientMusicType(), SoundAccess.musicRandom(ticker)));
	}
}
