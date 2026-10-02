package de.bettermusictoast.hud;

import de.bettermusictoast.BetterMusicToastClient;
import de.bettermusictoast.config.ModConfig;
import de.bettermusictoast.config.ModConfig.Position;
import de.bettermusictoast.track.NowPlayingTracker;
import de.bettermusictoast.track.TrackInfo;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

/**
 * Draws the "now playing" panel: a compact, advancement-toast-like box with a beige
 * background and a brown pixel border.
 */
public final class NowPlayingHud implements HudElement {
	private static final Identifier MUSIC_NOTES_SPRITE = Identifier.withDefaultNamespace("icon/music_notes");

	private static final int COLOR_BORDER = 0x4A2E16;
	private static final int COLOR_FILL = 0xE9D8B0;
	private static final int COLOR_HIGHLIGHT = 0xF8EED4;
	private static final int COLOR_SHADE = 0xC8AE80;
	private static final int COLOR_TITLE = 0x3B2410;
	private static final int COLOR_ARTIST = 0x7A5432;
	private static final int COLOR_ICON = 0x6B4626;

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

	private float anim;
	private float currentY = Float.NaN;
	private Position lastPosition;
	private long lastFrameNanos;

	/** In-game HUD pass. With "show in menus" the screen pass takes over while a menu is open. */
	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (BetterMusicToastClient.config().showInMenus && Minecraft.getInstance().gui.screen() != null) {
			return;
		}
		render(graphics, false);
	}

	/** Drawn on top of any open menu (pause menu, inventory, title screen, ...) when enabled. */
	public void extractOverScreen(GuiGraphicsExtractor graphics) {
		if (BetterMusicToastClient.config().showInMenus) {
			render(graphics, true);
		}
	}

	private void render(GuiGraphicsExtractor graphics, boolean overScreen) {
		long nowNanos = System.nanoTime();
		long dtMs = lastFrameNanos == 0 ? 0 : Math.min(100, (nowNanos - lastFrameNanos) / 1_000_000L);
		lastFrameNanos = nowNanos;

		Minecraft mc = Minecraft.getInstance();
		NowPlayingTracker tracker = BetterMusicToastClient.tracker();
		ModConfig config = BetterMusicToastClient.config();
		TrackInfo track = tracker.track();
		if (track == null || (!overScreen && mc.player == null)) {
			anim = 0;
			currentY = Float.NaN;
			return;
		}

		Font font = mc.font;
		String title = fit(font, track.title());
		String artist = config.showArtist && track.artist() != null ? fit(font, track.artist()) : null;
		int textWidth = Math.max(font.width(title), artist == null ? 0 : font.width(artist));
		int width = TEXT_X + textWidth + PADDING_RIGHT;
		int height = artist == null ? HEIGHT_ONE_LINE : HEIGHT_TWO_LINES;

		float scale = config.size.scale;
		int scaledWidth = (int) Math.ceil(width * scale);
		int scaledHeight = (int) Math.ceil(height * scale);
		int screenWidth = graphics.guiWidth();
		int screenHeight = graphics.guiHeight();
		Position position = config.position;

		int x = switch (position) {
			case TOP_LEFT -> SCREEN_MARGIN;
			case TOP_RIGHT -> screenWidth - scaledWidth - SCREEN_MARGIN;
			case TOP_CENTER, ABOVE_HOTBAR -> (screenWidth - scaledWidth) / 2;
		};
		int baseY = position == Position.ABOVE_HOTBAR
				? hotbarPanelBottom(mc, screenHeight) - scaledHeight
				: SCREEN_MARGIN;

		// Over a menu the HUD is covered, so only toasts can get in the way.
		List<Rect> obstacles = HudObstacles.collect(mc, screenWidth, screenHeight, !overScreen);
		int targetY = baseY;
		boolean blocked = false;
		if (config.avoidMode == ModConfig.AvoidMode.MOVE) {
			targetY = avoid(x, baseY, scaledWidth, scaledHeight, obstacles, position == Position.ABOVE_HOTBAR);
			targetY = Math.clamp(targetY, 0, Math.max(0, screenHeight - scaledHeight));
		} else {
			Rect panel = new Rect(x, baseY, scaledWidth, scaledHeight);
			blocked = obstacles.stream().anyMatch(panel::intersects);
		}

		boolean hudHidden = !overScreen && (mc.gui.hud.isHidden() || mc.gui.hud.getDebugOverlay().showDebugScreen());
		boolean show = tracker.wantsVisible() && !blocked && !hudHidden;

		// Only count time the player can actually see the panel, so a blocked or
		// covered display does not silently run out.
		if (show && anim >= 1.0f && (overScreen || mc.gui.screen() == null)) {
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
			case TOP_LEFT -> dx = -(scaledWidth + SCREEN_MARGIN) * (1.0f - eased);
			case TOP_RIGHT -> dx = (scaledWidth + SCREEN_MARGIN) * (1.0f - eased);
			case TOP_CENTER -> {
				alpha = eased;
				dy = -6 * (1.0f - eased);
			}
			case ABOVE_HOTBAR -> {
				alpha = eased;
				dy = 6 * (1.0f - eased);
			}
		}

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(x + dx, currentY + dy);
		pose.scale(scale, scale);
		drawPanel(graphics, font, track, title, artist, width, height, alpha);
		pose.popMatrix();
	}

	/** Above the hearts / hunger bar in survival, just above the hotbar item names in creative. */
	private static int hotbarPanelBottom(Minecraft mc, int screenHeight) {
		boolean survivalBars = mc.gameMode != null && mc.gameMode.canHurtPlayer();
		return screenHeight - (survivalBars ? 62 : 48);
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
			y = upward ? hit.y() - OBSTACLE_GAP - height : hit.bottom() + OBSTACLE_GAP;
		}
		return y;
	}

	static void drawPanel(GuiGraphicsExtractor g, Font font, TrackInfo track, String title, String artist,
			int w, int h, float alpha) {
		int border = argb(COLOR_BORDER, alpha);
		int highlight = argb(COLOR_HIGHLIGHT, alpha);
		int shade = argb(COLOR_SHADE, alpha);

		// Body.
		g.fill(1, 1, w - 1, h - 1, argb(COLOR_FILL, alpha));

		// Inner bevel, like vanilla buttons and toasts.
		g.fill(2, 1, w - 2, 2, highlight);
		g.fill(1, 2, 2, h - 2, highlight);
		g.fill(2, h - 2, w - 2, h - 1, shade);
		g.fill(w - 2, 2, w - 1, h - 2, shade);

		// Brown frame with cut corners for the pixel-rounded look.
		g.fill(1, 0, w - 1, 1, border);
		g.fill(1, h - 1, w - 1, h, border);
		g.fill(0, 1, 1, h - 1, border);
		g.fill(w - 1, 1, w, h - 1, border);
		g.fill(1, 1, 2, 2, border);
		g.fill(w - 2, 1, w - 1, 2, border);
		g.fill(1, h - 2, 2, h - 1, border);
		g.fill(w - 2, h - 2, w - 1, h - 1, border);

		// Divider between icon and text.
		g.fill(TEXT_X - 4, 5, TEXT_X - 3, h - 5, shade);

		int iconY = (h - 16) / 2;
		if (!track.icon().isEmpty() && alpha > 0.6f) {
			g.item(track.icon(), ICON_X, iconY);
		} else {
			g.blitSprite(RenderPipelines.GUI_TEXTURED, MUSIC_NOTES_SPRITE, ICON_X, iconY, 16, 16, argb(COLOR_ICON, alpha));
		}

		if (alpha < 0.05f) {
			return;
		}
		if (artist == null) {
			g.text(font, title, TEXT_X, (h - 8) / 2, argb(COLOR_TITLE, alpha), false);
		} else {
			g.text(font, title, TEXT_X, 5, argb(COLOR_TITLE, alpha), false);
			g.text(font, artist, TEXT_X, 15, argb(COLOR_ARTIST, alpha), false);
		}
	}

	private static String fit(Font font, String text) {
		if (font.width(text) <= MAX_TEXT_WIDTH) {
			return text;
		}
		String ellipsis = "...";
		return font.plainSubstrByWidth(text, MAX_TEXT_WIDTH - font.width(ellipsis)).trim() + ellipsis;
	}

	private static int argb(int rgb, float alpha) {
		int a = Math.round(Math.clamp(alpha, 0.0f, 1.0f) * 255.0f);
		return (a << 24) | (rgb & 0xFFFFFF);
	}
}
