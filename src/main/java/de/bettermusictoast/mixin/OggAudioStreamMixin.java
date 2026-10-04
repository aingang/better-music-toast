package de.bettermusictoast.mixin;

// Only before 1.20.5, where Minecraft decodes music with STB Vorbis (OggAudioStream); 1.20.5 switched
// to JOrbisAudioStream.
//? if <1.20.5 {
/*import com.mojang.blaze3d.audio.OggAudioStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/^*
 * Fixes a Minecraft bug that the longer music buffer (see ChannelMixin) runs into. Once the file has
 * been read to the end, every further read doubles the decoder's input buffer (forwardBuffer). A
 * channel keeps reading until its queued audio has played, so with more audio queued the buffer
 * doubles often enough to overflow, and the sound thread dies with an OutOfMemoryError when a song
 * ends. After the end, reads now simply return no more audio, which is all vanilla's reads produce.
 ^/
@Mixin(OggAudioStream.class)
public abstract class OggAudioStreamMixin {
	@Unique
	private boolean bettermusictoast$endOfFile;

	@Inject(method = "refillFromStream", at = @At("RETURN"))
	private void bettermusictoast$markEndOfFile(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ()) {
			bettermusictoast$endOfFile = true;
		}
	}

	@Inject(method = "readFrame", at = @At("HEAD"), cancellable = true)
	private void bettermusictoast$stopAtEndOfFile(CallbackInfoReturnable<Boolean> cir) {
		if (bettermusictoast$endOfFile) {
			cir.setReturnValue(false);
		}
	}
}
*///?}
