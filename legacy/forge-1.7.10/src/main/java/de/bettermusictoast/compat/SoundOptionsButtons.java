package de.bettermusictoast.compat;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import de.bettermusictoast.config.ConfigScreen;
import de.bettermusictoast.config.MusicFrequencyButton;
import de.bettermusictoast.config.OptionButton;
import de.bettermusictoast.config.ThemedButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Mouse;

/**
 * Adds the row the newer versions have in "Music & Sounds" below the volume sliders: the
 * "Music Frequency" option and a button that opens the mod's settings, both in the box's colour theme.
 */
public final class SoundOptionsButtons {
	// Button ids that Minecraft's sound screen does not use.
	private static final int FREQUENCY_ID = 0x4D54;
	private static final int SETTINGS_ID = 0x4D55;

	private GuiScreen screen;
	private MusicFrequencyButton frequency;
	private ThemedButton settings;

	@SubscribeEvent
	public void onInit(GuiScreenEvent.InitGuiEvent.Post event) {
		if (!(event.gui instanceof GuiScreenOptionsSounds)) {
			return;
		}
		final GuiScreen parent = event.gui;
		screen = parent;
		// The sliders fill rows 0 to 4 (master volume, then two per row); this is row 5.
		int y = parent.height / 6 - 12 + 24 * 5;
		frequency = new MusicFrequencyButton();
		frequency.id = FREQUENCY_ID;
		frequency.xPosition = parent.width / 2 - 155;
		frequency.yPosition = y;
		settings = new ThemedButton(150, I18n.format("bettermusictoast.soundOptions.button"), new Runnable() {
			@Override
			public void run() {
				Minecraft.getMinecraft().displayGuiScreen(new ConfigScreen(parent));
			}
		});
		settings.setTooltip(I18n.format("bettermusictoast.soundOptions.button.tooltip"));
		settings.id = SETTINGS_ID;
		settings.xPosition = parent.width / 2 + 5;
		settings.yPosition = y;
		// Only what is scrolled from now on counts (see onWheel).
		Mouse.getDWheel();
		event.buttonList.add(frequency);
		event.buttonList.add(settings);
	}

	// The click sound has played and Minecraft ignored the unknown button id; now run the action.
	@SubscribeEvent
	public void onAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
		if (event.gui == screen && event.button instanceof OptionButton) {
			((OptionButton) event.button).onPress();
		}
	}

	// Scrolling over Music Frequency cycles it, like in the newer versions. Forge 1.7.10 has no mouse event
	// for screens, so the wheel movement since the last frame is read here; this screen does not use it.
	@SubscribeEvent
	public void onWheel(GuiScreenEvent.DrawScreenEvent.Pre event) {
		if (event.gui != screen || frequency == null) {
			return;
		}
		int wheel = Mouse.getDWheel();
		if (wheel != 0 && frequency.isHovered()) {
			frequency.cycle(wheel > 0 ? -1 : 1);
		}
	}

	@SubscribeEvent
	public void onDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (event.gui != screen) {
			return;
		}
		for (OptionButton button : new OptionButton[] {frequency, settings}) {
			if (button != null && button.isHovered()) {
				button.drawTooltip(event.mouseX, event.mouseY, screen.width, screen.height);
			}
		}
	}
}
