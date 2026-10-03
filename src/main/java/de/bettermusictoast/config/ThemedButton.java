package de.bettermusictoast.config;

import de.bettermusictoast.BetterMusicToastClient;
import de.bettermusictoast.hud.NowPlayingHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? if >=1.21.9
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
//? if >=1.21.6
import org.joml.Matrix3x2fStack;

/** A button drawn like the song box itself, in the currently selected colour theme. */
public class ThemedButton extends AbstractButton {
	private final Runnable action;

	public ThemedButton(int width, Component message, Runnable action) {
		super(0, 0, width, 20, message);
		this.action = action;
	}

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
	//?} else {
	/*protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
	*///?}
		ColorTheme theme = BetterMusicToastClient.config().colorTheme;
		// Light up under the mouse, or when selected with the keyboard. A mouse click also selects the
		// button and the selection survives a trip to the settings screen, so it alone does not count.
		Minecraft mc = Minecraft.getInstance();
		boolean highlighted = isHovered() || isFocused() && mc.getLastInputType().isKeyboard();
		int border = highlighted ? ColorTheme.mix(theme.border, 0xFFFFFF, 0.35f) : theme.border;
		Font font = mc.font;
		Component message = getMessage();

		//? if >=1.21.6 {
		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(getX(), getY());
		//?} else {
		/*com.mojang.blaze3d.vertex.PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.translate(getX(), getY(), 0.0f);
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

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
