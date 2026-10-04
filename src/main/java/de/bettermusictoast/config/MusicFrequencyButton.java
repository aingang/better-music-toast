package de.bettermusictoast.config;

// Only before 1.21.6, where the mod adds Minecraft's "Music Frequency" option (see MusicFrequency).
//? if <1.21.6 {
/*import de.bettermusictoast.compat.MusicFrequency;
import net.minecraft.client.Options;
//? if >=1.19.3
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

// The Music Frequency button in the song box's colour theme, so it is clear it comes from the mod.
// It behaves like vanilla's cycle button: click for the next value, shift-click or scroll for others.
public final class MusicFrequencyButton extends ThemedButton {
	private static final Component CAPTION = Component.translatable("options.music_frequency");

	public MusicFrequencyButton() {
		super(150, Component.empty(), () -> {
		});
		//? if >=1.19.3 {
		setTooltip(Tooltip.create(Component.translatable("options.music_frequency.tooltip")));
		//?} else
		/^setTooltipText(Component.translatable("options.music_frequency.tooltip"));^/
		refresh();
	}

	@Override
	protected void pressed() {
		cycle(Screen.hasShiftDown() ? -1 : 1);
	}

	// Horizontal scrolling only reaches widgets since 1.20.2.
	//? if >=1.20.2 {
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
	//?} else {
	/^@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
	^///?}
		if (scrollY > 0.0) {
			cycle(-1);
		} else if (scrollY < 0.0) {
			cycle(1);
		}
		return true;
	}

	private void cycle(int step) {
		MusicFrequency[] values = MusicFrequency.values();
		int next = Math.floorMod(MusicFrequency.current().ordinal() + step, values.length);
		MusicFrequency.option().set(values[next]);
		refresh();
	}

	private void refresh() {
		setMessage(Options.genericValueLabel(CAPTION, MusicFrequency.current().getCaption()));
	}
}
*///?}
