package de.bettermusictoast.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

/** The seconds slider (2–30), drawn and dragged like Minecraft's own option sliders. */
public final class DurationSlider extends OptionButton {
	private static final int MIN = 2;
	private static final int MAX = 30;

	private final String caption;
	private final CycleButton.Listener<Integer> listener;
	private int value;
	private boolean dragging;

	public DurationSlider(int width, String captionKey, int value, CycleButton.Listener<Integer> listener) {
		super(width, "");
		this.caption = I18n.format(captionKey);
		this.listener = listener;
		this.value = value;
		setTooltip(I18n.format(captionKey + ".tooltip"));
		refresh();
	}

	@Override
	public boolean clicksOnPress() {
		return false;
	}

	@Override
	protected int getHoverState(boolean mouseOver) {
		return 0;
	}

	@Override
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if (!visible) {
			return;
		}
		if (dragging) {
			setFromMouse(mouseX);
		}
		mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		float position = (value - MIN) / (float) (MAX - MIN);
		int knobX = left() + (int) (position * (width - 8));
		drawTexturedModalRect(knobX, top(), 0, 66, 4, 20);
		drawTexturedModalRect(knobX + 4, top(), 196, 66, 4, 20);
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			setFromMouse(mouseX);
			dragging = true;
			return true;
		}
		return false;
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		if (dragging) {
			playPressSound(Minecraft.getMinecraft().getSoundHandler());
		}
		dragging = false;
	}

	private void setFromMouse(int mouseX) {
		float position = Math.max(0.0F, Math.min((mouseX - (left() + 4)) / (float) (width - 8), 1.0F));
		int newValue = MIN + Math.round(position * (MAX - MIN));
		if (newValue != value) {
			value = newValue;
			refresh();
			listener.changed(value);
		}
	}

	private void refresh() {
		displayString = caption + ": " + I18n.format("bettermusictoast.seconds", value);
	}
}
