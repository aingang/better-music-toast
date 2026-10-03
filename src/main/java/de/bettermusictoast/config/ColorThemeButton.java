package de.bettermusictoast.config;

import de.bettermusictoast.hud.NowPlayingHud;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.joml.Matrix3x2fStack;

/** Cycles through the colour themes and draws itself in the selected theme, like a small preview. */
public final class ColorThemeButton extends AbstractButton {
	private static final Component CAPTION = Component.translatable("bettermusictoast.option.colorTheme");

	private final ModConfig config;

	public ColorThemeButton(ModConfig config) {
		super(0, 0, 310, 20, Component.empty());
		this.config = config;
		refresh();
	}

	@Override
	public void onPress(InputWithModifiers input) {
		ColorTheme[] themes = ColorTheme.values();
		config.colorTheme = themes[(config.colorTheme.ordinal() + 1) % themes.length];
		refresh();
	}

	private void refresh() {
		ColorTheme theme = config.colorTheme;
		setMessage(Options.genericValueLabel(CAPTION, Component.translatable(theme.translationKey())));
		setTooltip(Tooltip.create(tooltip(theme)));
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		ColorTheme theme = config.colorTheme;
		int border = isHoveredOrFocused() ? ColorTheme.mix(theme.border, 0xFFFFFF, 0.35f) : theme.border;
		Font font = Minecraft.getInstance().font;
		Component message = getMessage();

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(getX(), getY());
		NowPlayingHud.drawFrame(graphics, getWidth(), getHeight(), theme, border, 1.0f);
		graphics.text(font, message, (getWidth() - font.width(message)) / 2, (getHeight() - 8) / 2,
				NowPlayingHud.argb(theme.title, 1.0f), false);
		pose.popMatrix();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
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
