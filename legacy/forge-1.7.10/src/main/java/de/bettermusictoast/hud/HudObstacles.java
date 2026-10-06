package de.bettermusictoast.hud;

import de.bettermusictoast.compat.Fields;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.item.ItemStack;

/**
 * Collects the screen areas currently used by vanilla notifications, so the panel can move out
 * of the way (or hide) instead of covering them. Minecraft 1.7.10 has no toasts and no effect icons
 * in the HUD; its notifications are the achievement pop-up, the boss bar, the record message and
 * the held item's name.
 */
final class HudObstacles {
	private static final Fields<Object> ACHIEVEMENT = new Fields<Object>(GuiAchievement.class, "field_146266_k");
	private static final Fields<Long> ACHIEVEMENT_TIME = new Fields<Long>(GuiAchievement.class, "field_146263_l");
	private static final Fields<Boolean> ACHIEVEMENT_PERMANENT = new Fields<Boolean>(GuiAchievement.class, "field_146262_n");
	private static final Fields<String> RECORD = new Fields<String>(GuiIngame.class, "recordPlaying", "field_73838_g");
	private static final Fields<Integer> RECORD_TIME = new Fields<Integer>(GuiIngame.class, "recordPlayingUpFor", "field_73845_h");
	private static final Fields<Integer> HIGHLIGHT_TICKS = new Fields<Integer>(GuiIngame.class, "remainingHighlightTicks", "field_92017_k");
	private static final Fields<ItemStack> HIGHLIGHT_ITEM = new Fields<ItemStack>(GuiIngame.class, "highlightingItemStack", "field_92016_l");

	private HudObstacles() {
	}

	/**
	 * @param includeHud whether HUD elements (boss bar, record message, item name) count; they are
	 *                   covered while a menu is open, the achievement pop-up is not
	 */
	static List<Rect> collect(Minecraft mc, int screenWidth, int screenHeight, boolean includeHud) {
		List<Rect> result = new ArrayList<Rect>();
		FontRenderer font = mc.fontRenderer;

		// "Achievement get!" (top right, slides down from the top). Same timing and placement as
		// GuiAchievement draws it.
		GuiAchievement achievement = mc.guiAchievement;
		Long shownAt = ACHIEVEMENT_TIME.get(achievement);
		if (ACHIEVEMENT.get(achievement) != null && shownAt != null && shownAt != 0L && mc.thePlayer != null) {
			double progress = (Minecraft.getSystemTime() - shownAt) / 3000.0;
			boolean visible = Boolean.TRUE.equals(ACHIEVEMENT_PERMANENT.get(achievement)) || progress >= 0.0 && progress <= 1.0;
			// The whole spot counts from the first frame of the slide-in to the last of the slide-out, so
			// the panel is already out of the way when the pop-up arrives instead of being pushed by it.
			if (visible) {
				result.add(new Rect(screenWidth - 160, 0, 160, 32));
			}
		}

		if (!includeHud) {
			return result;
		}

		// Boss bar (top center): bar at y=12, name 10px above it.
		if (BossStatus.bossName != null && BossStatus.statusBarTime > 0) {
			result.add(new Rect(screenWidth / 2 - 92, 2, 184, 16));
		}

		// Record "Now playing" message, drawn centered at screenHeight - 48 (1.8 moved it up to - 68).
		String record = RECORD.get(mc.ingameGUI);
		Integer recordTime = RECORD_TIME.get(mc.ingameGUI);
		if (record != null && !record.isEmpty() && recordTime != null && recordTime > 0) {
			int width = font.getStringWidth(record);
			result.add(new Rect(screenWidth / 2 - width / 2 - 4, screenHeight - 48 - 6, width + 8, 14));
		}

		// Name of the newly selected hotbar item.
		ItemStack highlight = HIGHLIGHT_ITEM.get(mc.ingameGUI);
		Integer highlightTicks = HIGHLIGHT_TICKS.get(mc.ingameGUI);
		if (highlightTicks != null && highlightTicks > 0 && highlight != null && mc.gameSettings.heldItemTooltips) {
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
