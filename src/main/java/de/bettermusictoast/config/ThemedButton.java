package de.bettermusictoast.config;

import de.bettermusictoast.BetterMusicToastClient;
import de.bettermusictoast.hud.NowPlayingHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

/** A button drawn like the song box itself, in the currently selected colour theme. */
public class ThemedButton extends AbstractButton {
	private final Runnable action;

	public ThemedButton(int width, Component message, Runnable action) {
		super(0, 0, width, 20, message);
		this.action = action;
	}

	@Override
	public void onPress(InputWithModifiers input) {
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
		int border = isHoveredOrFocused() ? ColorTheme.mix(theme.border, 0xFFFFFF, 0.35f) : theme.border;
		Font font = Minecraft.getInstance().font;
		Component message = getMessage();

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(getX(), getY());
		NowPlayingHud.drawFrame(graphics, getWidth(), getHeight(), theme, border, 1.0f);
		graphics.text(font, message, (getWidth() - font.width(message)) / 2, (getHeight() - 8) / 2,
				NowPlayingHud.argb(theme.title, 1.0f), false);
		pose.popMatrix();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
