package de.bettermusictoast.compat;

//? if <1.20 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/^*
 * Before 1.20 Minecraft has no GuiGraphics: menus and the HUD draw with a PoseStack and the static
 * helpers of GuiComponent. This class offers the few GuiGraphics methods the mod uses, so the
 * drawing code stays the same in every version. Only compiled before 1.20.
 ^/
public final class GuiGraphicsExtractor {
	private final PoseStack pose;

	public GuiGraphicsExtractor(PoseStack pose) {
		this.pose = pose;
	}

	public PoseStack pose() {
		return pose;
	}

	public int guiWidth() {
		return Minecraft.getInstance().getWindow().getGuiScaledWidth();
	}

	public int guiHeight() {
		return Minecraft.getInstance().getWindow().getGuiScaledHeight();
	}

	public void fill(int x1, int y1, int x2, int y2, int color) {
		GuiComponent.fill(pose, x1, y1, x2, y2, color);
	}

	public int drawString(Font font, String text, int x, int y, int color, boolean shadow) {
		return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
	}

	public int drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
		return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
	}

	public void renderItem(ItemStack stack, int x, int y) {
		//? if >=1.19.4 {
		Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(pose, stack, x, y);
		//?} else if >=1.17 {
		/^// Before 1.19.4 items are drawn with the global model-view matrix, so apply ours to it.
		PoseStack modelView = RenderSystem.getModelViewStack();
		modelView.pushPose();
		modelView.mulPoseMatrix(pose.last().pose());
		RenderSystem.applyModelViewMatrix();
		Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, x, y);
		modelView.popPose();
		RenderSystem.applyModelViewMatrix();
		^///?} else {
		/^// Before 1.17 the GUI still draws with OpenGL's fixed matrix stack, so apply ours to it.
		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(pose.last().pose());
		Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, x, y);
		RenderSystem.popMatrix();
		^///?}
	}

	public void setColor(float red, float green, float blue, float alpha) {
		//? if >=1.17 {
		RenderSystem.setShaderColor(red, green, blue, alpha);
		//?} else
		/^RenderSystem.color4f(red, green, blue, alpha);^/
	}

	public void blit(Identifier texture, int x, int y, float u, float v, int width, int height,
			int textureWidth, int textureHeight) {
		//? if >=1.17 {
		RenderSystem.setShaderTexture(0, texture);
		//?} else
		/^Minecraft.getInstance().getTextureManager().bind(texture);^/
		GuiComponent.blit(pose, x, y, u, v, width, height, textureWidth, textureHeight);
	}
}
*///?}
