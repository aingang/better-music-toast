package de.bettermusictoast.mixin;

// Only Forge 1.20.6 – 1.21.8: not every Forge build there can add HUD parts (see BetterMusicToastClient).
//? if forge && >=1.20.6 && <1.21.9 {
/*import de.bettermusictoast.BetterMusicToastClient;
import de.bettermusictoast.hud.NowPlayingHud;
//? if >=1.21
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class ForgeGuiMixin {
	// After the whole HUD, so the box is on top of every other part, like addLast on Fabric.
	//? if >=1.21.6 {
	// Forge 1.21.6+ hands the HUD to ForgeLayeredDraw and returns early, so TAIL is never reached.
	@Inject(method = "render", at = @At("RETURN"))
	//?} else {
	/^@Inject(method = "render", at = @At("TAIL"))
	^///?}
	//? if >=1.21 {
	private void bettermusictoast$renderNowPlaying(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
	//?} else {
	/^// 1.20.6 passes the partial tick instead of a DeltaTracker.
	private void bettermusictoast$renderNowPlaying(GuiGraphicsExtractor graphics, float deltaTracker, CallbackInfo ci) {
	^///?}
		NowPlayingHud hud = BetterMusicToastClient.forgeHud();
		if (hud != null) {
			hud.render(graphics, deltaTracker);
		}
	}
}
*///?}
