package de.bettermusictoast.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.bettermusictoast.compat.McCompat;
import de.bettermusictoast.config.ConfigScreen;
import de.bettermusictoast.config.ThemedButton;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21 {
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
//?} else {
/*import net.minecraft.client.gui.screens.SoundOptionsScreen;
*///?}
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin {
	/**
	 * Replaces vanilla's "Music Toast" button with one that opens our settings. The vanilla
	 * option itself lives on in our settings screen, so nothing is lost.
	 */
	// Before 1.21 the screen fills its list in init().
	//? if >=1.21 {
	@WrapOperation(method = "addOptions", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/OptionInstance;)V"))
	//?} else {
	/*@WrapOperation(method = "init", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/OptionInstance;)V"))
	*///?}
	private void bettermusictoast$replaceMusicToastButton(OptionsList list, OptionInstance<?>[] options, Operation<Void> original) {
		Options gameOptions = Minecraft.getInstance().options;
		// Before 1.21.6 there is neither a Music Frequency nor a music toast button, so the mod adds
		// the row 1.21.6+ has below Show Subtitles / Directional Audio: Music Frequency and our button.
		//? if <1.21 {
		/*original.call(list, options);
		if (java.util.Arrays.asList(options).contains(gameOptions.directionalAudio())) {
			// Option lists only take options before 1.21 (see WidgetOption).
			list.addSmall(de.bettermusictoast.compat.WidgetOption.of(new de.bettermusictoast.config.MusicFrequencyButton()),
					de.bettermusictoast.compat.WidgetOption.of(createSettingsButton()));
		}
		*///?} else if <1.21.6 {
		/*original.call(list, options);
		if (java.util.Arrays.asList(options).contains(gameOptions.directionalAudio())) {
			list.addSmall(List.of(new de.bettermusictoast.config.MusicFrequencyButton(), createSettingsButton()));
		}
		*///?} else {
		OptionInstance<?> musicToast = McCompat.vanillaToastOption(gameOptions);

		List<AbstractWidget> widgets = new ArrayList<>();
		boolean replaced = false;
		for (OptionInstance<?> option : options) {
			if (option == musicToast) {
				widgets.add(createSettingsButton());
				replaced = true;
			} else {
				widgets.add(option.createButton(gameOptions));
			}
		}

		if (replaced) {
			list.addSmall(widgets);
		} else {
			original.call(list, options);
		}
		//?}
	}

	/** Drawn in the selected colour theme, so it doubles as a small preview of the song box. */
	private ThemedButton createSettingsButton() {
		Screen parent = (Screen) (Object) this;
		ThemedButton button = new ThemedButton(150, Component.translatable("bettermusictoast.soundOptions.button"),
				() -> McCompat.setScreen(Minecraft.getInstance(), new ConfigScreen(parent)));
		button.setTooltip(Tooltip.create(Component.translatable("bettermusictoast.soundOptions.button.tooltip")));
		return button;
	}
}
