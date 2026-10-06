package de.bettermusictoast.compat;

import de.bettermusictoast.config.ConfigScreen;
import de.bettermusictoast.config.MusicFrequencyButton;
import de.bettermusictoast.config.OptionButton;
import de.bettermusictoast.config.ThemedButton;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

/**
 * Adds the row the newer versions have in "Music & Sounds" below the volume sliders: the
 * "Music Frequency" option and a button that opens the mod's settings, both in the box's colour theme.
 * The screen's buttons stay on its two-column grid: "Show Subtitles" (alone and centred below the
 * sliders) moves into the free spot next to the last slider, and the new row takes its place.
 */
public final class SoundOptionsButtons {
	// Button ids of Minecraft's sound screen: Done and Show Subtitles.
	private static final int DONE_ID = 200;
	private static final int SUBTITLES_ID = 201;
	// Button ids that Minecraft's sound screen does not use.
	private static final int FREQUENCY_ID = 0x4D54;
	private static final int SETTINGS_ID = 0x4D55;

	private GuiScreen screen;
	private MusicFrequencyButton frequency;
	private ThemedButton settings;

	@SubscribeEvent
	public void onInit(GuiScreenEvent.InitGuiEvent.Post event) {
		if (!(event.getGui() instanceof GuiScreenOptionsSounds)) {
			return;
		}
		final GuiScreen parent = event.getGui();
		screen = parent;
		List<GuiButton> buttons = event.getButtonList();
		int left = parent.width / 2 - 155;
		int right = parent.width / 2 + 5;

		GuiButton subtitles = null;
		GuiButton done = null;
		int lastRowY = Integer.MIN_VALUE;
		for (GuiButton button : buttons) {
			if (button.id == SUBTITLES_ID) {
				subtitles = button;
			} else if (button.id == DONE_ID) {
				done = button;
			} else {
				lastRowY = Math.max(lastRowY, y(button));
			}
		}
		boolean rightFree = lastRowY != Integer.MIN_VALUE;
		for (GuiButton button : buttons) {
			if (button.id != SUBTITLES_ID && button.id != DONE_ID && y(button) == lastRowY && x(button) >= right) {
				rightFree = false;
			}
		}

		int rowY;
		if (subtitles != null && rightFree) {
			// Minecraft's sliders end with "Voice/Speech" on the left; Show Subtitles fills the spot beside it.
			setPosition(subtitles, right, lastRowY);
			rowY = lastRowY + 24;
		} else {
			rowY = (subtitles != null ? y(subtitles) : lastRowY) + 24;
		}
		if (done != null && y(done) < rowY + 24) {
			setPosition(done, x(done), rowY + 24 + 12);
		}

		frequency = new MusicFrequencyButton();
		frequency.id = FREQUENCY_ID;
		frequency.setPosition(left, rowY);
		settings = new ThemedButton(150, I18n.format("bettermusictoast.soundOptions.button"), new Runnable() {
			@Override
			public void run() {
				Minecraft.getMinecraft().displayGuiScreen(new ConfigScreen(parent));
			}
		});
		settings.setTooltip(I18n.format("bettermusictoast.soundOptions.button.tooltip"));
		settings.id = SETTINGS_ID;
		settings.setPosition(right, rowY);
		buttons.add(frequency);
		buttons.add(settings);
	}

	// The click sound has played and Minecraft ignored the unknown button id; now run the action.
	@SubscribeEvent
	public void onAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
		if (event.getGui() == screen && event.getButton() instanceof OptionButton) {
			((OptionButton) event.getButton()).onPress();
		}
	}

	// Scrolling over Music Frequency cycles it, like in the newer versions.
	@SubscribeEvent
	public void onMouse(GuiScreenEvent.MouseInputEvent.Post event) {
		if (event.getGui() != screen || frequency == null) {
			return;
		}
		int wheel = Mouse.getEventDWheel();
		if (wheel != 0 && frequency.isHovered()) {
			frequency.cycle(wheel > 0 ? -1 : 1);
		}
	}

	@SubscribeEvent
	public void onDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (event.getGui() != screen) {
			return;
		}
		for (OptionButton button : new OptionButton[] {frequency, settings}) {
			if (button != null && button.isHovered()) {
				button.drawTooltip(event.getMouseX(), event.getMouseY(), screen.width, screen.height);
			}
		}
	}

	// Minecraft's own buttons; their position fields were renamed in 1.11.

	private static int x(GuiButton button) {
		//#if MC>=11100
		return button.x;
		//#else
		//$$ return button.xPosition;
		//#endif
	}

	private static int y(GuiButton button) {
		//#if MC>=11100
		return button.y;
		//#else
		//$$ return button.yPosition;
		//#endif
	}

	private static void setPosition(GuiButton button, int x, int y) {
		//#if MC>=11100
		button.x = x;
		button.y = y;
		//#else
		//$$ button.xPosition = x;
		//$$ button.yPosition = y;
		//#endif
	}
}
