package de.bettermusictoast.compat;

import de.bettermusictoast.mixin.HudAccessor;
import net.minecraft.client.Minecraft;
//? if >=1.19
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.OptionsList;
//? if >=1.21.2
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.screens.Screen;

/**
 * Every Minecraft API that differs between the supported versions, so the rest of the mod can
 * stay version-independent. The {@code //?} comments are Stonecutter conditions: the active
 * branch is real code, the others are commented out and switched in when building that version.
 *
 * <p>26.2 moved the HUD parts of {@code Gui} into a separate {@code Hud} class and let {@code Gui}
 * manage screens and toasts; on 26.1 all of that still lives on {@code Gui} / {@code Minecraft}.
 */
public final class McCompat {
	private McCompat() {
	}

	public static Screen screen(Minecraft mc) {
		//? if >=26.2 {
		return mc.gui.screen();
		//?} else {
		/*return mc.screen;
		*///?}
	}

	public static void setScreen(Minecraft mc, Screen screen) {
		//? if >=26.2 {
		mc.gui.setScreen(screen);
		//?} else {
		/*mc.setScreen(screen);
		*///?}
	}

	//? if >=26.2 {
	public static ToastManager toastManager(Minecraft mc) {
		return mc.gui.toastManager();
	}
	//?} else if >=1.21.2 {
	/*public static ToastManager toastManager(Minecraft mc) {
		return mc.getToastManager();
	}
	*///?} else {
	/*// Before 1.21.2 the toast manager is called ToastComponent.
	public static net.minecraft.client.gui.components.toasts.ToastComponent toastManager(Minecraft mc) {
		return mc.getToasts();
	}
	*///?}

	/** F1 (hidden HUD) or the F3 debug screen. */
	public static boolean isHudHidden(Minecraft mc) {
		//? if >=26.2 {
		return mc.gui.hud.isHidden() || mc.gui.hud.getDebugOverlay().showDebugScreen();
		//?} else if >=1.20.2 {
		/*return mc.options.hideGui || mc.gui.getDebugOverlay().showDebugScreen();
		*///?} else {
		/*return mc.options.hideGui || mc.options.renderDebug;
		*///?}
	}

	public static BossHealthOverlay bossOverlay(Minecraft mc) {
		//? if >=26.2 {
		return mc.gui.hud.getBossOverlay();
		//?} else {
		/*return mc.gui.getBossOverlay();
		*///?}
	}

	/** Action bar and held-item-name state (see {@link HudAccessor}). */
	public static HudAccessor hud(Minecraft mc) {
		//? if >=26.2 {
		return (HudAccessor) mc.gui.hud;
		//?} else {
		/*return (HudAccessor) mc.gui;
		*///?}
	}

	/**
	 * Vanilla's music toast setting: a three-way choice since 1.21.11, a plain on/off switch
	 * ("Show Music Toast") in 1.21.6 – 1.21.10, and no music toast at all before that.
	 */
	//? if >=1.21.11 {
	public static OptionInstance<?> vanillaToastOption(Options options) {
		return options.musicToast();
	}
	//?} else if >=1.21.6 {
	/*public static OptionInstance<?> vanillaToastOption(Options options) {
		return options.showNowPlayingToast();
	}
	*///?}

	// The music the game wants right now; only needed for the Music Frequency option before 1.21.6.
	//? if <1.21.4 {
	/*public static net.minecraft.sounds.Music situationalMusic(Minecraft mc) {
		return mc.getSituationalMusic();
	}
	*///?} else if <1.21.6 {
	/*public static net.minecraft.sounds.Music situationalMusic(Minecraft mc) {
		return mc.getSituationalMusic().music();
	}
	*///?}

	//? if <1.19 {
	/*/^* "Caption: value", like Options.genericValueLabel in 1.19+ (see the replacement in stonecutter.gradle.kts). ^/
	public static net.minecraft.network.chat.Component genericValueLabel(net.minecraft.network.chat.Component caption,
			net.minecraft.network.chat.Component value) {
		//? if >=1.16.2 {
		return new net.minecraft.network.chat.TranslatableComponent("options.generic_value", caption, value);
		//?} else {
		/^// 1.16.1 has no "options.generic_value" text yet; its own options build "Caption: value" like this.
		return new net.minecraft.network.chat.TextComponent("").append(caption).append(": ").append(value);
		^///?}
	}

	/^* Like Identifier.toShortLanguageKey in 1.19+: the path alone for Minecraft's own ids. ^/
	public static String shortLanguageKey(net.minecraft.resources.Identifier id) {
		return id.getNamespace().equals("minecraft") ? id.getPath() : id.getNamespace() + "." + id.getPath();
	}
	*///?}

	/** Adds a full-width widget row to an options list. */
	public static void addWide(OptionsList list, AbstractWidget widget) {
		//? if >=26.2 {
		list.addBig(widget);
		//?} else if >=1.21 {
		/*list.addSmall(java.util.List.of(widget));
		*///?} else {
		/*// Before 1.21 option lists only take options (see WidgetOption).
		list.addBig(WidgetOption.of(widget));
		*///?}
	}
}
