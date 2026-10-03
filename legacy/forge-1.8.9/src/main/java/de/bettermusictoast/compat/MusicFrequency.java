package de.bettermusictoast.compat;

import de.bettermusictoast.BetterMusicToast;
import java.util.Locale;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.util.MathHelper;

/**
 * Minecraft's "Music Frequency" option from 1.21.6+, brought to 1.8.9 by the mod: same values,
 * timings and texts.
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
		return MathHelper.getRandomIntegerInRange(random, Math.min(music.getMinDelay(), maxDelay), Math.min(music.getMaxDelay(), maxDelay));
	}

	public static MusicFrequency current() {
		return BetterMusicToast.config().musicFrequency;
	}

	// Like MusicManager.setMinutesBetweenSongs in 1.21.6+: a new choice applies to the current pause.
	public static void set(MusicFrequency frequency) {
		BetterMusicToast.config().musicFrequency = frequency;
		BetterMusicToast.config().save();
		Minecraft mc = Minecraft.getMinecraft();
		MusicTickerAccess music = (MusicTickerAccess) mc.getMusicTicker();
		music.bettermusictoast$setTimeUntilNextMusic(frequency.nextSongDelay(mc.getAmbientMusicType(), music.bettermusictoast$getRandom()));
	}
}
