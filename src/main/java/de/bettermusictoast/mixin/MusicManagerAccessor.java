package de.bettermusictoast.mixin;

// Only for the "Music Frequency" option the mod adds before 1.21.6 (see MusicFrequency).
//? if <1.21.6 {
/*import net.minecraft.client.sounds.MusicManager;
//? if >=1.19 {
import net.minecraft.util.RandomSource;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MusicManager.class)
public interface MusicManagerAccessor {
	@Accessor("nextSongDelay")
	void bettermusictoast$setNextSongDelay(int delay);

	@Accessor("random")
	//? if >=1.19 {
	RandomSource bettermusictoast$getRandom();
	//?} else
	/^java.util.Random bettermusictoast$getRandom();^/
}
*///?}
