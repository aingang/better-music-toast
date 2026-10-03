package de.bettermusictoast.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.config.GuiUtils;

/**
 * A button of the mod's settings with a click action and a hover tooltip. Minecraft 1.8.9 has no
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
		return visible && hovered;
	}

	/** Draws the tooltip next to the mouse; "\n" in the text starts a new line. */
	public void drawTooltip(int mouseX, int mouseY, int screenWidth, int screenHeight) {
		if (tooltip == null) {
			return;
		}
		// Line breaks are written as "\n" in the .lang files of 1.8.9.
		List<String> lines = new ArrayList<String>(Arrays.asList(tooltip.replace("\\n", "\n").split("\n", -1)));
		GuiUtils.drawHoveringText(lines, mouseX, mouseY, screenWidth, screenHeight, TOOLTIP_WIDTH,
				Minecraft.getMinecraft().fontRendererObj);
	}
}
