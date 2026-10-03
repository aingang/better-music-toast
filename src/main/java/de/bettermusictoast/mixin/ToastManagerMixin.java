package de.bettermusictoast.mixin;

// Vanilla's music toast only exists since 1.21.6; older versions build without this mixin.
//? if >=1.21.6 {
import de.bettermusictoast.BetterMusicToastClient;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public abstract class ToastManagerMixin {
	/**
	 * Vanilla's own "music toast" shows the same information in the top left corner.
	 * Suppress it while our display is active (configurable), otherwise remember when
	 * it popped up so our panel can step out of its way.
	 */
	@Inject(method = "showNowPlayingToast", at = @At("HEAD"), cancellable = true)
	private void bettermusictoast$onShowNowPlayingToast(CallbackInfo ci) {
		if (BetterMusicToastClient.config().hideVanillaToast) {
			ci.cancel();
		} else {
			BetterMusicToastClient.tracker().onVanillaToastShown();
		}
	}
}
//?}
