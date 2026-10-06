package de.bettermusictoast.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.config.GuiUtils;

/**
 * A button of the mod's settings with a click action and a hover tooltip. Minecraft before 1.13 has
 * no tooltips on its options, but the newer versions do, so the mod draws them like those.
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

	// The position fields were renamed in 1.11.

	public int left() {
		//#if MC>=11100
		return x;
		//#else
		//$$ return xPosition;
		//#endif
	}

	public int top() {
		//#if MC>=11100
		return y;
		//#else
		//$$ return yPosition;
		//#endif
	}

	public void setPosition(int left, int top) {
		//#if MC>=11100
		x = left;
		y = top;
		//#else
		//$$ xPosition = left;
		//$$ yPosition = top;
		//#endif
	}

	/** Draws the button like Minecraft does; the method gained a partial-ticks argument in 1.12. */
	public void draw(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		//#if MC>=11200
		drawButton(mc, mouseX, mouseY, partialTicks);
		//#else
		//$$ drawButton(mc, mouseX, mouseY);
		//#endif
	}

	static FontRenderer font(Minecraft mc) {
		//#if MC>=11100
		return mc.fontRenderer;
		//#else
		//$$ return mc.fontRendererObj;
		//#endif
	}

	/** Draws the tooltip next to the mouse; "\n" in the text starts a new line. */
	public void drawTooltip(int mouseX, int mouseY, int screenWidth, int screenHeight) {
		if (tooltip == null) {
			return;
		}
		// Line breaks are written as "\n" in the .lang files before 1.13.
		List<String> lines = new ArrayList<String>(Arrays.asList(tooltip.replace("\\n", "\n").split("\n", -1)));
		GuiUtils.drawHoveringText(lines, mouseX, mouseY, screenWidth, screenHeight, TOOLTIP_WIDTH,
				font(Minecraft.getMinecraft()));
	}
}
