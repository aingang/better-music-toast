package de.bettermusictoast.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("bettermusictoast");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("bettermusictoast.json");
	/** Settings file from before the mod was renamed (Now Playing Toast). */
	private static final Path LEGACY_PATH = FabricLoader.getInstance().getConfigDir().resolve("nowplayingtoast.json");

	public enum Position {
		TOP_LEFT, TOP_CENTER, TOP_RIGHT, ABOVE_HOTBAR;

		public String translationKey() {
			return "bettermusictoast.position." + name().toLowerCase();
		}
	}

	public enum DisplayMode {
		WHOLE_SONG, TIMED;

		public String translationKey() {
			return "bettermusictoast.displayMode." + name().toLowerCase();
		}
	}

	public enum AvoidMode {
		MOVE, HIDE;

		public String translationKey() {
			return "bettermusictoast.avoidMode." + name().toLowerCase();
		}
	}

	public enum MusicStyle {
		MIXED, CLASSIC;

		public String translationKey() {
			return "bettermusictoast.musicStyle." + name().toLowerCase();
		}
	}

	public boolean enabled = true;
	public Position position = Position.TOP_LEFT;
	public DisplayMode displayMode = DisplayMode.TIMED;
	public int durationSeconds = 6;
	public AvoidMode avoidMode = AvoidMode.MOVE;
	/** Requested box size in percent; snapped to a sharp step for the current GUI scale. */
	public Integer sizePercent;
	/** Pre-1.3 setting (SMALL / NORMAL / LARGE), only read to migrate old configs. */
	private String size;
	public boolean showArtist = true;
	public boolean showMusicDiscs = true;
	public boolean hideVanillaToast = true;
	public boolean showInMenus = false;
	public MusicStyle musicStyle = MusicStyle.MIXED;
	public ColorTheme colorTheme = ColorTheme.CLASSIC;

	public static ModConfig load() {
		Path source = Files.exists(PATH) ? PATH : LEGACY_PATH;
		if (Files.exists(source)) {
			try (Reader reader = Files.newBufferedReader(source)) {
				ModConfig config = GSON.fromJson(reader, ModConfig.class);
				if (config != null) {
					config.sanitize();
					if (source != PATH) {
						config.save();
					}
					return config;
				}
			} catch (IOException | JsonParseException e) {
				LOGGER.warn("Could not read {}, using defaults", source, e);
			}
		}
		ModConfig config = new ModConfig().sanitize();
		config.save();
		return config;
	}

	/*
	 * The box stays pixel-sharp only if every font pixel covers a whole number of screen
	 * pixels, so sizes are expressed in screen pixels per GUI pixel: from one step below
	 * normal (the GUI scale itself) up to twice the normal size.
	 */

	public static int minSizePixels(int guiScale) {
		return Math.max(1, guiScale - 1);
	}

	public static int maxSizePixels(int guiScale) {
		return guiScale * 2;
	}

	public int sizePixels(int guiScale) {
		return Math.clamp(Math.round(sizePercent / 100.0f * guiScale), minSizePixels(guiScale), maxSizePixels(guiScale));
	}

	public float scale(int guiScale) {
		return (float) sizePixels(guiScale) / guiScale;
	}

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			LOGGER.warn("Could not write {}", PATH, e);
		}
	}

	/** Gson leaves unknown enum names as null, so fall back to defaults. */
	private ModConfig sanitize() {
		ModConfig defaults = new ModConfig();
		if (position == null) position = defaults.position;
		if (displayMode == null) displayMode = defaults.displayMode;
		if (avoidMode == null) avoidMode = defaults.avoidMode;
		if (musicStyle == null) musicStyle = defaults.musicStyle;
		if (colorTheme == null) colorTheme = defaults.colorTheme;
		if (sizePercent == null) {
			sizePercent = switch (size == null ? "NORMAL" : size) {
				case "SMALL" -> 75;
				case "LARGE" -> 125;
				default -> 100;
			};
		}
		size = null;
		sizePercent = Math.clamp(sizePercent, 25, 400);
		durationSeconds = Math.clamp(durationSeconds, 2, 30);
		return this;
	}
}
