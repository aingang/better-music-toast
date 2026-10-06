package de.bettermusictoast.config;

import de.bettermusictoast.BetterMusicToast;
import de.bettermusictoast.hud.NowPlayingHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

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
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (!visible) {
			return;
		}
		field_146123_n = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
		ColorTheme theme = BetterMusicToast.config().colorTheme;
		// Lights up under the mouse (1.7.10 has no keyboard focus for buttons).
		int border = field_146123_n ? ColorTheme.mix(theme.border, 0xFFFFFF, 0.35f) : theme.border;
		FontRenderer font = mc.fontRenderer;

		GL11.glPushMatrix();
		GL11.glTranslatef(xPosition, yPosition, 0.0f);
		NowPlayingHud.drawFrame(width, height, theme, border, 1.0f);
		font.drawString(displayString, (width - font.getStringWidth(displayString)) / 2, (height - 8) / 2,
				NowPlayingHud.argb(theme.title, 1.0f), false);
		GL11.glPopMatrix();
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
	}
}
