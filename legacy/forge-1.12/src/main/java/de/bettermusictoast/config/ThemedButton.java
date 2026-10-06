package de.bettermusictoast.config;

import de.bettermusictoast.BetterMusicToast;
import de.bettermusictoast.hud.NowPlayingHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

/** A button drawn like the song box itself, in the currently selected colour theme. */
public class ThemedButton extends OptionButton {
	private final Runnable action;

	public ThemedButton(int width, String text, Runnable action) {
		super(width, text);
		this.action = action;
	}

	@Override
	public void onPress() {
		action.run();
	}

	@Override
	//#if MC>=11200
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
	//#else
	//$$ public void drawButton(Minecraft mc, int mouseX, int mouseY) {
	//#endif
		if (!visible) {
			return;
		}
		int x = left();
		int y = top();
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		ColorTheme theme = BetterMusicToast.config().colorTheme;
		// Lights up under the mouse (these versions have no keyboard focus for buttons).
		int border = hovered ? ColorTheme.mix(theme.border, 0xFFFFFF, 0.35f) : theme.border;
		FontRenderer font = font(mc);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0.0f);
		NowPlayingHud.drawFrame(width, height, theme, border, 1.0f);
		font.drawString(displayString, (width - font.getStringWidth(displayString)) / 2, (height - 8) / 2,
				NowPlayingHud.argb(theme.title, 1.0f), false);
		GlStateManager.popMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}
}
