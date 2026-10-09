package de.bettermusictoast.config;

import de.bettermusictoast.compat.MusicFrequency;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

// The Music Frequency button in the song box's colour theme, so it is clear it comes from the mod.
// It behaves like vanilla's cycle button: click for the next value, shift-click or scroll for others.
public final class MusicFrequencyButton extends ThemedButton {
	public MusicFrequencyButton() {
		super(150, "", new Runnable() {
			@Override
			public void run() {
			}
		});
		setTooltip(I18n.format("options.music_frequency.tooltip"));
		refresh();
	}

	@Override
	public void onPress() {
		cycle(GuiScreen.isShiftKeyDown() ? -1 : 1);
	}

	public void cycle(int step) {
		MusicFrequency[] values = MusicFrequency.values();
		int next = ((MusicFrequency.current().ordinal() + step) % values.length + values.length) % values.length;
		MusicFrequency.set(values[next]);
		refresh();
	}

	private void refresh() {
		displayString = I18n.format("options.music_frequency") + ": " + I18n.format(MusicFrequency.current().translationKey());
	}
}
