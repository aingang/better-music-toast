package de.bettermusictoast.compat;

// Minecraft added the "Music Frequency" option in 1.21.6; older versions get it from the mod.
//? if <1.21.6 {
/*import com.mojang.serialization.Codec;
import de.bettermusictoast.mixin.MusicManagerAccessor;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.client.Minecraft;
//? if >=1.19 {
import net.minecraft.client.OptionInstance;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
//? if >=1.19 {
import net.minecraft.util.OptionEnum;
import net.minecraft.util.RandomSource;
//?} else {
/^import java.util.Random;
^///?}
import net.minecraft.util.StringRepresentable;

// The same option as in Minecraft 1.21.6+: same values, timings and texts, and saved under the same
// name in options.txt, so the choice carries over when updating Minecraft.
//? if >=1.19 {
public enum MusicFrequency implements OptionEnum, StringRepresentable {
//?} else {
/^// Before 1.19 there are no OptionInstance / OptionEnum; the value is kept here (see OptionsMixin).
public enum MusicFrequency implements StringRepresentable {
^///?}
	DEFAULT(0, 20),
	FREQUENT(1, 10),
	CONSTANT(2, 0);

	//? if >=1.19 {
	public static final Codec<MusicFrequency> CODEC = StringRepresentable.fromEnum(MusicFrequency::values);

	private static OptionInstance<MusicFrequency> option;
	//?} else {
	/^private static MusicFrequency value = DEFAULT;
	^///?}

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
	//? if >=1.19 {
	public int nextSongDelay(Music music, RandomSource random) {
	//?} else {
	/^public int nextSongDelay(Music music, Random random) {
	^///?}
		if (music == null) {
			return maxDelay;
		}
		if (this == CONSTANT) {
			return 100;
		}
		return Mth.nextInt(random, Math.min(music.getMinDelay(), maxDelay), Math.min(music.getMaxDelay(), maxDelay));
	}

	//? if >=1.19 {
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
	//?} else {
	/^public static MusicFrequency current() {
		return value;
	}

	// Sets a new choice from the settings button.
	public static void set(MusicFrequency frequency) {
		value = frequency;
		apply(frequency);
	}

	// Loads and saves the choice in options.txt (see OptionsMixin), without touching the music.
	public static void process(net.minecraft.client.Options.FieldAccess access) {
		value = access.process("musicFrequency", value, MusicFrequency::byName, MusicFrequency::getSerializedName);
	}

	private static MusicFrequency byName(String name) {
		return Arrays.stream(values()).filter(f -> f.name().equals(name)).findFirst().orElse(DEFAULT);
	}

	// The value's name, like OptionEnum.getCaption in 1.19+.
	public Component getCaption() {
		return Component.translatable(key);
	}
	^///?}

	// Like MusicManager.setMinutesBetweenSongs in 1.21.6+: a new choice applies to the current pause.
	private static void apply(MusicFrequency frequency) {
		Minecraft mc = Minecraft.getInstance();
		MusicManagerAccessor music = (MusicManagerAccessor) mc.getMusicManager();
		music.bettermusictoast$setNextSongDelay(
				frequency.nextSongDelay(McCompat.situationalMusic(mc), music.bettermusictoast$getRandom()));
	}

	//? if >=1.19 {
	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getKey() {
		return key;
	}
	//?}

	@Override
	public String getSerializedName() {
		return name();
	}
}
*///?}
