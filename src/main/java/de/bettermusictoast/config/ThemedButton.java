package de.bettermusictoast.config;

import de.bettermusictoast.BetterMusicToastClient;
import de.bettermusictoast.hud.NowPlayingHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if >=1.20 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else
/*import de.bettermusictoast.compat.GuiGraphicsExtractor;*/
import net.minecraft.client.gui.components.AbstractButton;
//? if >=1.17
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? if >=1.21.9
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
//? if >=1.21.6
import org.joml.Matrix3x2fStack;

/** A button drawn like the song box itself, in the currently selected colour theme. */
//? if >=1.19.3 {
public class ThemedButton extends AbstractButton {
//?} else if >=1.16.2 {
/*// Before 1.19.3 buttons have no tooltip of their own; option screens ask widgets that are a TooltipAccessor.
public class ThemedButton extends AbstractButton implements net.minecraft.client.gui.components.TooltipAccessor {
*///?} else {
/*// Before 1.16.2 there is no TooltipAccessor; the mod's screens ask these buttons for their tooltip directly.
public class ThemedButton extends AbstractButton {
*///?}
	private final Runnable action;
	//? if >=1.17 && <1.19.3 {
	/*private java.util.List<net.minecraft.util.FormattedCharSequence> tooltip = java.util.List.of();
	*///?} else if <1.17 {
	/*private java.util.List<net.minecraft.util.FormattedCharSequence> tooltip = java.util.Collections.emptyList();
	*///?}

	public ThemedButton(int width, Component message, Runnable action) {
		super(0, 0, width, 20, message);
		this.action = action;
	}

	//? if <1.19.3 {
	/*/^* Sets the tooltip, wrapped like 1.19.3+'s Tooltip.create. ^/
	public void setTooltipText(Component text) {
		tooltip = Minecraft.getInstance().font.split(text, 170);
	}

	//? if >=1.17 {
	@Override
	public java.util.List<net.minecraft.util.FormattedCharSequence> getTooltip() {
		return tooltip;
	}
	//?} else {
	/^// Before 1.17 option screens ask for an Optional.
	public java.util.Optional<java.util.List<net.minecraft.util.FormattedCharSequence>> getTooltip() {
		return java.util.Optional.of(tooltip);
	}
	^///?}
	*///?}

	// Since 1.21.9 the click also reports which key or mouse button triggered it.
	@Override
	//? if >=1.21.9 {
	public void onPress(InputWithModifiers input) {
	//?} else {
	/*public void onPress() {
	*///?}
		pressed();
	}

	/** Version-independent click handler for subclasses. */
	protected void pressed() {
		action.run();
	}

	// Before 1.21.11 buttons draw themselves entirely in renderWidget.
	@Override
	//? if >=1.21.11 {
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
	//?} else if >=1.20.1 {
	/*protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
	*///?} else if >=1.20 {
	/*// Public in 1.20.
	public void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
	*///?} else if >=1.19.4 {
	/*// Before 1.20 widgets draw with a PoseStack; wrap it so the drawing below stays the same.
	public void renderWidget(com.mojang.blaze3d.vertex.PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(poseStack);
	*///?} else {
	/*// Before 1.19.4 the method is called renderButton.
	public void renderButton(com.mojang.blaze3d.vertex.PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(poseStack);
	*///?}
		ColorTheme theme = BetterMusicToastClient.config().colorTheme;
		// Light up under the mouse, or when selected with the keyboard. A mouse click also selects the
		// button and the selection survives a trip to the settings screen, so it alone does not count.
		Minecraft mc = Minecraft.getInstance();
		// (Before 1.19.4 only the keyboard focuses buttons.)
		//? if >=1.19.4 {
		boolean highlighted = isHovered() || isFocused() && mc.getLastInputType().isKeyboard();
		//?} else
		/*boolean highlighted = isHovered || isFocused();*/
		int border = highlighted ? ColorTheme.mix(theme.border, 0xFFFFFF, 0.35f) : theme.border;
		Font font = mc.font;
		Component message = getMessage();

		//? if >=1.21.6 {
		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(getX(), getY());
		//?} else if >=1.19.3 {
		/*com.mojang.blaze3d.vertex.PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.translate(getX(), getY(), 0.0f);
		*///?} else {
		/*// Before 1.19.3 the position is a pair of public fields.
		com.mojang.blaze3d.vertex.PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.translate(x, y, 0.0f);
		*///?}
		NowPlayingHud.drawFrame(graphics, getWidth(), getHeight(), theme, border, 1.0f);
		graphics.text(font, message, (getWidth() - font.width(message)) / 2, (getHeight() - 8) / 2,
				NowPlayingHud.argb(theme.title, 1.0f), false);
		//? if >=1.21.6 {
		pose.popMatrix();
		//?} else {
		/*pose.popPose();
		*///?}
	}

	// Before 1.17 there is no narration output yet; buttons narrate their message on their own.
	//? if >=1.17 {
	@Override
	//? if >=1.19.3 {
	protected void updateWidgetNarration(NarrationElementOutput output) {
	//?} else
	/*public void updateNarration(NarrationElementOutput output) {*/
		defaultButtonNarrationText(output);
	}
	//?}
}
