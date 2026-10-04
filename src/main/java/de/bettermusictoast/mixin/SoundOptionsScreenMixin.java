package de.bettermusictoast.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.bettermusictoast.compat.McCompat;
import de.bettermusictoast.config.ConfigScreen;
import de.bettermusictoast.config.ThemedButton;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
//? if >=1.19
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
//? if >=1.19.3
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

//? if >=1.19.3 {
@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin {
//?} else {
/*@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin extends Screen {
	//? if >=1.19 {
	@org.spongepowered.asm.mixin.Shadow
	private AbstractWidget directionalAudioButton;
	//?}

	@org.spongepowered.asm.mixin.Unique
	private final List<ThemedButton> bettermusictoast$row = new ArrayList<>();

	private SoundOptionsScreenMixin(Component title) {
		super(title);
	}

	/^*
	 * Before 1.19.3 the screen places its buttons itself, without an option list. The row 1.21.6+ has
	 * below Show Subtitles / Directional Audio (Music Frequency and our button) goes where the Done
	 * button was, and Done moves down one row (before 1.19 see makeRoom).
	 ^/
	@org.spongepowered.asm.mixin.injection.Inject(method = "init", at = @At("TAIL"))
	private void bettermusictoast$addRow(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
		//? if >=1.19 {
		int y = directionalAudioButton.y + 22;
		for (net.minecraft.client.gui.components.events.GuiEventListener child : children()) {
			if (child instanceof net.minecraft.client.gui.components.Button done
					&& done.getMessage() == net.minecraft.network.chat.CommonComponents.GUI_DONE) {
				done.y += 22;
			}
		}
		//?} else {
		/^int y = bettermusictoast$makeRoom();
		^///?}
		ThemedButton frequency = new de.bettermusictoast.config.MusicFrequencyButton();
		frequency.x = width / 2 - 155;
		frequency.y = y;
		ThemedButton settings = createSettingsButton();
		settings.x = width / 2 + 5;
		settings.y = y;
		bettermusictoast$row.clear();
		bettermusictoast$row.add(frequency);
		bettermusictoast$row.add(settings);
		addRenderableWidget(frequency);
		addRenderableWidget(settings);
	}

	//? if <1.19 {
	/^// Before 1.19 there is no Directional Audio: Show Subtitles sits alone in the middle below Device,
	// while the slot next to the last volume slider (Voice/Speech) stays empty. Show Subtitles moves into
	// that slot and our row takes its place, so the screen keeps its two-column grid and Done stays where
	// it is. Returns the height of our row.
	@org.spongepowered.asm.mixin.Unique
	private int bettermusictoast$makeRoom() {
		net.minecraft.client.gui.components.Button done = null;
		for (net.minecraft.client.gui.components.events.GuiEventListener child : children()) {
			if (child instanceof net.minecraft.client.gui.components.Button button
					&& button.getMessage() == net.minecraft.network.chat.CommonComponents.GUI_DONE) {
				done = button;
			}
		}
		if (done == null) {
			return height / 6 - 12 + 22 * 8;
		}
		int subtitlesY = done.y - 22;
		int slotX = width / 2 + 5;
		int slotY = subtitlesY - 44;
		AbstractWidget subtitles = null;
		boolean slotFree = true;
		for (net.minecraft.client.gui.components.events.GuiEventListener child : children()) {
			if (child instanceof AbstractWidget widget && widget != done) {
				if (widget.y == subtitlesY) {
					subtitles = widget;
				}
				if (widget.y == slotY && widget.x == slotX) {
					slotFree = false;
				}
			}
		}
		if (subtitles != null && slotFree) {
			subtitles.x = slotX;
			subtitles.y = slotY;
			return subtitlesY;
		}
		// Laid out differently (e.g. by another mod): our row goes where Done was, and Done one row lower.
		int y = done.y;
		done.y += 22;
		return y;
	}
	^///?}

	// The screen only shows the tooltip of Directional Audio itself, so show ours as well.
	@org.spongepowered.asm.mixin.injection.Inject(method = "render", at = @At("TAIL"))
	private void bettermusictoast$renderTooltips(com.mojang.blaze3d.vertex.PoseStack pose, int mouseX, int mouseY,
			float partialTick, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
		for (ThemedButton button : bettermusictoast$row) {
			if (button.isMouseOver(mouseX, mouseY)) {
				renderTooltip(pose, button.getTooltip(), mouseX, mouseY);
			}
		}
	}
*///?}

	//? if >=1.19.3 {
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
	//?}

	/** Drawn in the selected colour theme, so it doubles as a small preview of the song box. */
	private ThemedButton createSettingsButton() {
		Screen parent = (Screen) (Object) this;
		ThemedButton button = new ThemedButton(150, Component.translatable("bettermusictoast.soundOptions.button"),
				() -> McCompat.setScreen(Minecraft.getInstance(), new ConfigScreen(parent)));
		//? if >=1.19.3 {
		button.setTooltip(Tooltip.create(Component.translatable("bettermusictoast.soundOptions.button.tooltip")));
		//?} else
		/*button.setTooltipText(Component.translatable("bettermusictoast.soundOptions.button.tooltip"));*/
		return button;
	}
}
