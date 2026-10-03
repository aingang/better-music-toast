package de.bettermusictoast.config;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

/** Cycles through the colour themes and draws itself in the selected theme, like a small preview. */
public final class ColorThemeButton extends ThemedButton {
	private final ModConfig config;
	private final Runnable onChange;

	public ColorThemeButton(ModConfig config, Runnable onChange) {
		super(310, "", new Runnable() {
			@Override
			public void run() {
			}
		});
		this.config = config;
		this.onChange = onChange;
		refresh();
	}

	@Override
	public void onPress() {
		ColorTheme[] themes = ColorTheme.values();
		config.colorTheme = themes[(config.colorTheme.ordinal() + 1) % themes.length];
		refresh();
		onChange.run();
	}

	private void refresh() {
		ColorTheme theme = config.colorTheme;
		displayString = I18n.format("bettermusictoast.option.colorTheme") + ": " + I18n.format(theme.translationKey());
		setTooltip(tooltip(theme));
	}

	/**
	 * Book themes list both colours with their Japanese name and reading, the English name
	 * underneath in grey, and the source at the bottom.
	 */
	private static String tooltip(ColorTheme theme) {
		if (theme.bookNumber == null) {
			return I18n.format(theme.translationKey() + ".tooltip");
		}
		StringBuilder text = new StringBuilder();
		appendColor(text, theme.borderName, "bettermusictoast.colorTheme.border");
		text.append("\n\n");
		appendColor(text, theme.fillName, "bettermusictoast.colorTheme.background");
		text.append("\n\n");
		text.append(EnumChatFormatting.DARK_GRAY).append(EnumChatFormatting.ITALIC)
				.append(I18n.format("bettermusictoast.colorTheme.source", theme.bookNumber));
		return text.toString();
	}

	private static void appendColor(StringBuilder text, ColorTheme.ColorName name, String roleKey) {
		text.append(EnumChatFormatting.DARK_GRAY).append(I18n.format(roleKey)).append("\n");
		text.append(EnumChatFormatting.WHITE).append(name.kanji).append("  ").append(name.romaji).append("\n");
		text.append(EnumChatFormatting.GRAY).append(name.english);
	}
}
