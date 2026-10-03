package de.bettermusictoast.mixin;

// Only for the "Music Frequency" option the mod adds before 1.21.6 (see MusicFrequency).
//? if <1.21.6 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.bettermusictoast.compat.MusicFrequency;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// Makes the pauses between songs follow the chosen frequency, the way MusicManager.tick does in 1.21.6+.
@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {
	@Shadow
	@Final
	private RandomSource random;

	// The pause after a song has ended.
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I", ordinal = 1))
	private int bettermusictoast$pauseAfterSong(RandomSource random, int min, int max, Operation<Integer> original,
			@Local Music music) {
		return MusicFrequency.current().nextSongDelay(music, random);
	}

	// The cap on the remaining pause, checked every tick: the frequency instead of the biome's maximum.
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/sounds/Music;getMaxDelay()I", ordinal = 1))
	private int bettermusictoast$capPause(Music music, Operation<Integer> original) {
		return MusicFrequency.current().nextSongDelay(music, this.random);
	}
}
*///?}
