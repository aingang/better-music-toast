package de.bettermusictoast.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

/**
 * Scrolling list of option rows, laid out like Minecraft 1.8.9's video settings (and the options
 * lists of the newer versions): one full-width button or two side by side per row.
 */
public final class OptionList extends GuiListExtended {
	private final List<Row> rows = new ArrayList<Row>();
	private final int screenWidth;

	public OptionList(Minecraft mc, int width, int height) {
		super(mc, width, height, 32, height - 32, 25);
		this.screenWidth = width;
		this.field_148163_i = false;
	}

	/** A full-width row (310 px). */
	public void addBig(OptionButton button) {
		button.width = 310;
		button.xPosition = screenWidth / 2 - 155;
		rows.add(new Row(button));
	}

	/** Two buttons side by side (150 px each). */
	public void addSmall(OptionButton left, OptionButton right) {
		left.width = 150;
		left.xPosition = screenWidth / 2 - 155;
		right.width = 150;
		right.xPosition = screenWidth / 2 - 155 + 160;
		rows.add(new Row(left, right));
	}

	/** The button under the mouse inside the visible part of the list, or null. */
	public OptionButton hoveredButton(int mouseY) {
		if (!isMouseYWithinSlotBounds(mouseY)) {
			return null;
		}
		for (Row row : rows) {
			for (OptionButton button : row.buttons) {
				if (button.isHovered()) {
					return button;
				}
			}
		}
		return null;
	}

	@Override
	public Row getListEntry(int index) {
		return rows.get(index);
	}

	@Override
	protected int getSize() {
		return rows.size();
	}

	@Override
	public int getListWidth() {
		return 400;
	}

	@Override
	protected int getScrollBarX() {
		return super.getScrollBarX() + 32;
	}

	final class Row implements GuiListExtended.IGuiListEntry {
		private final List<OptionButton> buttons;

		Row(OptionButton... buttons) {
			this.buttons = Arrays.asList(buttons);
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
			for (OptionButton button : buttons) {
				button.yPosition = y;
				button.drawButton(mc, mouseX, mouseY);
			}
		}

		@Override
		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
			for (OptionButton button : buttons) {
				if (button.mousePressed(mc, mouseX, mouseY)) {
					if (button.clicksOnPress()) {
						button.playPressSound(mc.getSoundHandler());
						button.onPress();
					}
					return true;
				}
			}
			return false;
		}

		@Override
		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			for (OptionButton button : buttons) {
				button.mouseReleased(x, y);
			}
		}

		@Override
		public void setSelected(int index, int x, int y) {
		}
	}
}
