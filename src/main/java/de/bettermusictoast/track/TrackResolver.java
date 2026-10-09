package de.bettermusictoast.track;

import java.util.Locale;
//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
//?} else if forge {
/*import net.minecraftforge.fml.ModList;
*///?} else {
/*import net.neoforged.fml.ModList;
*///?}
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
//? if >=1.19.3 {
import net.minecraft.core.registries.BuiltInRegistries;
//?} else
/*import net.minecraft.core.Registry;*/
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Turns a playing sound into a readable title. Vanilla ships translations such as
 * {@code "music.game.swamp.aerie": "Lena Raine - Aerie"} and
 * {@code "jukebox_song.minecraft.cat": "C418 - cat"}, so we look those up first.
 */
public final class TrackResolver {
	/** Dashes between artist and title; translations differ, e.g. German uses "–" and Polish "—". */
	private static final String[] SEPARATORS = {" - ", " – ", " — "};

	private TrackResolver() {
	}

	public static TrackInfo resolve(SoundInstance instance, boolean disc) {
		Sound sound = instance.getSound();
		//? if >=1.19.4 {
		if (sound == null || sound == SoundManager.EMPTY_SOUND || sound == SoundManager.INTENTIONALLY_EMPTY_SOUND) {
		//?} else
		/*if (sound == null || sound == SoundManager.EMPTY_SOUND) {*/
			return null;
		}

		Identifier location = sound.getLocation();
		String fileName = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
		ItemStack icon = disc ? discItem(location.getNamespace(), fileName) : ItemStack.EMPTY;

		Language language = Language.getInstance();
		//? if >=1.21 {
		String[] keys = disc
				? new String[] {"jukebox_song." + location.getNamespace() + "." + fileName, location.toShortLanguageKey().replace('/', '.')}
				: new String[] {location.toShortLanguageKey().replace('/', '.')};
		//?} else {
		/*// Before 1.21 jukebox songs are named in the disc's description, e.g. "C418 - cat".
		//? if >=1.19 {
		String[] keys = disc
				? new String[] {"item." + location.getNamespace() + ".music_disc_" + fileName + ".desc", location.toShortLanguageKey().replace('/', '.')}
				: new String[] {location.toShortLanguageKey().replace('/', '.')};
		//?} else {
		/^String shortKey = de.bettermusictoast.compat.McCompat.shortLanguageKey(location).replace('/', '.');
		String[] keys = disc
				? new String[] {"item." + location.getNamespace() + ".music_disc_" + fileName + ".desc", shortKey}
				: new String[] {shortKey};
		^///?}
		*///?}
		String translation = null;
		for (String key : keys) {
			if (language.has(key)) {
				translation = language.getOrDefault(key);
				break;
			}
		}

		// A resource pack or mod may play another song under this path; then that song's own name counts.
		TrackInfo fromPack = PackTrackInfo.resolve(sound.getPath(), keys, translation != null, fileName, icon);
		if (fromPack != null) {
			return fromPack;
		}
		if (translation != null) {
			return fromTranslation(translation, icon);
		}

		// Unknown (e.g. modded) track: prettify the file name and credit the mod.
		return new TrackInfo(prettify(fileName), modName(location.getNamespace()), icon, true);
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
		Identifier id = Identifier.fromNamespaceAndPath(namespace, "music_disc_" + fileName);
		//? if >=1.19.3 {
		return BuiltInRegistries.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);
		//?} else
		/*return Registry.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);*/
	}

	static String modName(String namespace) {
		//? if >=1.17 {
		if (namespace.equals(Identifier.DEFAULT_NAMESPACE)) {
		//?} else
		/*if (namespace.equals("minecraft")) {*/
			return null;
		}
		String name = modDisplayName(namespace);
		return name != null ? name : namespace;
	}

	/** The name of the loaded mod with this id, or null if there is none. */
	static String modDisplayName(String modId) {
		//? if fabric {
		return FabricLoader.getInstance().getModContainer(modId)
				.map(ModContainer::getMetadata)
				.map(meta -> meta.getName())
				.orElse(null);
		//?} else if forge && >=26.1 {
		/*// Since 26.1 Forge's mod list is static.
		return ModList.getModContainerById(modId)
				.map(mod -> mod.getModInfo().getDisplayName())
				.orElse(null);
		*///?} else {
		/*return ModList.get().getModContainerById(modId)
				.map(mod -> mod.getModInfo().getDisplayName())
				.orElse(null);
		*///?}
	}

	/** The name of the mod whose jar contains this file (e.g. "assets/minecraft/sounds/…"), or null. */
	static String modOwning(String path) {
		try {
			//? if fabric {
			for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
				if (mod.findPath(path).isPresent()) {
					return mod.getMetadata().getName();
				}
			}
			//?} else {
			/*// Looking through every mod file takes a moment, and the same songs come again.
			if (MOD_OWNING.containsKey(path)) {
				return MOD_OWNING.get(path);
			}
			String owner = null;
			//? if forge && >=26.1 {
			/^for (var file : ModList.getModFiles()) {^/
			//?} else
			for (var file : ModList.get().getModFiles()) {
				if (modFileContains(file.getFile().getFilePath(), path)) {
					owner = file.getMods().get(0).getDisplayName();
					break;
				}
			}
			MOD_OWNING.put(path, owner);
			return owner;
			*///?}
		} catch (RuntimeException | LinkageError e) {
			// An unusual mod file or loader: then the source stays unknown.
		}
		return null;
	}

	//? if !fabric {
	/*private static final java.util.Map<String, String> MOD_OWNING = new java.util.HashMap<>();

	private static boolean modFileContains(java.nio.file.Path modFile, String path) {
		try {
			if (java.nio.file.Files.isDirectory(modFile)) {
				return java.nio.file.Files.exists(modFile.resolve(path));
			}
			try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(modFile.toFile())) {
				return zip.getEntry(path) != null;
			}
		} catch (java.io.IOException | RuntimeException e) {
			// Mods inside other mods' jars have no file of their own; they are skipped.
			return false;
		}
	}
	*///?}

	static String prettify(String fileName) {
		StringBuilder result = new StringBuilder();
		for (String word : fileName.split("[_\\-]+")) {
			if (word.isEmpty()) continue;
			// StringBuilder.isEmpty only exists since Java 15; before 1.17 the jar targets Java 8.
			//? if >=1.17 {
			if (!result.isEmpty()) result.append(' ');
			//?} else
			/*if (result.length() > 0) result.append(' ');*/
			result.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
		}
		//? if >=1.17 {
		return result.isEmpty() ? fileName : result.toString();
		//?} else
		/*return result.length() == 0 ? fileName : result.toString();*/
	}
}
