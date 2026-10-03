package de.bettermusictoast.mixin;

// Vanilla's music toast only exists since 1.21.6; older versions build without this mixin.
//? if >=1.21.6 {
import de.bettermusictoast.BetterMusicToastClient;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.NowPlayingToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NowPlayingToast.class)
public abstract class NowPlayingToastMixin {
	/**
	 * Every place vanilla draws its music toast (the popup over the game and over menus, and
	 * the permanent one in the pause menu) goes through this method, so hiding it here
	 * catches all of them.
	 */
	@Inject(method = "extractToast", at = @At("HEAD"), cancellable = true)
	private static void bettermusictoast$hideVanillaToast(GuiGraphicsExtractor graphics, Font font, CallbackInfo ci) {
		if (BetterMusicToastClient.config().hideVanillaToast) {
			ci.cancel();
		}
	}
}
//?}
