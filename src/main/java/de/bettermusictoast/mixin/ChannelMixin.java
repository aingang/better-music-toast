package de.bettermusictoast.mixin;

import com.mojang.blaze3d.audio.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Channel.class)
public abstract class ChannelMixin {
	/** Seconds of audio queued ahead for streamed sounds (vanilla: 4). */
	@Unique
	private static final int BUFFERED_SECONDS = 10;

	/**
	 * Streamed sounds (music, music discs) are only refilled from the game loop. When the game
	 * freezes for longer than the queued audio lasts, e.g. while Iris compiles a shader pack,
	 * the stream runs dry, Minecraft treats the song as finished and starts the next one.
	 * Queuing more audio ahead lets music survive those freezes.
	 */
	@ModifyArg(method = "attachBufferStream", at = @At(value = "INVOKE",
			target = "Lcom/mojang/blaze3d/audio/Channel;pumpBuffers(I)V"))
	private int bettermusictoast$queueMoreAudio(int buffers) {
		return Math.max(buffers, BUFFERED_SECONDS);
	}
}
