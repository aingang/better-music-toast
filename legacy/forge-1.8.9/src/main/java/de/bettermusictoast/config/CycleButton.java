package de.bettermusictoast.config;

import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/**
 * A button that cycles through values, shown as "Caption: Value", like the cycle buttons of the
 * newer versions: click for the next value, shift-click for the previous one.
 */
public class CycleButton<T> extends OptionButton {
	/** Turns a value into the text shown after the caption. */
	public interface Labeler<T> {
		String label(T value);
	}

	/** Receives the newly chosen value. */
	public interface Listener<T> {
		void changed(T value);
	}

	private final String caption;
	private final List<T> values;
	private final Labeler<T> labeler;
	private final Listener<T> listener;
	private int index;

	public CycleButton(int width, String captionKey, List<T> values, T value, Labeler<T> labeler, Listener<T> listener) {
		super(width, "");
		this.caption = I18n.format(captionKey);
		this.values = values;
		this.labeler = labeler;
		this.listener = listener;
		this.index = Math.max(0, values.indexOf(value));
		setTooltip(I18n.format(captionKey + ".tooltip"));
		refresh();
	}

	@Override
	public void onPress() {
		int step = GuiScreen.isShiftKeyDown() ? -1 : 1;
		index = ((index + step) % values.size() + values.size()) % values.size();
		refresh();
		listener.changed(values.get(index));
	}

	private void refresh() {
		displayString = caption + ": " + labeler.label(values.get(index));
	}
}
