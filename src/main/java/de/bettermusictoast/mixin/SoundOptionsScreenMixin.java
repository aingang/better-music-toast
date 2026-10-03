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
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin {
	/**
	 * Replaces vanilla's "Music Toast" button with one that opens our settings. The vanilla
	 * option itself lives on in our settings screen, so nothing is lost.
	 */
	@WrapOperation(method = "addOptions", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/OptionInstance;)V"))
	private void bettermusictoast$replaceMusicToastButton(OptionsList list, OptionInstance<?>[] options, Operation<Void> original) {
		Options gameOptions = Minecraft.getInstance().options;
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
