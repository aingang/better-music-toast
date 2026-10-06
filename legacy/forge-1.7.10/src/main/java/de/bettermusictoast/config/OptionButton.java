package de.bettermusictoast.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * A button of the mod's settings with a click action and a hover tooltip. Minecraft 1.7.10 has no
 * tooltips on its options, but the newer versions do, so the mod draws them like those.
 */
public class OptionButton extends GuiButton {
	/** Same wrapping width as the tooltips of the newer versions. */
	private static final int TOOLTIP_WIDTH = 170;

	private String tooltip;

	public OptionButton(int width, String text) {
		super(0, 0, 0, width, 20, text);
	}

	/** Called after a click (the click sound has already played). */
	public void onPress() {
	}

	/** Whether a click plays the click sound right away (sliders play it on release). */
	public boolean clicksOnPress() {
		return true;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public boolean isHovered() {
		return visible && field_146123_n;
	}

	/**
	 * Draws the tooltip next to the mouse; "\n" in the text starts a new line. Looks like Minecraft's
	 * item tooltips (GuiScreen.drawHoveringText), with long lines wrapped like in the newer versions.
	 */
	public void drawTooltip(int mouseX, int mouseY, int screenWidth, int screenHeight) {
		if (tooltip == null) {
			return;
		}
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		// Line breaks are written as "\n" in the .lang files of 1.7.10.
		String[] paragraphs = tooltip.replace("\\n", "\n").split("\n", -1);
		List<String> lines = new ArrayList<String>();
		int titleLines = 0;
		for (int i = 0; i < paragraphs.length; i++) {
			@SuppressWarnings("unchecked")
			List<String> wrapped = font.listFormattedStringToWidth(paragraphs[i], TOOLTIP_WIDTH);
			if (wrapped.isEmpty()) {
				wrapped.add("");
			}
			lines.addAll(wrapped);
			if (i == 0) {
				titleLines = wrapped.size();
			}
		}
		int textWidth = 0;
		for (String line : lines) {
			textWidth = Math.max(textWidth, font.getStringWidth(line));
		}

		int x = mouseX + 12;
		int y = mouseY - 12;
		int textHeight = 8;
		if (lines.size() > 1) {
			textHeight += 2 + (lines.size() - 1) * 10;
		}
		if (x + textWidth > screenWidth) {
			x -= 28 + textWidth;
		}
		if (y + textHeight + 6 > screenHeight) {
			y = screenHeight - textHeight - 6;
		}

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		zLevel = 300.0F;
		int background = 0xF0100010;
		drawGradientRect(x - 3, y - 4, x + textWidth + 3, y - 3, background, background);
		drawGradientRect(x - 3, y + textHeight + 3, x + textWidth + 3, y + textHeight + 4, background, background);
		drawGradientRect(x - 3, y - 3, x + textWidth + 3, y + textHeight + 3, background, background);
		drawGradientRect(x - 4, y - 3, x - 3, y + textHeight + 3, background, background);
		drawGradientRect(x + textWidth + 3, y - 3, x + textWidth + 4, y + textHeight + 3, background, background);
		int borderStart = 0x505000FF;
		int borderEnd = (borderStart & 0xFEFEFE) >> 1 | borderStart & 0xFF000000;
		drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + textHeight + 3 - 1, borderStart, borderEnd);
		drawGradientRect(x + textWidth + 2, y - 3 + 1, x + textWidth + 3, y + textHeight + 3 - 1, borderStart, borderEnd);
		drawGradientRect(x - 3, y - 3, x + textWidth + 3, y - 3 + 1, borderStart, borderStart);
		drawGradientRect(x - 3, y + textHeight + 2, x + textWidth + 3, y + textHeight + 3, borderEnd, borderEnd);
		for (int i = 0; i < lines.size(); i++) {
			font.drawStringWithShadow(lines.get(i), x, y, -1);
			// A small gap below the first paragraph, like below an item's name.
			if (i + 1 == titleLines) {
				y += 2;
			}
			y += 10;
		}
		zLevel = 0.0F;
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
}
