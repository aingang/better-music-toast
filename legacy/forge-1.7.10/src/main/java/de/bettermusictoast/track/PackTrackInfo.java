package de.bettermusictoast.track;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Names songs whose file comes from a resource pack or a mod. The song names go by file path
 * ("music.game.calm1"), so a pack that puts another song under a vanilla path would otherwise keep the old
 * name. In that order: the title and artist stored in the .ogg file, then the name of the pack or mod.
 */
final class PackTrackInfo {
	private PackTrackInfo() {
	}

	/**
	 * @param file     the sound file, e.g. "minecraft:sounds/music/game/calm1.ogg"
	 * @param known    whether the path has a song name (it is a known song)
	 * @param fileName the file name without folder and extension
	 * @return the song's name, or null if the file is Minecraft's own (then the usual name is right)
	 */
	static TrackInfo resolve(ResourceLocation file, boolean known, String fileName, ItemStack icon) {
		String source;
		String[] tags;
		InputStream in = null;
		try {
			// 1.7.10 does not tell which pack a resource came from, so ask the packs in Minecraft's order.
			Source found = findPack(file);
			if (found == null) {
				return null;
			}
			source = found.name;
			in = found.pack.getInputStream(file);
			tags = OggTags.read(in);
		} catch (IOException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		} finally {
			closeQuietly(in);
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
		String namespace = file.getResourceDomain();
		String artist = namespace.equals("minecraft") ? source : TrackResolver.modName(namespace);
		return new TrackInfo(TrackResolver.prettify(fileName), artist, icon, true);
	}

	/**
	 * The pack that wins for this file and its name, or null if it is Minecraft's own. Minecraft stacks the
	 * packs as: its own, the mods', the chosen resource packs (top one last), the server's.
	 */
	private static Source findPack(ResourceLocation file) {
		ResourcePackRepository repository = Minecraft.getMinecraft().getResourcePackRepository();
		IResourcePack server = repository.func_148530_e();
		if (server != null && server.resourceExists(file)) {
			return new Source(server, cleanPackName(server.getPackName()));
		}
		List<?> entries = repository.getRepositoryEntries();
		for (int i = entries.size() - 1; i >= 0; i--) {
			IResourcePack pack = ((ResourcePackRepository.Entry) entries.get(i)).getResourcePack();
			if (pack.resourceExists(file)) {
				return new Source(pack, cleanPackName(pack.getPackName()));
			}
		}
		for (ModContainer mod : Loader.instance().getActiveModList()) {
			IResourcePack pack = FMLClientHandler.instance().getResourcePackFor(mod.getModId());
			if (pack != null && pack.resourceExists(file)) {
				return new Source(pack, mod.getName());
			}
		}
		return null;
	}

	/** "Medieval Music.zip" → "Medieval Music" */
	private static String cleanPackName(String pack) {
		if (pack == null) {
			return null;
		}
		if (pack.toLowerCase(Locale.ROOT).endsWith(".zip")) {
			pack = pack.substring(0, pack.length() - ".zip".length());
		}
		return pack.isEmpty() ? null : pack;
	}

	private static final class Source {
		final IResourcePack pack;
		final String name;

		Source(IResourcePack pack, String name) {
			this.pack = pack;
			this.name = name;
		}
	}

	private static void closeQuietly(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// Nothing left to do.
			}
		}
	}
}
