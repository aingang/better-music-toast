package de.bettermusictoast.mixin;

// Only for the "Music Frequency" option the mod adds before 1.21.6 (see MusicFrequency).
//? if <1.21.6 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.bettermusictoast.compat.MusicFrequency;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
//? if >=1.19 {
import net.minecraft.util.RandomSource;
//?} else {
/^import java.util.Random;
^///?}
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// Makes the pauses between songs follow the chosen frequency, the way MusicManager.tick does in 1.21.6+.
@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {
	//? if >=1.19 {
	@Shadow
	@Final
	private RandomSource random;

	// The pause after a song has ended.
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I", ordinal = 1))
	private int bettermusictoast$pauseAfterSong(RandomSource random, int min, int max, Operation<Integer> original,
			@Local Music music) {
	//?} else if forge && <1.18.2 {
	/^// Before 1.19 the random source is a java.util.Random. Forge 1.18 – 1.18.1 cannot load MixinExtras
	// (no jar-in-jar yet), so plain redirects do the same there.
	@Shadow
	@Final
	private Random random;

	// The pause after a song has ended.
	@org.spongepowered.asm.mixin.injection.Redirect(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/Mth;nextInt(Ljava/util/Random;II)I", ordinal = 1))
	private int bettermusictoast$pauseAfterSong(Random random, int min, int max) {
		// The music tick() is working with: what the game wants to play right now.
		Music music = de.bettermusictoast.compat.McCompat.situationalMusic(net.minecraft.client.Minecraft.getInstance());
	^///?} else {
	/^// Before 1.19 the random source is a java.util.Random.
	@Shadow
	@Final
	private Random random;

	// The pause after a song has ended.
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/Mth;nextInt(Ljava/util/Random;II)I", ordinal = 1))
	private int bettermusictoast$pauseAfterSong(Random random, int min, int max, Operation<Integer> original,
			@Local Music music) {
	^///?}
		return MusicFrequency.current().nextSongDelay(music, random);
	}

	// The cap on the remaining pause, checked every tick: the frequency instead of the biome's maximum.
	//? if forge && <1.18.2 {
	/^@org.spongepowered.asm.mixin.injection.Redirect(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/sounds/Music;getMaxDelay()I", ordinal = 1))
	private int bettermusictoast$capPause(Music music) {
	^///?} else {
	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/sounds/Music;getMaxDelay()I", ordinal = 1))
	private int bettermusictoast$capPause(Music music, Operation<Integer> original) {
	//?}
		return MusicFrequency.current().nextSongDelay(music, this.random);
	}
}
*///?}
