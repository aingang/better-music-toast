package de.bettermusictoast.compat;

import de.bettermusictoast.mixin.HudAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.OptionsList;
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

	public static ToastManager toastManager(Minecraft mc) {
		//? if >=26.2 {
		return mc.gui.toastManager();
		//?} else {
		/*return mc.getToastManager();
		*///?}
	}

	/** F1 (hidden HUD) or the F3 debug screen. */
	public static boolean isHudHidden(Minecraft mc) {
		//? if >=26.2 {
		return mc.gui.hud.isHidden() || mc.gui.hud.getDebugOverlay().showDebugScreen();
		//?} else {
		/*return mc.options.hideGui || mc.gui.getDebugOverlay().showDebugScreen();
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

	/** Adds a full-width widget row to an options list. */
	public static void addWide(OptionsList list, AbstractWidget widget) {
		//? if >=26.2 {
		list.addBig(widget);
		//?} else {
		/*list.addSmall(java.util.List.of(widget));
		*///?}
	}
}
