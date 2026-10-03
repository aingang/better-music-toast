package de.bettermusictoast.hud;

import de.bettermusictoast.BetterMusicToastClient;
import de.bettermusictoast.compat.McCompat;
import de.bettermusictoast.mixin.BossHealthOverlayAccessor;
import de.bettermusictoast.mixin.HudAccessor;
import de.bettermusictoast.mixin.ToastInstanceAccessor;
import de.bettermusictoast.mixin.ToastManagerAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

/**
 * Collects the screen areas currently used by vanilla notifications, so the panel can
 * move out of the way (or hide) instead of covering them.
 */
public final class HudObstacles {
	private HudObstacles() {
	}

	/**
	 * @param includeHud whether HUD elements (effects, boss bars, action bar, item names) count;
	 *                   they are covered while a menu is open, toasts are not
	 */
	public static List<Rect> collect(Minecraft mc, int screenWidth, int screenHeight, boolean includeHud) {
		List<Rect> result = new ArrayList<>();
		Font font = mc.font;

		// Advancement / recipe / system toasts (top right, slide in from the right).
		for (Object entry : ((ToastManagerAccessor) McCompat.toastManager(mc)).bettermusictoast$getVisibleToasts()) {
			ToastInstanceAccessor instance = (ToastInstanceAccessor) entry;
			//? if >=1.21.2 {
			float portion = instance.bettermusictoast$getVisiblePortion();
			//?} else {
			/*float portion = instance.bettermusictoast$getVisibility(net.minecraft.Util.getMillis());
			*///?}
			if (portion <= 0.0f) continue;
			Toast toast = instance.bettermusictoast$getToast();
			//? if >=1.21.6 {
			int x = (int) toast.xPos(screenWidth, portion);
			int y = (int) toast.yPos(instance.bettermusictoast$getFirstSlotIndex());
			//?} else {
			/*// Same placement vanilla's ToastInstance.render uses before 1.21.6.
			int x = (int) (screenWidth - toast.width() * portion);
			int y = instance.bettermusictoast$getFirstSlotIndex() * 32;
			*///?}
			result.add(new Rect(x, y, toast.width(), toast.height()));
		}

		// Vanilla's own music toast (top left), only if the player kept it enabled.
		// Its permanent pause-menu variant sits in the same spot. It only exists since 1.21.6.
		//? if >=1.21.6 {
		if (!BetterMusicToastClient.config().hideVanillaToast
				&& (BetterMusicToastClient.tracker().isVanillaToastVisible()
						|| McCompat.screen(mc) instanceof PauseScreen pause && pause.rendersNowPlayingToast())) {
			result.add(new Rect(0, 0, 200, 30));
		}
		//?}

		if (!includeHud) {
			return result;
		}

		// Status effect icons: beneficial row at y=1, harmful row at y=27, 25px per icon from the right.
		if (mc.player != null) {
			int beneficial = 0;
			int harmful = 0;
			for (MobEffectInstance effect : mc.player.getActiveEffects()) {
				if (!effect.showIcon()) continue;
				if (effect.getEffect().value().isBeneficial()) beneficial++;
				else harmful++;
			}
			int top = mc.isDemo() ? 15 : 0;
			if (beneficial > 0) result.add(new Rect(screenWidth - 25 * beneficial, top, 25 * beneficial, 26));
			if (harmful > 0) result.add(new Rect(screenWidth - 25 * harmful, top + 26, 25 * harmful, 26));
		}

		// Boss bars (top center): first bar at y=12, name 9px above, 19px per bar, capped at a third of the screen.
		int bosses = ((BossHealthOverlayAccessor) McCompat.bossOverlay(mc)).bettermusictoast$getEvents().size();
		if (bosses > 0) {
			int y = 12;
			int bottom = 0;
			for (int i = 0; i < bosses; i++) {
				bottom = y + 5;
				y += 10 + font.lineHeight;
				if (y >= screenHeight / 3) break;
			}
			result.add(new Rect(screenWidth / 2 - 92, 2, 184, bottom - 1));
		}

		HudAccessor hudAccess = McCompat.hud(mc);

		// Action bar / jukebox "Now Playing" message, drawn centered at screenHeight - 68.
		Component overlay = hudAccess.bettermusictoast$getOverlayMessage();
		if (overlay != null && hudAccess.bettermusictoast$getOverlayMessageTime() > 0) {
			int width = font.width(overlay);
			result.add(new Rect(screenWidth / 2 - width / 2 - 4, screenHeight - 68 - 6, width + 8, 14));
		}

		// Name of the newly selected hotbar item.
		ItemStack highlight = hudAccess.bettermusictoast$getLastToolHighlight();
		if (hudAccess.bettermusictoast$getToolHighlightTimer() > 0 && highlight != null && !highlight.isEmpty()) {
			int width = font.width(highlight.getHoverName());
			int y = screenHeight - 59;
			if (mc.gameMode != null && !mc.gameMode.canHurtPlayer()) {
				y += 14;
			}
			result.add(new Rect(screenWidth / 2 - width / 2 - 4, y - 2, width + 8, 12));
		}

		return result;
	}
}
