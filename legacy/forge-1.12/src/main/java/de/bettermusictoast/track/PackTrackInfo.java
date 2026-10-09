package de.bettermusictoast.track;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Names songs whose file comes from a resource pack or a mod. The song names go by file path
 * ("music.game.calm1"), so a pack that puts another song under a vanilla path would otherwise keep the old
 * name. In that order: the title and artist stored in the .ogg file, then the name of the pack or mod.
 */
final class PackTrackInfo {
	private static final String[] MOD_PACK_PREFIXES = {"FMLFileResourcePack:", "FMLFolderResourcePack:"};

	private PackTrackInfo() {
	}

	/**
	 * @param file      the sound file, e.g. "minecraft:sounds/music/game/calm1.ogg"
	 * @param namespace the file's namespace
	 * @param known     whether the path has a song name (it is a known song)
	 * @param fileName  the file name without folder and extension
	 * @return the song's name, or null if the file is Minecraft's own (then the usual name is right)
	 */
	static TrackInfo resolve(ResourceLocation file, String namespace, boolean known, String fileName, ItemStack icon) {
		String source;
		String[] tags;
		IResource resource = null;
		try {
			resource = Minecraft.getMinecraft().getResourceManager().getResource(file);
			String pack = resource.getResourcePackName();
			if (pack == null || pack.equals("Default")) {
				return null;
			}
			source = sourceName(pack);
			tags = OggTags.read(resource.getInputStream());
		} catch (IOException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		} finally {
			close(resource);
		}

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
		String artist = namespace.equals("minecraft") ? source : TrackResolver.modName(namespace);
		return new TrackInfo(TrackResolver.prettify(fileName), artist, icon, true);
	}

	/** "FMLFileResourcePack:Medieval Music" → "Medieval Music", "Medieval Music.zip" → "Medieval Music" */
	private static String sourceName(String pack) {
		for (String prefix : MOD_PACK_PREFIXES) {
			if (pack.startsWith(prefix)) {
				pack = pack.substring(prefix.length());
			}
		}
		if (pack.toLowerCase(Locale.ROOT).endsWith(".zip")) {
			pack = pack.substring(0, pack.length() - ".zip".length());
		}
		return pack.isEmpty() ? null : pack;
	}

	/** Resources are closeable since 1.10; before that only their stream is. */
	private static void close(IResource resource) {
		if (resource == null) {
			return;
		}
		try {
			if (resource instanceof Closeable) {
				((Closeable) resource).close();
			} else {
				InputStream in = resource.getInputStream();
				in.close();
			}
		} catch (IOException e) {
			// Nothing left to do.
		}
	}
}
