package de.bettermusictoast.hud;

import de.bettermusictoast.mixin.GuiAchievementAccessor;
import de.bettermusictoast.mixin.GuiIngameAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.item.ItemStack;

/**
 * Collects the screen areas currently used by vanilla notifications, so the panel can move out
 * of the way (or hide) instead of covering them. Minecraft 1.8.9 has no toasts and no effect icons
 * in the HUD; its notifications are the achievement pop-up, the boss bar, the record message and
 * the held item's name.
 */
final class HudObstacles {
	private HudObstacles() {
	}

	/**
	 * @param includeHud whether HUD elements (boss bar, record message, item name) count; they are
	 *                   covered while a menu is open, the achievement pop-up is not
	 */
	static List<Rect> collect(Minecraft mc, int screenWidth, int screenHeight, boolean includeHud) {
		List<Rect> result = new ArrayList<Rect>();
		FontRenderer font = mc.fontRendererObj;

		// "Achievement get!" (top right, slides down from the top). Same timing and placement as
		// GuiAchievement.updateAchievementWindow.
		GuiAchievementAccessor achievement = (GuiAchievementAccessor) mc.guiAchievement;
		long shownAt = achievement.bettermusictoast$getNotificationTime();
		if (achievement.bettermusictoast$getAchievement() != null && shownAt != 0L && mc.thePlayer != null) {
			double progress = (Minecraft.getSystemTime() - shownAt) / 3000.0;
			boolean visible = achievement.bettermusictoast$isPermanent() || progress >= 0.0 && progress <= 1.0;
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

		GuiIngameAccessor hud = (GuiIngameAccessor) mc.ingameGUI;

		// Record "Now playing" message, drawn centered at screenHeight - 68.
		String record = hud.bettermusictoast$getRecordPlaying();
		if (record != null && !record.isEmpty() && hud.bettermusictoast$getRecordPlayingUpFor() > 0) {
			int width = font.getStringWidth(record);
			result.add(new Rect(screenWidth / 2 - width / 2 - 4, screenHeight - 68 - 6, width + 8, 14));
		}

		// Name of the newly selected hotbar item.
		ItemStack highlight = hud.bettermusictoast$getHighlightingItemStack();
		if (hud.bettermusictoast$getRemainingHighlightTicks() > 0 && highlight != null && mc.gameSettings.heldItemTooltips) {
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
