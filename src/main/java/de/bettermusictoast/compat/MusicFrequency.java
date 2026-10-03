package de.bettermusictoast.compat;

// Minecraft added the "Music Frequency" option in 1.21.6; older versions get it from the mod.
//? if <1.21.6 {
/*import com.mojang.serialization.Codec;
import de.bettermusictoast.mixin.MusicManagerAccessor;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

// The same option as in Minecraft 1.21.6+: same values, timings and texts, and saved under the same
// name in options.txt, so the choice carries over when updating Minecraft.
public enum MusicFrequency implements OptionEnum, StringRepresentable {
	DEFAULT(0, 20),
	FREQUENT(1, 10),
	CONSTANT(2, 0);

	public static final Codec<MusicFrequency> CODEC = StringRepresentable.fromEnum(MusicFrequency::values);

	private static OptionInstance<MusicFrequency> option;

	private final int id;
	// Longest pause between two songs, in ticks.
	private final int maxDelay;
	private final String key;

	MusicFrequency(int id, int maxMinutes) {
		this.id = id;
		this.maxDelay = maxMinutes * 1200;
		this.key = "options.music_frequency." + name().toLowerCase(Locale.ROOT);
	}

	// Ticks until the next song, like MusicManager.MusicFrequency.getNextSongDelay in 1.21.6+.
	public int nextSongDelay(Music music, RandomSource random) {
		if (music == null) {
			return maxDelay;
		}
		if (this == CONSTANT) {
			return 100;
		}
		return Mth.nextInt(random, Math.min(music.getMinDelay(), maxDelay), Math.min(music.getMaxDelay(), maxDelay));
	}

	// Created on first use: Minecraft loads options.txt before the mod is initialised.
	public static OptionInstance<MusicFrequency> option() {
		if (option == null) {
			option = new OptionInstance<>("options.music_frequency",
					OptionInstance.cachedConstantTooltip(Component.translatable("options.music_frequency.tooltip")),
					OptionInstance.forOptionEnum(),
					new OptionInstance.Enum<>(Arrays.asList(values()), CODEC),
					DEFAULT, MusicFrequency::apply);
		}
		return option;
	}

	public static MusicFrequency current() {
		return option().get();
	}

	// Like MusicManager.setMinutesBetweenSongs in 1.21.6+: a new choice applies to the current pause.
	private static void apply(MusicFrequency frequency) {
		Minecraft mc = Minecraft.getInstance();
		MusicManagerAccessor music = (MusicManagerAccessor) mc.getMusicManager();
		music.bettermusictoast$setNextSongDelay(
				frequency.nextSongDelay(mc.getSituationalMusic().music(), music.bettermusictoast$getRandom()));
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getSerializedName() {
		return name();
	}
}
*///?}
