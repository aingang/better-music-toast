package de.bettermusictoast.track;

import java.util.Locale;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/**
 * Turns a playing sound file into a readable title. Minecraft names its music discs
 * ({@code "item.record.cat.desc": "C418 - cat"}); the names of the music files
 * ({@code "music.game.calm1": "C418 - Minecraft"}) come with the mod.
 */
public final class TrackResolver {
	/** Dashes between artist and title; translations differ, e.g. German uses "–" and Polish "—". */
	private static final String[] SEPARATORS = {" - ", " – ", " — "};

	private TrackResolver() {
	}

	/**
	 * @param file  the sound file, e.g. "minecraft:music/game/calm1"
	 * @param event the sound event that played it, e.g. "minecraft:record.cat"
	 */
	public static TrackInfo resolve(ResourceLocation file, ResourceLocation event, boolean disc) {
		//#if MC>=11200
		String path = file.getPath();
		String namespace = file.getNamespace();
		//#else
		//$$ String path = file.getResourcePath();
		//$$ String namespace = file.getResourceDomain();
		//#endif
		if (path.startsWith("sounds/")) {
			path = path.substring("sounds/".length());
		}
		if (path.endsWith(".ogg")) {
			path = path.substring(0, path.length() - ".ogg".length());
		}
		String fileName = path.substring(path.lastIndexOf('/') + 1);
		ItemStack icon = disc ? discItem(namespace, fileName, event) : null;

		// Same keys as Minecraft's own song names in newer versions, e.g. "music.game.calm1".
		String shortKey = (namespace.equals("minecraft") ? "" : namespace + ".") + path.replace('/', '.');
		String[] keys = disc
				? new String[] {"item.record." + fileName + ".desc", shortKey}
				: new String[] {shortKey};
		for (String key : keys) {
			String text = I18n.format(key);
			if (!text.equals(key)) {
				return fromTranslation(text, icon);
			}
		}

		// Unknown (e.g. modded) track: prettify the file name and credit the mod.
		return new TrackInfo(prettify(fileName), modName(namespace), icon);
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

	private static ItemStack discItem(String namespace, String fileName, ResourceLocation event) {
		Item item = Item.getByNameOrId(namespace + ":record_" + fileName);
		if (item == null) {
			// Discs of other mods: the disc that plays this sound event.
			SoundEvent sound = SoundEvent.REGISTRY.getObject(event);
			item = sound == null ? null : ItemRecord.getBySound(sound);
		}
		return item == null ? null : new ItemStack(item);
	}

	private static String modName(String namespace) {
		if (namespace.equals("minecraft")) {
			return null;
		}
		ModContainer mod = Loader.instance().getIndexedModList().get(namespace);
		return mod != null ? mod.getName() : namespace;
	}

	private static String prettify(String fileName) {
		StringBuilder result = new StringBuilder();
		for (String word : fileName.split("[_\\-]+")) {
			if (word.isEmpty()) continue;
			if (result.length() > 0) result.append(' ');
			result.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
		}
		return result.length() == 0 ? fileName : result.toString();
	}
}
