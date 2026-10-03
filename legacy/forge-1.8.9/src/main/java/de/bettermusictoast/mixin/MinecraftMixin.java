package de.bettermusictoast.mixin;

import de.bettermusictoast.compat.SoundManagerAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// In 1.8.9 pausing the game stops the music too. Since 1.21.6 music keeps playing in the pause
// menu and the settings; this brings that behaviour to 1.8.9. While paused, the mod keeps the
// music going (next song, cleanup) from its client tick, see BetterMusicToast.
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Redirect(method = "displayInGameMenu", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/audio/SoundHandler;pauseSounds()V"))
	private void bettermusictoast$keepMusicPlaying(SoundHandler soundHandler) {
		((SoundManagerAccess) ((SoundHandlerAccessor) soundHandler).bettermusictoast$getSoundManager())
				.bettermusictoast$pauseAllButMusic();
	}
}
