package de.bettermusictoast.mixin;

// Only on Fabric before 1.17: Fabric API has screen events there only in its later builds, so the box
// is drawn over menus from here, right where Fabric API's own screen event would run.
//? if fabric && <1.17 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import de.bettermusictoast.BetterMusicToastClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Inject(method = "render", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/Screen;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V",
			shift = At.Shift.AFTER))
	private void bettermusictoast$drawOverScreen(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
		BetterMusicToastClient.drawOverScreen(new PoseStack());
	}
}
*///?}
