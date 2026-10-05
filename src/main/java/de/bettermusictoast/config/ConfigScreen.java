package de.bettermusictoast.config;

import com.mojang.serialization.Codec;
import de.bettermusictoast.BetterMusicToastClient;
import de.bettermusictoast.compat.McCompat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
//? if >=1.19 {
import net.minecraft.client.OptionInstance;
//?} else {
/*import net.minecraft.client.CycleOption;
import net.minecraft.client.Option;
import net.minecraft.client.ProgressOption;
//? if >=1.17 {
import net.minecraft.client.gui.components.CycleButton;
//?} else
/^import net.minecraft.client.BooleanOption;^/
import net.minecraft.util.FormattedCharSequence;
*///?}
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21 {
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
//?} else if >=1.16.2 {
/*import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
*///?} else {
/*import net.minecraft.client.gui.screens.OptionsSubScreen;
*///?}
import net.minecraft.network.chat.Component;

/** Settings screen built from vanilla option widgets, so it looks like the regular options menus. */
//? if >=1.21 {
public final class ConfigScreen extends OptionsSubScreen {
//?} else if >=1.16.2 {
/*// Before 1.21 option screens fill their list in init(); SimpleOptionsSubScreen provides the list,
// title and Done button exactly like the vanilla screens of that version.
public final class ConfigScreen extends SimpleOptionsSubScreen {
*///?} else {
/*// Before 1.16.2 there is no SimpleOptionsSubScreen yet; the screen sets up its list, title, Done button
// and tooltips itself, the way the vanilla option screens of that version do.
public final class ConfigScreen extends OptionsSubScreen {
*///?}
	private final ModConfig config;
	//? if >=1.19 {
	private OptionInstance<Integer> durationOption;
	//?} else
	/*private Option durationOption;*/
	//? if >=1.16.2 && <1.19.3 {
	/*// Before 1.19.3 the list of SimpleOptionsSubScreen is private; this field stands in for it.
	private net.minecraft.client.gui.components.OptionsList list;
	*///?} else if <1.16.2 {
	/*private TrackingList list;
	*///?}

	public ConfigScreen(Screen parent) {
		//? if >=1.21 {
		super(parent, Minecraft.getInstance().options, Component.translatable("bettermusictoast.config.title"));
		//?} else if >=1.16.2 {
		/*super(parent, Minecraft.getInstance().options, Component.translatable("bettermusictoast.config.title"),
				//? if >=1.19 {
				new OptionInstance<?>[0]);
				//?} else
				/^new Option[0]);^/
		*///?} else {
		/*super(parent, Minecraft.getInstance().options, Component.translatable("bettermusictoast.config.title"));
		*///?}
		this.config = BetterMusicToastClient.config();
	}

	//? if >=1.16.2 && <1.21 {
	/*@Override
	protected void init() {
		super.init();
		//? if <1.19.3
		/^this.list = ((de.bettermusictoast.mixin.SimpleOptionsSubScreenAccessor) (Object) this).bettermusictoast$getList();^/
		addOptions();
	}

	private void addOptions() {
	*///?} else if <1.16.2 {
	/*@Override
	protected void init() {
		this.list = new TrackingList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		addOptions();
		this.children.add(this.list);
		addButton(new net.minecraft.client.gui.components.Button(this.width / 2 - 100, this.height - 27, 200, 20,
				net.minecraft.network.chat.CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen)));
	}

	@Override
	public void render(com.mojang.blaze3d.vertex.PoseStack pose, int mouseX, int mouseY, float partialTick) {
		renderBackground(pose);
		this.list.render(pose, mouseX, mouseY, partialTick);
		drawCenteredString(pose, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
		super.render(pose, mouseX, mouseY, partialTick);
		List<net.minecraft.util.FormattedCharSequence> tooltip = this.list.tooltipAt(mouseX, mouseY);
		if (tooltip != null) {
			renderTooltip(pose, tooltip, mouseX, mouseY);
		}
	}

	private void addOptions() {
	*///?} else {
	@Override
	protected void addOptions() {
	//?}
		this.list.addBig(bool("enabled", config.enabled, v -> config.enabled = v));
		this.list.addBig(enumOption("position", ModConfig.Position.values(), config.position,
				ModConfig.Position::translationKey, v -> config.position = v));
		McCompat.addWide(this.list, new ColorThemeButton(config, this::showPreview));
		this.list.addBig(enumOption("displayMode", ModConfig.DisplayMode.values(), config.displayMode,
				ModConfig.DisplayMode::translationKey, v -> {
					config.displayMode = v;
					updateDurationSlider();
				}));
		this.durationOption = duration();
		this.list.addSmall(durationOption, size());
		updateDurationSlider();
		this.list.addSmall(
				enumOption("avoidMode", ModConfig.AvoidMode.values(), config.avoidMode,
						ModConfig.AvoidMode::translationKey, v -> config.avoidMode = v),
				bool("animateIcon", config.animateIcon, v -> config.animateIcon = v));
		this.list.addSmall(
				bool("showArtist", config.showArtist, v -> config.showArtist = v),
				bool("showMusicDiscs", config.showMusicDiscs, v -> config.showMusicDiscs = v));
		this.list.addBig(enumOption("musicStyle", ModConfig.MusicStyle.values(), config.musicStyle,
				ModConfig.MusicStyle::translationKey, v -> config.musicStyle = v));
		this.list.addBig(bool("showInMenus", config.showInMenus, v -> config.showInMenus = v));
		// Minecraft only has its own music toast since 1.21.6.
		//? if >=1.21.6 {
		this.list.addBig(bool("hideVanillaToast", config.hideVanillaToast, v -> {
			config.hideVanillaToast = v;
			updateVanillaToastButton();
		}));
		// Moved here from the Music & Sounds screen, where our button takes its place.
		this.list.addBig(McCompat.vanillaToastOption(this.options));
		updateVanillaToastButton();
		//?}
		showPreview();
	}

	/** Shows the song box at its configured position for a few seconds, so every change is visible. */
	private void showPreview() {
		BetterMusicToastClient.tracker().showSettingsPreview();
	}

	private <T> Consumer<T> withPreview(Consumer<T> setter) {
		return value -> {
			setter.accept(value);
			showPreview();
		};
	}

	/** The seconds only matter in timed mode. */
	private void updateDurationSlider() {
		if (this.list == null || this.durationOption == null) {
			return;
		}
		AbstractWidget slider = this.list.findOption(this.durationOption);
		if (slider != null) {
			slider.active = config.displayMode == ModConfig.DisplayMode.TIMED;
		}
	}

	//? if >=1.21.6 {
	/** Vanilla's music toast settings are irrelevant (and greyed out) while it is hidden. */
	private void updateVanillaToastButton() {
		if (this.list == null) {
			return;
		}
		AbstractWidget button = this.list.findOption(McCompat.vanillaToastOption(this.options));
		if (button != null) {
			button.active = !config.hideVanillaToast;
		}
	}
	//?}

	@Override
	public void removed() {
		super.removed();
		config.save();
		// Give immediate feedback of the new look when returning to the game.
		if (Minecraft.getInstance().level != null) {
			BetterMusicToastClient.tracker().preview();
		}
	}

	private static String key(String name) {
		return "bettermusictoast.option." + name;
	}

	//? if >=1.19 {
	private OptionInstance<Boolean> bool(String name, boolean value, Consumer<Boolean> setter) {
		return OptionInstance.createBoolean(key(name),
				OptionInstance.cachedConstantTooltip(Component.translatable(key(name) + ".tooltip")),
				value, withPreview(setter)::accept);
	}

	private <E extends Enum<E>> OptionInstance<E> enumOption(String name, E[] values, E value,
			Function<E, String> translationKey, Consumer<E> setter) {
		Codec<E> codec = Codec.STRING.xmap(s -> Arrays.stream(values).filter(e -> e.name().equals(s)).findFirst().orElse(value), Enum::name);
		return new OptionInstance<>(key(name),
				OptionInstance.cachedConstantTooltip(Component.translatable(key(name) + ".tooltip")),
				// Cycle buttons prepend the caption themselves.
				(caption, v) -> Component.translatable(translationKey.apply(v)),
				new OptionInstance.Enum<>(Arrays.asList(values), codec),
				value, withPreview(setter)::accept);
	}

	/**
	 * Cycles through the sizes that stay pixel-sharp at the current GUI scale, like vanilla's GUI
	 * Scale button: click for the next size, shift-click for the previous one.
	 */
	private OptionInstance<Integer> size() {
		// The cast is only needed before 1.21.6, where the GUI scale is a double.
		int guiScale = (int) Minecraft.getInstance().getWindow().getGuiScale();
		List<Integer> sizes = new ArrayList<>();
		for (int pixels = ModConfig.minSizePixels(guiScale); pixels <= ModConfig.maxSizePixels(guiScale); pixels++) {
			sizes.add(pixels);
		}
		return new OptionInstance<>(key("size"),
				OptionInstance.cachedConstantTooltip(Component.translatable(key("size") + ".tooltip")),
				// Cycle buttons prepend the caption themselves.
				(caption, pixels) -> Component.literal(Math.round(pixels * 100.0f / guiScale) + "%"),
				new OptionInstance.Enum<>(sizes, Codec.INT),
				config.sizePixels(guiScale),
				withPreview((Integer pixels) -> config.sizePercent = Math.round(pixels * 100.0f / guiScale))::accept);
	}

	private OptionInstance<Integer> duration() {
		return new OptionInstance<>(key("duration"),
				OptionInstance.cachedConstantTooltip(Component.translatable(key("duration") + ".tooltip")),
				(caption, v) -> Options.genericValueLabel(caption, Component.translatable("bettermusictoast.seconds", v)),
				new OptionInstance.IntRange(2, 30),
				config.durationSeconds, withPreview((Integer v) -> config.durationSeconds = v)::accept);
	}
	//?} else if >=1.17 {
	/*// Before 1.19 the same four kinds of options are built from CycleOption and ProgressOption. They read
	// their start value once when the button is created and report every change to the setter.

	private static <T> Function<Minecraft, CycleButton.TooltipSupplier<T>> tooltip(String name) {
		return mc -> {
			List<FormattedCharSequence> lines = mc.font.split(Component.translatable(key(name) + ".tooltip"), 200);
			return value -> lines;
		};
	}

	private Option bool(String name, boolean value, Consumer<Boolean> setter) {
		return CycleOption.createOnOff(key(name), options -> value, (options, option, v) -> withPreview(setter).accept(v))
				.setTooltip(tooltip(name));
	}

	private <E extends Enum<E>> Option enumOption(String name, E[] values, E value,
			Function<E, String> translationKey, Consumer<E> setter) {
		return CycleOption.create(key(name), values, v -> Component.translatable(translationKey.apply(v)), options -> value,
				(options, option, v) -> withPreview(setter).accept(v))
				.setTooltip(tooltip(name));
	}

	/^*
	 * Cycles through the sizes that stay pixel-sharp at the current GUI scale, like vanilla's GUI
	 * Scale button: click for the next size, shift-click for the previous one.
	 ^/
	private Option size() {
		int guiScale = (int) Minecraft.getInstance().getWindow().getGuiScale();
		List<Integer> sizes = new ArrayList<>();
		for (int pixels = ModConfig.minSizePixels(guiScale); pixels <= ModConfig.maxSizePixels(guiScale); pixels++) {
			sizes.add(pixels);
		}
		int start = config.sizePixels(guiScale);
		return CycleOption.create(key("size"), sizes, pixels -> Component.literal(Math.round(pixels * 100.0f / guiScale) + "%"),
				options -> start,
				(options, option, pixels) -> withPreview((Integer p) -> config.sizePercent = Math.round(p * 100.0f / guiScale)).accept(pixels))
				.setTooltip(tooltip("size"));
	}

	private Option duration() {
		List<FormattedCharSequence> tooltip = Minecraft.getInstance().font.split(
				Component.translatable(key("duration") + ".tooltip"), 200);
		return new ProgressOption(key("duration"), 2, 30, 1.0f,
				options -> (double) config.durationSeconds,
				(options, v) -> {
					int seconds = (int) Math.round(v);
					if (seconds != config.durationSeconds) {
						withPreview((Integer s) -> config.durationSeconds = s).accept(seconds);
					}
				},
				(options, option) -> Options.genericValueLabel(Component.translatable(key("duration")),
						Component.translatable("bettermusictoast.seconds", config.durationSeconds)),
				mc -> tooltip);
	}
	*///?} else {
	/*// Before 1.17 the options are BooleanOption, CycleOption (stepping through an index, which the label
	// shows) and ProgressOption, with their tooltip set directly. Each keeps its current value itself.

	private static List<net.minecraft.util.FormattedCharSequence> tooltipLines(String name) {
		return Minecraft.getInstance().font.split(Component.translatable(key(name) + ".tooltip"), 200);
	}

	// Shift-click steps backwards, like the cycle buttons of 1.17+.
	private static int cycleStep(int step) {
		return Screen.hasShiftDown() ? -step : step;
	}

	private Option bool(String name, boolean value, Consumer<Boolean> setter) {
		boolean[] state = {value};
		BooleanOption option = new BooleanOption(key(name), options -> state[0], (options, v) -> {
			state[0] = v;
			withPreview(setter).accept(v);
		});
		option.setTooltip(tooltipLines(name));
		return option;
	}

	private <E extends Enum<E>> Option enumOption(String name, E[] values, E value,
			Function<E, String> translationKey, Consumer<E> setter) {
		int[] index = {Math.max(0, Arrays.asList(values).indexOf(value))};
		CycleOption option = new CycleOption(key(name), (options, step) -> {
			index[0] = Math.floorMod(index[0] + cycleStep(step), values.length);
			withPreview(setter).accept(values[index[0]]);
		}, (options, o) -> Options.genericValueLabel(Component.translatable(key(name)),
				Component.translatable(translationKey.apply(values[index[0]]))));
		option.setTooltip(tooltipLines(name));
		return option;
	}

	/^*
	 * Cycles through the sizes that stay pixel-sharp at the current GUI scale, like vanilla's GUI
	 * Scale button: click for the next size, shift-click for the previous one.
	 ^/
	private Option size() {
		int guiScale = (int) Minecraft.getInstance().getWindow().getGuiScale();
		List<Integer> sizes = new ArrayList<>();
		for (int pixels = ModConfig.minSizePixels(guiScale); pixels <= ModConfig.maxSizePixels(guiScale); pixels++) {
			sizes.add(pixels);
		}
		int[] index = {Math.max(0, sizes.indexOf(config.sizePixels(guiScale)))};
		CycleOption option = new CycleOption(key("size"), (options, step) -> {
			index[0] = Math.floorMod(index[0] + cycleStep(step), sizes.size());
			withPreview((Integer p) -> config.sizePercent = Math.round(p * 100.0f / guiScale)).accept(sizes.get(index[0]));
		}, (options, o) -> Options.genericValueLabel(Component.translatable(key("size")),
				Component.literal(Math.round(sizes.get(index[0]) * 100.0f / guiScale) + "%")));
		option.setTooltip(tooltipLines("size"));
		return option;
	}

	private Option duration() {
		ProgressOption option = new ProgressOption(key("duration"), 2, 30, 1.0f,
				options -> (double) config.durationSeconds,
				(options, v) -> {
					int seconds = (int) Math.round(v);
					if (seconds != config.durationSeconds) {
						withPreview((Integer s) -> config.durationSeconds = s).accept(seconds);
					}
				},
				(options, o) -> Options.genericValueLabel(Component.translatable(key("duration")),
						Component.translatable("bettermusictoast.seconds", config.durationSeconds)));
		option.setTooltip(tooltipLines("duration"));
		return option;
	}
	*///?}

	//? if <1.16.2 {
	/*/^*
	 * Before 1.16.2 an option list cannot look up an option's button, and option buttons do not show their
	 * tooltip themselves. This list remembers the button of every option it adds.
	 ^/
	private static final class TrackingList extends net.minecraft.client.gui.components.OptionsList {
		private final java.util.Map<AbstractWidget, Option> buttons = new java.util.HashMap<>();

		TrackingList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
			super(minecraft, width, height, top, bottom, itemHeight);
		}

		@Override
		public int addBig(Option option) {
			int index = super.addBig(option);
			remember(option, 0);
			return index;
		}

		@Override
		public void addSmall(Option left, Option right) {
			super.addSmall(left, right);
			remember(left, 0);
			if (right != null) {
				remember(right, 1);
			}
		}

		private void remember(Option option, int column) {
			List<? extends net.minecraft.client.gui.components.events.GuiEventListener> widgets =
					children().get(children().size() - 1).children();
			if (column < widgets.size()) {
				buttons.put((AbstractWidget) widgets.get(column), option);
			}
		}

		AbstractWidget findOption(Option option) {
			for (java.util.Map.Entry<AbstractWidget, Option> entry : buttons.entrySet()) {
				if (entry.getValue() == option) {
					return entry.getKey();
				}
			}
			return null;
		}

		// The tooltip of the button under the mouse: the mod's own buttons carry theirs, vanilla's come from
		// their option.
		List<net.minecraft.util.FormattedCharSequence> tooltipAt(int x, int y) {
			java.util.Optional<AbstractWidget> widget = getMouseOver(x, y);
			if (!widget.isPresent()) {
				return null;
			}
			if (widget.get() instanceof ThemedButton button) {
				return button.getTooltip().orElse(null);
			}
			Option option = buttons.get(widget.get());
			return option == null ? null : option.getTooltip().orElse(null);
		}
	}
	*///?}
}
