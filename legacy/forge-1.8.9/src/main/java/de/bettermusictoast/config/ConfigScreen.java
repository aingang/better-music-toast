package de.bettermusictoast.config;

import de.bettermusictoast.BetterMusicToast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

/**
 * Settings screen laid out like Minecraft's option screens: title, a scrolling list of options
 * and a Done button. Same options as in the newer versions, except the ones 1.8.9 cannot have
 * (music selection, Minecraft's own music toast).
 */
public final class ConfigScreen extends GuiScreen {
	private static final int DONE = 200;

	private final GuiScreen parent;
	private final ModConfig config;
	private OptionList list;
	private DurationSlider durationSlider;

	/** Also used by Forge's mod list (see GuiFactory). */
	public ConfigScreen(GuiScreen parent) {
		this.parent = parent;
		this.config = BetterMusicToast.config();
	}

	@Override
	public void initGui() {
		buttonList.clear();
		buttonList.add(new GuiButton(DONE, width / 2 - 100, height - 27, I18n.format("gui.done")));

		list = new OptionList(mc, width, height);
		list.addBig(bool("enabled", config.enabled, new CycleButton.Listener<Boolean>() {
			@Override
			public void changed(Boolean v) {
				config.enabled = v;
			}
		}));
		list.addBig(enumOption("position", ModConfig.Position.values(), config.position,
				new CycleButton.Labeler<ModConfig.Position>() {
					@Override
					public String label(ModConfig.Position v) {
						return I18n.format(v.translationKey());
					}
				}, new CycleButton.Listener<ModConfig.Position>() {
					@Override
					public void changed(ModConfig.Position v) {
						config.position = v;
					}
				}));
		list.addBig(new ColorThemeButton(config, new Runnable() {
			@Override
			public void run() {
				showPreview();
			}
		}));
		list.addBig(enumOption("displayMode", ModConfig.DisplayMode.values(), config.displayMode,
				new CycleButton.Labeler<ModConfig.DisplayMode>() {
					@Override
					public String label(ModConfig.DisplayMode v) {
						return I18n.format(v.translationKey());
					}
				}, new CycleButton.Listener<ModConfig.DisplayMode>() {
					@Override
					public void changed(ModConfig.DisplayMode v) {
						config.displayMode = v;
						updateDurationSlider();
					}
				}));
		durationSlider = new DurationSlider(150, key("duration"), config.durationSeconds, withPreview(new CycleButton.Listener<Integer>() {
			@Override
			public void changed(Integer v) {
				config.durationSeconds = v;
			}
		}));
		list.addSmall(durationSlider, size());
		updateDurationSlider();
		list.addSmall(
				enumOption("avoidMode", ModConfig.AvoidMode.values(), config.avoidMode,
						new CycleButton.Labeler<ModConfig.AvoidMode>() {
							@Override
							public String label(ModConfig.AvoidMode v) {
								return I18n.format(v.translationKey());
							}
						}, new CycleButton.Listener<ModConfig.AvoidMode>() {
							@Override
							public void changed(ModConfig.AvoidMode v) {
								config.avoidMode = v;
							}
						}),
				bool("animateIcon", config.animateIcon, new CycleButton.Listener<Boolean>() {
					@Override
					public void changed(Boolean v) {
						config.animateIcon = v;
					}
				}));
		list.addSmall(
				bool("showArtist", config.showArtist, new CycleButton.Listener<Boolean>() {
					@Override
					public void changed(Boolean v) {
						config.showArtist = v;
					}
				}),
				bool("showMusicDiscs", config.showMusicDiscs, new CycleButton.Listener<Boolean>() {
					@Override
					public void changed(Boolean v) {
						config.showMusicDiscs = v;
					}
				}));
		list.addBig(bool("showInMenus", config.showInMenus, new CycleButton.Listener<Boolean>() {
			@Override
			public void changed(Boolean v) {
				config.showInMenus = v;
			}
		}));
		showPreview();
	}

	/** Shows the song box at its configured position for a few seconds, so every change is visible. */
	private void showPreview() {
		BetterMusicToast.tracker().showSettingsPreview();
	}

	private <T> CycleButton.Listener<T> withPreview(final CycleButton.Listener<T> setter) {
		return new CycleButton.Listener<T>() {
			@Override
			public void changed(T value) {
				setter.changed(value);
				showPreview();
			}
		};
	}

	/** The seconds only matter in timed mode. */
	private void updateDurationSlider() {
		if (durationSlider != null) {
			durationSlider.enabled = config.displayMode == ModConfig.DisplayMode.TIMED;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		list.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRendererObj, I18n.format("bettermusictoast.config.title"), width / 2, 12, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
		OptionButton hovered = list.hoveredButton(mouseY);
		if (hovered != null) {
			hovered.drawTooltip(mouseX, mouseY, width, height);
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		list.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		list.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		list.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled && button.id == DONE) {
			mc.displayGuiScreen(parent);
		}
	}

	// Escape goes back to the previous screen, like in the newer versions.
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(parent);
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void onGuiClosed() {
		config.save();
		// Give immediate feedback of the new look when returning to the game.
		if (mc.theWorld != null) {
			BetterMusicToast.tracker().preview();
		}
	}

	private static String key(String name) {
		return "bettermusictoast.option." + name;
	}

	private CycleButton<Boolean> bool(String name, boolean value, CycleButton.Listener<Boolean> setter) {
		return new CycleButton<Boolean>(150, key(name), Arrays.asList(Boolean.FALSE, Boolean.TRUE), value,
				new CycleButton.Labeler<Boolean>() {
					@Override
					public String label(Boolean v) {
						return I18n.format(v ? "options.on" : "options.off");
					}
				}, withPreview(setter));
	}

	private <E extends Enum<E>> CycleButton<E> enumOption(String name, E[] values, E value,
			CycleButton.Labeler<E> labeler, CycleButton.Listener<E> setter) {
		return new CycleButton<E>(150, key(name), Arrays.asList(values), value, labeler, withPreview(setter));
	}

	/**
	 * Cycles through the sizes that stay pixel-sharp at the current GUI scale, like vanilla's GUI
	 * Scale button: click for the next size, shift-click for the previous one.
	 */
	private CycleButton<Integer> size() {
		final int guiScale = new ScaledResolution(mc).getScaleFactor();
		List<Integer> sizes = new ArrayList<Integer>();
		for (int pixels = ModConfig.minSizePixels(guiScale); pixels <= ModConfig.maxSizePixels(guiScale); pixels++) {
			sizes.add(pixels);
		}
		return new CycleButton<Integer>(150, key("size"), sizes, config.sizePixels(guiScale),
				new CycleButton.Labeler<Integer>() {
					@Override
					public String label(Integer pixels) {
						return Math.round(pixels * 100.0f / guiScale) + "%";
					}
				}, withPreview(new CycleButton.Listener<Integer>() {
					@Override
					public void changed(Integer pixels) {
						config.sizePercent = Math.round(pixels * 100.0f / guiScale);
					}
				}));
	}
}
