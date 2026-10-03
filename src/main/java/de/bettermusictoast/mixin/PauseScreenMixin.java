package de.bettermusictoast.mixin;

// Vanilla's music toast only exists since 1.21.6; older versions build without this mixin.
//? if >=1.21.6 {
import de.bettermusictoast.BetterMusicToastClient;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {
	/** The pause menu also uses this to decide whether to animate the vanilla toast's notes. */
	@Inject(method = "rendersNowPlayingToast", at = @At("HEAD"), cancellable = true)
	private void bettermusictoast$hideVanillaToast(CallbackInfoReturnable<Boolean> cir) {
		if (BetterMusicToastClient.config().hideVanillaToast) {
			cir.setReturnValue(false);
		}
	}
}
//?}
