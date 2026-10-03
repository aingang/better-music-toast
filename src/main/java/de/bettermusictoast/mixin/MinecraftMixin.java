package de.bettermusictoast.mixin;

// Before 1.21.6 pausing the game stops the music too. Since 1.21.6 music keeps playing in the
// pause menu and the settings; this brings that behaviour to the older versions.
//? if <1.21.6 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.bettermusictoast.compat.PausesAllButMusic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow
	private volatile boolean pause;

	@Shadow
	@Final
	private MusicManager musicManager;

	// Pausing keeps music (and menu clicks) playing, like 1.21.6+.
	@WrapOperation(method = "pauseGame", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/sounds/SoundManager;pause()V"))
	private void bettermusictoast$keepMusicPlaying(SoundManager soundManager, Operation<Void> original) {
		((PausesAllButMusic) ((SoundManagerAccessor) soundManager).bettermusictoast$getSoundEngine())
				.bettermusictoast$pauseAllButMusic();
	}

	// The music keeps being managed while paused (next song, fading), like 1.21.6+. Unpaused,
	// vanilla already does this right before the same call.
	@Inject(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/sounds/SoundManager;tick(Z)V"))
	private void bettermusictoast$tickMusicWhenPaused(CallbackInfo ci) {
		if (pause) {
			musicManager.tick();
		}
	}
}
*///?}
