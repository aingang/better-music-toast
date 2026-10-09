package de.bettermusictoast.track;

import java.util.Locale;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

/**
 * Turns a playing sound file into a readable title. Minecraft 1.7.10 names its music discs
 * ({@code "item.record.cat.desc": "C418 - cat"}); the names of the music files
 * ({@code "music.game.calm1": "C418 - Minecraft"}) come with the mod.
 */
public final class TrackResolver {
	/** Dashes between artist and title; translations differ, e.g. German uses "–" and Polish "—". */
	private static final String[] SEPARATORS = {" - ", " – ", " — "};

	private TrackResolver() {
	}

	/** @param file the sound file, e.g. "minecraft:sounds/music/game/calm1.ogg" */
	public static TrackInfo resolve(ResourceLocation file, boolean disc) {
		String path = file.getResourcePath();
		if (path.startsWith("sounds/")) {
			path = path.substring("sounds/".length());
		}
		if (path.endsWith(".ogg")) {
			path = path.substring(0, path.length() - ".ogg".length());
		}
		String namespace = file.getResourceDomain();
		String fileName = path.substring(path.lastIndexOf('/') + 1);
		ItemStack icon = disc ? discItem(namespace, fileName) : null;

		// Same keys as Minecraft's own song names in newer versions, e.g. "music.game.calm1".
		String shortKey = (namespace.equals("minecraft") ? "" : namespace + ".") + path.replace('/', '.');
		String[] keys = disc
				? new String[] {"item.record." + fileName + ".desc", shortKey}
				: new String[] {shortKey};
		String translation = null;
		for (String key : keys) {
			String text = I18n.format(key);
			if (!text.equals(key)) {
				translation = text;
				break;
			}
		}

		// A resource pack or mod may play another song under this path; then that song's own name counts.
		TrackInfo fromPack = PackTrackInfo.resolve(file, translation != null, fileName, icon);
		if (fromPack != null) {
			return fromPack;
		}
		if (translation != null) {
			return fromTranslation(translation, icon);
		}

		// Unknown (e.g. modded) track: prettify the file name and credit the mod.
		return new TrackInfo(prettify(fileName), modName(namespace), icon, true);
	}

	private static TrackInfo fromTranslation(String text, ItemStack icon) {
		String[] parts = splitArtist(text);
		return parts != null ? new TrackInfo(parts[1], parts[0], icon) : new TrackInfo(text, null, icon);
	}

	/** Splits "Artist - Title" into {artist, title}, or returns null if the text names no artist. */
	static String[] splitArtist(String text) {
		int best = -1;
		String separator = null;
		for (String candidate : SEPARATORS) {
			int index = text.indexOf(candidate);
			if (index > 0 && (best < 0 || index < best)) {
				best = index;
				separator = candidate;
			}
		}
		if (separator == null) {
			return null;
		}
		return new String[] {text.substring(0, best).trim(), text.substring(best + separator.length()).trim()};
	}

	private static ItemStack discItem(String namespace, String fileName) {
		Item item = (Item) Item.itemRegistry.getObject(namespace + ":record_" + fileName);
		if (item == null) {
			item = ItemRecord.getRecord("records." + fileName);
		}
		return item == null ? null : new ItemStack(item);
	}

	static String modName(String namespace) {
		if (namespace.equals("minecraft")) {
			return null;
		}
		ModContainer mod = Loader.instance().getIndexedModList().get(namespace);
		return mod != null ? mod.getName() : namespace;
	}

	static String prettify(String fileName) {
		StringBuilder result = new StringBuilder();
		for (String word : fileName.split("[_\\-]+")) {
			if (word.isEmpty()) continue;
			if (result.length() > 0) result.append(' ');
			result.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
		}
		return result.length() == 0 ? fileName : result.toString();
	}
}
