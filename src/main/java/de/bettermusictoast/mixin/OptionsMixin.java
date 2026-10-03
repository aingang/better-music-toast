package de.bettermusictoast.mixin;

// Only for the "Music Frequency" option the mod adds before 1.21.6 (see MusicFrequency).
//? if <1.21.6 {
/*import de.bettermusictoast.compat.MusicFrequency;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {
	// Loads and saves the option in options.txt under the name Minecraft 1.21.6+ uses.
	@Inject(method = "processOptions", at = @At("TAIL"))
	private void bettermusictoast$processMusicFrequency(Options.FieldAccess access, CallbackInfo ci) {
		access.process("musicFrequency", MusicFrequency.option());
	}
}
*///?}
