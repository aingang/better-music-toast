package de.bettermusictoast.hud;

import de.bettermusictoast.BetterMusicToast;
import de.bettermusictoast.config.ColorTheme;
import de.bettermusictoast.config.ModConfig;
import de.bettermusictoast.config.ModConfig.Position;
import de.bettermusictoast.track.NowPlayingTracker;
import de.bettermusictoast.track.TrackInfo;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;

/**
 * Draws the "now playing" panel: a compact, advancement-toast-like box with a light background
 * and a darker pixel border in the selected {@link ColorTheme}. Same sizes, colours and animations
 * as in the newer Minecraft versions.
 */
public final class NowPlayingHud {
	// Minecraft before 1.21.6 has no music notes icon of its own, so the mod ships the one from newer versions.
	private static final ResourceLocation MUSIC_NOTES = new ResourceLocation("bettermusictoast", "textures/gui/sprites/music_notes.png");

	private static final int SCREEN_MARGIN = 4;
	private static final int OBSTACLE_GAP = 2;
	private static final int ICON_X = 5;
	private static final int TEXT_X = 28;
	private static final int PADDING_RIGHT = 7;
	private static final int MAX_TEXT_WIDTH = 150;
	private static final int HEIGHT_TWO_LINES = 26;
	private static final int HEIGHT_ONE_LINE = 22;

	private static final float FADE_MS = 280.0f;
	private static final float MOVE_MS = 90.0f;
	/** How long the way back has to stay clear before the panel returns. */
	private static final long HOLD_MS = 700;

	private float anim;
	private float currentY = Float.NaN;
	private Position lastPosition;
	private long lastFrameNanos;
	private int heldY;
	private long holdUntil;
	private long blockedUntil;

	/** In-game HUD pass. With "show in menus" the screen pass takes over while a menu is open. */
	public void renderHud() {
		if (drawsOverScreen() && Minecraft.getMinecraft().currentScreen != null) {
			return;
		}
		render(false);
	}

	/** Drawn on top of any open menu (pause menu, inventory, title screen, ...) when enabled. */
	public void renderOverScreen() {
		if (drawsOverScreen()) {
			render(true);
		}
	}

	/** "Show in menus", or the short preview while the settings screen is open. */
	private static boolean drawsOverScreen() {
		return BetterMusicToast.config().showInMenus || BetterMusicToast.tracker().isSettingsPreview();
	}

	private void render(boolean overScreen) {
		long nowNanos = System.nanoTime();
		long dtMs = lastFrameNanos == 0 ? 0 : Math.min(100, (nowNanos - lastFrameNanos) / 1_000_000L);
		lastFrameNanos = nowNanos;

		Minecraft mc = Minecraft.getMinecraft();
		NowPlayingTracker tracker = BetterMusicToast.tracker();
		ModConfig config = BetterMusicToast.config();
		tracker.checkEnded();
		TrackInfo track = tracker.track();
		//#if MC>=11002
		if (track == null || (!overScreen && mc.player == null)) {
		//#else
		//$$ if (track == null || (!overScreen && mc.thePlayer == null)) {
		//#endif
			anim = 0;
			currentY = Float.NaN;
			return;
		}

		//#if MC>=11100
		FontRenderer font = mc.fontRenderer;
		//#else
		//$$ FontRenderer font = mc.fontRendererObj;
		//#endif
		String title = fit(font, track.title());
		String artist = config.showArtist && track.artist() != null ? fit(font, track.artist()) : null;
		int textWidth = Math.max(font.getStringWidth(title), artist == null ? 0 : font.getStringWidth(artist));
		int width = TEXT_X + textWidth + PADDING_RIGHT;
		int height = artist == null ? HEIGHT_ONE_LINE : HEIGHT_TWO_LINES;

		ScaledResolution resolution = new ScaledResolution(mc);
		int guiScale = resolution.getScaleFactor();
		float scale = config.scale(guiScale);
		int scaledWidth = (int) Math.ceil(width * scale);
		int scaledHeight = (int) Math.ceil(height * scale);
		int screenWidth = resolution.getScaledWidth();
		int screenHeight = resolution.getScaledHeight();
		Position position = config.position;

		int x;
		switch (position) {
			case TOP_LEFT:
				x = SCREEN_MARGIN;
				break;
			case TOP_RIGHT:
				x = screenWidth - scaledWidth - SCREEN_MARGIN;
				break;
			default:
				x = (screenWidth - scaledWidth) / 2;
				break;
		}
		int baseY = position == Position.ABOVE_HOTBAR
				? hotbarPanelBottom(mc, screenHeight) - scaledHeight
				: SCREEN_MARGIN;

		// Over a menu the HUD is covered, so only toasts / the achievement pop-up can get in the way.
		List<Rect> obstacles = HudObstacles.collect(mc, screenWidth, screenHeight, !overScreen);
		long nowMs = nowNanos / 1_000_000L;
		int targetY;
		boolean blocked;
		if (config.avoidMode == ModConfig.AvoidMode.MOVE) {
			int wantedY = avoid(x, baseY, scaledWidth, scaledHeight, obstacles, position == Position.ABOVE_HOTBAR);
			// No room left beside the notifications: step aside completely rather than cover them.
			blocked = wantedY < 0 || wantedY + scaledHeight > screenHeight;
			wantedY = Math.max(0, Math.min(wantedY, Math.max(0, screenHeight - scaledHeight)));
			targetY = holdPosition(wantedY, baseY, x, scaledWidth, scaledHeight, obstacles, nowMs, position);
		} else {
			blocked = intersectsAny(new Rect(x, baseY, scaledWidth, scaledHeight), obstacles);
			targetY = baseY;
		}
		// Notifications in a row leave a gap of a few frames between them; stay out of the way
		// through it instead of popping back in just before the next one arrives.
		if (blocked) {
			blockedUntil = nowMs + HOLD_MS;
		} else if (nowMs < blockedUntil) {
			blocked = true;
		}

		// F1 (hidden HUD) or the F3 debug screen.
		boolean hudHidden = !overScreen && (mc.gameSettings.hideGUI || mc.gameSettings.showDebugInfo);
		boolean show = tracker.wantsVisible() && !blocked && !hudHidden;

		// Only count time the player can actually see the panel, so a blocked or
		// covered display does not silently run out.
		if (show && anim >= 1.0f && (overScreen || mc.currentScreen == null)) {
			tracker.addShownTime(dtMs);
		}

		float step = dtMs / FADE_MS;
		anim = show ? Math.min(1.0f, anim + step) : Math.max(0.0f, anim - step);
		if (anim <= 0.0f) {
			currentY = Float.NaN;
			tracker.clearIfDone();
			return;
		}

		if (Float.isNaN(currentY) || lastPosition != position) {
			currentY = targetY;
		} else {
			currentY += (targetY - currentY) * Math.min(1.0f, dtMs / MOVE_MS);
		}
		lastPosition = position;

		if (hudHidden) {
			return;
		}

		float eased = 1.0f - (float) Math.pow(1.0f - anim, 3);
		float dx = 0;
		float dy = 0;
		float alpha = 1.0f;
		switch (position) {
			case TOP_LEFT:
				dx = -(scaledWidth + SCREEN_MARGIN) * (1.0f - eased);
				break;
			case TOP_RIGHT:
				dx = (scaledWidth + SCREEN_MARGIN) * (1.0f - eased);
				break;
			case TOP_CENTER:
				alpha = eased;
				dy = -6 * (1.0f - eased);
				break;
			case ABOVE_HOTBAR:
				alpha = eased;
				dy = 6 * (1.0f - eased);
				break;
		}

		GlStateManager.pushMatrix();
		// Lift the box to the height the newer versions use for toasts, so menu items and tooltips
		// never show through it, and snap to whole screen pixels so the font never lands between pixels.
		GlStateManager.translate(Math.round((x + dx) * guiScale) / (float) guiScale,
				Math.round((currentY + dy) * guiScale) / (float) guiScale, 800.0f);
		GlStateManager.scale(scale, scale, 1.0f);
		// Screens may leave item lighting switched on.
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		drawPanel(mc, font, track, title, artist, width, height, alpha, config.colorTheme);
		GlStateManager.popMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	/** Above the hearts / hunger bar in survival, just above the hotbar item names in creative. */
	private static int hotbarPanelBottom(Minecraft mc, int screenHeight) {
		boolean survivalBars = mc.playerController != null && mc.playerController.shouldDrawHUD();
		return screenHeight - (survivalBars ? 62 : 48);
	}

	/**
	 * Moving away from the usual spot happens at once; moving back only after the way has been clear
	 * for {@link #HOLD_MS}, so notifications in a row keep the panel below them the whole time.
	 */
	private int holdPosition(int wantedY, int baseY, int x, int width, int height, List<Rect> obstacles,
			long nowMs, Position position) {
		boolean fresh = Float.isNaN(currentY) || lastPosition != position;
		if (fresh || Math.abs(wantedY - baseY) >= Math.abs(heldY - baseY)
				|| intersectsAny(new Rect(x, heldY, width, height), obstacles)) {
			heldY = wantedY;
			holdUntil = nowMs + HOLD_MS;
		} else if (nowMs >= holdUntil) {
			heldY = wantedY;
		}
		return heldY;
	}

	private static boolean intersectsAny(Rect panel, List<Rect> obstacles) {
		for (Rect obstacle : obstacles) {
			if (panel.intersects(obstacle)) {
				return true;
			}
		}
		return false;
	}

	private static int avoid(int x, int y, int width, int height, List<Rect> obstacles, boolean upward) {
		for (int i = 0; i < 16; i++) {
			Rect panel = new Rect(x, y, width, height);
			Rect hit = null;
			for (Rect obstacle : obstacles) {
				if (obstacle.intersects(panel)) {
					hit = obstacle;
					break;
				}
			}
			if (hit == null) {
				break;
			}
			y = upward ? hit.y - OBSTACLE_GAP - height : hit.bottom() + OBSTACLE_GAP;
		}
		return y;
	}

	private static void drawPanel(Minecraft mc, FontRenderer font, TrackInfo track, String title, String artist,
			int w, int h, float alpha, ColorTheme theme) {
		drawFrame(w, h, theme, theme.border, alpha);

		// Divider between icon and text.
		Gui.drawRect(TEXT_X - 4, 5, TEXT_X - 3, h - 5, argb(theme.shade, alpha));

		int iconY = (h - 16) / 2;
		if (track.icon() != null && alpha > 0.6f) {
			// drawRect leaves its colour set, which would tint the item.
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			RenderHelper.enableGUIStandardItemLighting();
			mc.getRenderItem().renderItemAndEffectIntoGUI(track.icon(), ICON_X, iconY);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
		} else {
			// A plain texture: its animation (8 frames, 2 ticks each, stacked vertically) is played
			// here, and it is tinted and faded through the colour, like in the newer versions.
			int tint = argb(theme.icon, alpha);
			int frame = BetterMusicToast.config().animateIcon ? (int) (Minecraft.getSystemTime() / 100L % 8L) : 0;
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.color(((tint >> 16) & 0xFF) / 255.0f, ((tint >> 8) & 0xFF) / 255.0f, (tint & 0xFF) / 255.0f,
					((tint >>> 24) & 0xFF) / 255.0f);
			mc.getTextureManager().bindTexture(MUSIC_NOTES);
			Gui.drawModalRectWithCustomSizedTexture(ICON_X, iconY, 0.0f, frame * 16.0f, 16, 16, 16, 128);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		}

		if (alpha < 0.05f) {
			return;
		}
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		if (artist == null) {
			font.drawString(title, TEXT_X, (h - 8) / 2, argb(theme.title, alpha), false);
		} else {
			font.drawString(title, TEXT_X, 5, argb(theme.title, alpha), false);
			font.drawString(artist, TEXT_X, 15, argb(theme.artist, alpha), false);
		}
		GlStateManager.disableBlend();
	}

	/** Background, bevel and pixel-rounded frame at (0, 0), shared with the themed buttons in the settings. */
	public static void drawFrame(int w, int h, ColorTheme theme, int borderColor, float alpha) {
		int border = argb(borderColor, alpha);
		int highlight = argb(theme.highlight, alpha);
		int shade = argb(theme.shade, alpha);

		// Body.
		Gui.drawRect(1, 1, w - 1, h - 1, argb(theme.fill, alpha));

		// Inner bevel, like vanilla buttons and toasts.
		Gui.drawRect(2, 1, w - 2, 2, highlight);
		Gui.drawRect(1, 2, 2, h - 2, highlight);
		Gui.drawRect(2, h - 2, w - 2, h - 1, shade);
		Gui.drawRect(w - 2, 2, w - 1, h - 2, shade);

		// Frame with cut corners for the pixel-rounded look.
		Gui.drawRect(1, 0, w - 1, 1, border);
		Gui.drawRect(1, h - 1, w - 1, h, border);
		Gui.drawRect(0, 1, 1, h - 1, border);
		Gui.drawRect(w - 1, 1, w, h - 1, border);
		Gui.drawRect(1, 1, 2, 2, border);
		Gui.drawRect(w - 2, 1, w - 1, 2, border);
		Gui.drawRect(1, h - 2, 2, h - 1, border);
		Gui.drawRect(w - 2, h - 2, w - 1, h - 1, border);
	}

	private static String fit(FontRenderer font, String text) {
		if (font.getStringWidth(text) <= MAX_TEXT_WIDTH) {
			return text;
		}
		String ellipsis = "...";
		return font.trimStringToWidth(text, MAX_TEXT_WIDTH - font.getStringWidth(ellipsis)).trim() + ellipsis;
	}

	public static int argb(int rgb, float alpha) {
		int a = Math.round(Math.max(0.0f, Math.min(alpha, 1.0f)) * 255.0f);
		return (a << 24) | (rgb & 0xFFFFFF);
	}
}
