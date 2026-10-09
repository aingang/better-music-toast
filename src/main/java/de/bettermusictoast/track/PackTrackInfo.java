package de.bettermusictoast.track;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

/**
 * Names songs whose file comes from a resource pack or a mod. Minecraft names its songs by file path
 * ("music.game.sweden"), so a pack that puts another song under a vanilla path would otherwise keep the old
 * name. In that order: the pack's own language file, the title and artist stored in the .ogg file, and
 * finally the name of the pack or mod.
 */
final class PackTrackInfo {
	private PackTrackInfo() {
	}

	/**
	 * @param file     the sound file, e.g. "minecraft:sounds/music/game/sweden.ogg"
	 * @param keys     the language keys that name the song
	 * @param known    whether one of the keys has a translation (the path is a known song)
	 * @param fileName the file name without folder and extension
	 * @return the song's name, or null if the file is Minecraft's own (then the usual name is right)
	 */
	static TrackInfo resolve(Identifier file, String[] keys, boolean known, String fileName, ItemStack icon) {
		try {
			ResourceManager resources = Minecraft.getInstance().getResourceManager();
			//? if >=1.19 {
			Resource resource = resources.getResource(file).orElse(null);
			if (resource == null || resource.sourcePackId().equals("vanilla")) {
				return null;
			}
			String packId = resource.sourcePackId();
			String source = sourceName(packId, file);
			TrackInfo named = fromPackLanguage(resources, packId, file.getNamespace(), keys, icon);
			if (named != null) {
				return named;
			}
			String[] tags;
			try (InputStream in = resource.open()) {
				tags = OggTags.read(in);
			}
			//?} else {
			/*String source;
			String[] tags;
			try (Resource resource = resources.getResource(file)) {
				String packName = resource.getSourceName();
				if (packName.equals("Default") || packName.equals("vanilla")) {
					return null;
				}
				source = sourceName(packName, file);
				tags = OggTags.read(resource.getInputStream());
			}
			*///?}
			if (tags != null) {
				String[] split = tags[1] == null ? TrackResolver.splitArtist(tags[0]) : null;
				return split != null
						? new TrackInfo(split[1], split[0], icon)
						: new TrackInfo(tags[0], tags[1] != null ? tags[1] : source, icon);
			}
			if (known) {
				// A replaced song without a name of its own: the file name would still be the old song's.
				return new TrackInfo(source != null ? source : TrackResolver.prettify(fileName), null, icon, true);
			}
			// A new song: its file name, credited to the mod of its namespace or else to the pack.
			String namespace = file.getNamespace();
			String artist = namespace.equals("minecraft") ? source : TrackResolver.modName(namespace);
			return new TrackInfo(TrackResolver.prettify(fileName), artist, icon, true);
		} catch (IOException | RuntimeException e) {
			return null;
		}
	}

	//? if >=1.19 {
	/** The song's name from a language file in the same pack, in the game's language or else in English. */
	private static TrackInfo fromPackLanguage(ResourceManager resources, String packId, String fileNamespace, String[] keys, ItemStack icon) {
		String language = Minecraft.getInstance().options.languageCode;
		// Language files are read from every namespace; packs use "minecraft" or their own.
		String[] namespaces = fileNamespace.equals(Identifier.DEFAULT_NAMESPACE)
				? new String[] {Identifier.DEFAULT_NAMESPACE}
				: new String[] {Identifier.DEFAULT_NAMESPACE, fileNamespace};
		for (String code : language.equals("en_us") ? new String[] {"en_us"} : new String[] {language, "en_us"}) {
			for (String namespace : namespaces) {
				for (Resource lang : resources.getResourceStack(Identifier.fromNamespaceAndPath(namespace, "lang/" + code + ".json"))) {
					if (!lang.sourcePackId().equals(packId)) {
						continue;
					}
					try (Reader reader = lang.openAsReader()) {
						JsonObject json = GsonHelper.parse(reader);
						for (String key : keys) {
							JsonElement text = json.get(key);
							if (text != null && text.isJsonPrimitive()) {
								String[] parts = TrackResolver.splitArtist(text.getAsString());
								return parts != null ? new TrackInfo(parts[1], parts[0], icon) : new TrackInfo(text.getAsString(), null, icon);
							}
						}
					} catch (IOException | RuntimeException e) {
						// A broken language file names nothing.
					}
				}
			}
		}
		return null;
	}
	//?}

	/** A readable name for the pack or mod the file comes from, or null if it cannot be told. */
	private static String sourceName(String pack, Identifier file) {
		// Forge and NeoForge put all mods into one pack (inside it, Forge names each after its jar), Fabric sometimes
		// too; then the mod that has the file is the source.
		if (pack.equals("mod_resources") || pack.equals("Mod Resources") || pack.endsWith(".jar")
				|| pack.equals("fabric") || pack.equals("Fabric Mods")) {
			return TrackResolver.modOwning("assets/" + file.getNamespace() + "/" + file.getPath());
		}
		// NeoForge names each mod's pack "mod/<id>", Fabric just "<id>".
		String mod = TrackResolver.modDisplayName(pack.startsWith("mod/") ? pack.substring("mod/".length()) : pack);
		if (mod != null) {
			return mod;
		}
		//? if >=1.19 {
		Pack entry = Minecraft.getInstance().getResourcePackRepository().getPack(pack);
		return cleanPackName(entry != null ? entry.getTitle().getString() : pack);
		//?} else
		/*return cleanPackName(pack);*/
	}

	/** "file/Medieval Music.zip" → "Medieval Music" */
	private static String cleanPackName(String name) {
		if (name.startsWith("file/")) {
			name = name.substring("file/".length());
		}
		if (name.toLowerCase(Locale.ROOT).endsWith(".zip")) {
			name = name.substring(0, name.length() - ".zip".length());
		}
		return name.isEmpty() ? null : name;
	}
}
