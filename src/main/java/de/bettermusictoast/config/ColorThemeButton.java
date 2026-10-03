package de.bettermusictoast.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Cycles through the colour themes and draws itself in the selected theme, like a small preview. */
public final class ColorThemeButton extends ThemedButton {
	private static final Component CAPTION = Component.translatable("bettermusictoast.option.colorTheme");

	private final ModConfig config;
	private final Runnable onChange;

	public ColorThemeButton(ModConfig config, Runnable onChange) {
		super(310, Component.empty(), () -> {
		});
		this.config = config;
		this.onChange = onChange;
		refresh();
	}

	@Override
	public void onPress(InputWithModifiers input) {
		ColorTheme[] themes = ColorTheme.values();
		config.colorTheme = themes[(config.colorTheme.ordinal() + 1) % themes.length];
		refresh();
		onChange.run();
	}

	private void refresh() {
		ColorTheme theme = config.colorTheme;
		setMessage(Options.genericValueLabel(CAPTION, Component.translatable(theme.translationKey())));
		setTooltip(Tooltip.create(tooltip(theme)));
	}

	/**
	 * Book themes list both colours with their Japanese name and reading, the English name
	 * underneath in grey, and the source at the bottom.
	 */
	private static Component tooltip(ColorTheme theme) {
		if (theme.bookNumber == null) {
			return Component.translatable(theme.translationKey() + ".tooltip");
		}
		MutableComponent text = Component.empty();
		appendColor(text, theme.borderName, "bettermusictoast.colorTheme.border");
		text.append("\n\n");
		appendColor(text, theme.fillName, "bettermusictoast.colorTheme.background");
		text.append("\n\n");
		text.append(Component.translatable("bettermusictoast.colorTheme.source", theme.bookNumber)
				.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		return text;
	}

	private static void appendColor(MutableComponent text, ColorTheme.ColorName name, String roleKey) {
		text.append(Component.translatable(roleKey).withStyle(ChatFormatting.DARK_GRAY));
		text.append("\n");
		text.append(Component.literal(name.kanji() + "  " + name.romaji()).withStyle(ChatFormatting.WHITE));
		text.append("\n");
		text.append(Component.literal(name.english()).withStyle(ChatFormatting.GRAY));
	}
}
