package de.bettermusictoast.config;

import com.mojang.serialization.Codec;
import de.bettermusictoast.BetterMusicToastClient;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/** Settings screen built from vanilla option widgets, so it looks like the regular options menus. */
public final class ConfigScreen extends OptionsSubScreen {
	private final ModConfig config;
	private OptionInstance<Integer> durationOption;

	public ConfigScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable("bettermusictoast.config.title"));
		this.config = BetterMusicToastClient.config();
	}

	@Override
	protected void addOptions() {
		this.list.addBig(bool("enabled", config.enabled, v -> config.enabled = v));
		this.list.addBig(enumOption("position", ModConfig.Position.values(), config.position,
				ModConfig.Position::translationKey, v -> config.position = v));
		this.list.addBig(new ColorThemeButton(config, this::showPreview));
		this.list.addBig(enumOption("displayMode", ModConfig.DisplayMode.values(), config.displayMode,
				ModConfig.DisplayMode::translationKey, v -> {
					config.displayMode = v;
					updateDurationSlider();
				}));
		this.durationOption = duration();
		this.list.addSmall(durationOption, size());
		updateDurationSlider();
		this.list.addBig(enumOption("avoidMode", ModConfig.AvoidMode.values(), config.avoidMode,
				ModConfig.AvoidMode::translationKey, v -> config.avoidMode = v));
		this.list.addSmall(
				bool("showArtist", config.showArtist, v -> config.showArtist = v),
				bool("showMusicDiscs", config.showMusicDiscs, v -> config.showMusicDiscs = v));
		this.list.addBig(enumOption("musicStyle", ModConfig.MusicStyle.values(), config.musicStyle,
				ModConfig.MusicStyle::translationKey, v -> config.musicStyle = v));
		this.list.addBig(bool("showInMenus", config.showInMenus, v -> config.showInMenus = v));
		this.list.addBig(bool("hideVanillaToast", config.hideVanillaToast, v -> {
			config.hideVanillaToast = v;
			updateVanillaToastButton();
		}));
		// Moved here from the Music & Sounds screen, where our button takes its place.
		this.list.addBig(this.options.musicToast());
		updateVanillaToastButton();
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

	/** Vanilla's music toast settings are irrelevant (and greyed out) while it is hidden. */
	private void updateVanillaToastButton() {
		if (this.list == null) {
			return;
		}
		AbstractWidget button = this.list.findOption(this.options.musicToast());
		if (button != null) {
			button.active = !config.hideVanillaToast;
		}
	}

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

	/** Slider over the sizes that stay pixel-sharp at the current GUI scale. */
	private OptionInstance<Integer> size() {
		int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
		return new OptionInstance<>(key("size"),
				OptionInstance.cachedConstantTooltip(Component.translatable(key("size") + ".tooltip")),
				(caption, pixels) -> Options.genericValueLabel(caption,
						Component.literal(Math.round(pixels * 100.0f / guiScale) + "%")),
				new OptionInstance.IntRange(ModConfig.minSizePixels(guiScale), ModConfig.maxSizePixels(guiScale)),
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
}
