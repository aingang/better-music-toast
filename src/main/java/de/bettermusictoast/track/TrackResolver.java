package de.bettermusictoast.track;

import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Turns a playing sound into a readable title. Vanilla ships translations such as
 * {@code "music.game.swamp.aerie": "Lena Raine - Aerie"} and
 * {@code "jukebox_song.minecraft.cat": "C418 - cat"}, so we look those up first.
 */
public final class TrackResolver {
	private TrackResolver() {
	}

	public static TrackInfo resolve(SoundInstance instance, boolean disc) {
		Sound sound = instance.getSound();
		if (sound == null || sound == SoundManager.EMPTY_SOUND || sound == SoundManager.INTENTIONALLY_EMPTY_SOUND) {
			return null;
		}

		Identifier location = sound.getLocation();
		String fileName = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
		ItemStack icon = disc ? discItem(location.getNamespace(), fileName) : ItemStack.EMPTY;

		Language language = Language.getInstance();
		String[] keys = disc
				? new String[] {"jukebox_song." + location.getNamespace() + "." + fileName, location.toShortLanguageKey().replace('/', '.')}
				: new String[] {location.toShortLanguageKey().replace('/', '.')};
		for (String key : keys) {
			if (language.has(key)) {
				return fromTranslation(language.getOrDefault(key), icon);
			}
		}

		// Unknown (e.g. modded) track: prettify the file name and credit the mod.
		return new TrackInfo(prettify(fileName), modName(location.getNamespace()), icon);
	}

	private static TrackInfo fromTranslation(String text, ItemStack icon) {
		int separator = text.indexOf(" - ");
		if (separator > 0) {
			return new TrackInfo(text.substring(separator + 3).trim(), text.substring(0, separator).trim(), icon);
		}
		return new TrackInfo(text, null, icon);
	}

	private static ItemStack discItem(String namespace, String fileName) {
		Identifier id = Identifier.fromNamespaceAndPath(namespace, "music_disc_" + fileName);
		return BuiltInRegistries.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);
	}

	private static String modName(String namespace) {
		if (namespace.equals(Identifier.DEFAULT_NAMESPACE)) {
			return null;
		}
		return FabricLoader.getInstance().getModContainer(namespace)
				.map(ModContainer::getMetadata)
				.map(meta -> meta.getName())
				.orElse(namespace);
	}

	private static String prettify(String fileName) {
		StringBuilder result = new StringBuilder();
		for (String word : fileName.split("[_\\-]+")) {
			if (word.isEmpty()) continue;
			if (!result.isEmpty()) result.append(' ');
			result.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
		}
		return result.isEmpty() ? fileName : result.toString();
	}
}
