package de.bettermusictoast.hud;

import de.bettermusictoast.compat.Fields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
//#if MC>=11200
import net.minecraft.client.gui.toasts.GuiToast;
//#else
//$$ import net.minecraft.client.gui.achievement.GuiAchievement;
//#endif

/**
 * Collects the screen areas currently used by vanilla notifications, so the panel can move out
 * of the way (or hide) instead of covering them: toasts (1.12) or the achievement pop-up (before
 * 1.12), effect icons, boss bars, the action bar / record message and the held item's name.
 */
final class HudObstacles {
	//#if MC>=11200
	private static final Fields<Object[]> TOASTS = new Fields<Object[]>(GuiToast.class, "visible", "field_191791_g");
	//#else
	//$$ private static final Fields<Object> ACHIEVEMENT = new Fields<Object>(GuiAchievement.class, "theAchievement", "achievement", "field_146266_k");
	//$$ private static final Fields<Long> ACHIEVEMENT_TIME = new Fields<Long>(GuiAchievement.class, "notificationTime", "field_146263_l");
	//$$ private static final Fields<Boolean> ACHIEVEMENT_PERMANENT = new Fields<Boolean>(GuiAchievement.class, "permanentNotification", "field_146262_n");
	//#endif
	private static final Fields<Map<?, ?>> BOSSES = new Fields<Map<?, ?>>(GuiBossOverlay.class, "mapBossInfos", "field_184060_g");
	private static final Fields<String> OVERLAY_MESSAGE = new Fields<String>(GuiIngame.class, "overlayMessage", "recordPlaying", "field_73838_g");
	private static final Fields<Integer> OVERLAY_MESSAGE_TIME = new Fields<Integer>(GuiIngame.class, "overlayMessageTime", "recordPlayingUpFor", "field_73845_h");
	private static final Fields<Integer> HIGHLIGHT_TICKS = new Fields<Integer>(GuiIngame.class, "remainingHighlightTicks", "field_92017_k");
	private static final Fields<ItemStack> HIGHLIGHT_ITEM = new Fields<ItemStack>(GuiIngame.class, "highlightingItemStack", "field_92016_l");

	private HudObstacles() {
	}

	/**
	 * @param includeHud whether HUD elements (effects, boss bars, action bar, item names) count; they
	 *                   are covered while a menu is open, toasts and the achievement pop-up are not
	 */
	static List<Rect> collect(Minecraft mc, int screenWidth, int screenHeight, boolean includeHud) {
		List<Rect> result = new ArrayList<Rect>();
		//#if MC>=11100
		FontRenderer font = mc.fontRenderer;
		//#else
		//$$ FontRenderer font = mc.fontRendererObj;
		//#endif

		//#if MC>=11200
		// Advancement / recipe / tutorial toasts (top right, 160x32 each, one slot per row). The whole
		// slot counts from the first frame of the slide-in to the last of the slide-out, so the panel
		// is already out of the way when the toast arrives instead of being pushed while it slides.
		Object[] toasts = TOASTS.get(mc.getToastGui());
		if (toasts != null) {
			for (int slot = 0; slot < toasts.length; slot++) {
				if (toasts[slot] != null) {
					result.add(new Rect(screenWidth - 160, slot * 32, 160, 32));
				}
			}
		}
		//#else
		//$$ // "Achievement get!" (top right, slides down from the top). Same timing and placement as
		//$$ // GuiAchievement.updateAchievementWindow; the whole spot counts while it is on screen.
		//$$ GuiAchievement achievement = mc.guiAchievement;
		//$$ Long shownAt = ACHIEVEMENT_TIME.get(achievement);
		//#if MC>=11002
		//$$ boolean inWorld = mc.player != null;
		//#else
		//$$ boolean inWorld = mc.thePlayer != null;
		//#endif
		//$$ if (ACHIEVEMENT.get(achievement) != null && shownAt != null && shownAt != 0L && inWorld) {
		//$$ 	double progress = (Minecraft.getSystemTime() - shownAt) / 3000.0;
		//$$ 	if (Boolean.TRUE.equals(ACHIEVEMENT_PERMANENT.get(achievement)) || progress >= 0.0 && progress <= 1.0) {
		//$$ 		result.add(new Rect(screenWidth - 160, 0, 160, 32));
		//$$ 	}
		//$$ }
		//#endif

		if (!includeHud) {
			return result;
		}

		// Status effect icons: beneficial row at y=1, harmful row at y=27, 25px per icon from the right.
		//#if MC>=11002
		if (mc.player != null) {
			java.util.Collection<PotionEffect> effects = mc.player.getActivePotionEffects();
		//#else
		//$$ if (mc.thePlayer != null) {
		//$$ 	java.util.Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
		//#endif
			int beneficial = 0;
			int harmful = 0;
			for (PotionEffect effect : effects) {
				if (!effect.getPotion().shouldRenderHUD(effect) || !effect.doesShowParticles()) continue;
				if (effect.getPotion().isBeneficial()) beneficial++;
				else harmful++;
			}
			int top = mc.isDemo() ? 15 : 0;
			if (beneficial > 0) result.add(new Rect(screenWidth - 25 * beneficial, top, 25 * beneficial, 26));
			if (harmful > 0) result.add(new Rect(screenWidth - 25 * harmful, top + 26, 25 * harmful, 26));
		}

		// Boss bars (top center): first bar at y=12, name 9px above, 19px per bar, capped at a third of the screen.
		Map<?, ?> bossMap = BOSSES.get(mc.ingameGUI.getBossOverlay());
		int bosses = bossMap == null ? 0 : bossMap.size();
		if (bosses > 0) {
			int y = 12;
			int bottom = 0;
			for (int i = 0; i < bosses; i++) {
				bottom = y + 5;
				y += 10 + font.FONT_HEIGHT;
				if (y >= screenHeight / 3) break;
			}
			result.add(new Rect(screenWidth / 2 - 92, 2, 184, bottom - 1));
		}

		// Action bar / record "Now playing" message, drawn centered at screenHeight - 68.
		String overlay = OVERLAY_MESSAGE.get(mc.ingameGUI);
		Integer overlayTime = OVERLAY_MESSAGE_TIME.get(mc.ingameGUI);
		if (overlay != null && !overlay.isEmpty() && overlayTime != null && overlayTime > 0) {
			int width = font.getStringWidth(overlay);
			result.add(new Rect(screenWidth / 2 - width / 2 - 4, screenHeight - 68 - 6, width + 8, 14));
		}

		// Name of the newly selected hotbar item.
		ItemStack highlight = HIGHLIGHT_ITEM.get(mc.ingameGUI);
		Integer highlightTicks = HIGHLIGHT_TICKS.get(mc.ingameGUI);
		//#if MC>=11100
		boolean hasItem = highlight != null && !highlight.isEmpty();
		//#else
		//$$ boolean hasItem = highlight != null;
		//#endif
		if (highlightTicks != null && highlightTicks > 0 && hasItem && mc.gameSettings.heldItemTooltips) {
			int width = font.getStringWidth(highlight.getDisplayName());
			int y = screenHeight - 59;
			if (mc.playerController != null && !mc.playerController.shouldDrawHUD()) {
				y += 14;
			}
			result.add(new Rect(screenWidth / 2 - width / 2 - 4, y - 2, width + 8, 12));
		}

		return result;
	}
}
