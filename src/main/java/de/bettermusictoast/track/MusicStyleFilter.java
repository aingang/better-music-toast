package de.bettermusictoast.track;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;

/**
 * Restricts game music to C418's original soundtrack while keeping it tied to the biome:
 * <ol>
 *   <li>biomes whose vanilla music has no C418 track at all get a hand-picked pool,</li>
 *   <li>everywhere else only the C418 tracks Mojang assigned to that biome play,</li>
 *   <li>anything left (e.g. modded biomes) falls back to C418's music for the dimension.</li>
 * </ol>
 * Music events can reference other events, so instead of walking their contents we draw
 * from them repeatedly until a matching track comes up.
 */
public final class MusicStyleFilter {
	private static final int MAX_DRAWS = 40;
	private static final String CLASSIC_ARTIST = "C418";

	/** C418's overworld tracks from Volume Alpha / Beta. */
	private static final Identifier OVERWORLD_POOL = Identifier.withDefaultNamespace("music.game");
	/** Contains C418's four Nether tracks next to Lena Raine's Rubedo. */
	private static final Identifier NETHER_POOL = Identifier.withDefaultNamespace("music.nether.nether_wastes");

	/**
	 * Biomes whose vanilla music contains no C418 track. Calm but slightly darker pieces fit
	 * the swamp and the deep dark best.
	 */
	private static final Map<String, Set<String>> CUSTOM_POOLS = Map.of(
			"music.overworld.swamp", Set.of("subwoofer_lullaby", "living_mice", "oxygene", "key", "mice_on_venus"),
			"music.overworld.deep_dark", Set.of("subwoofer_lullaby", "living_mice", "key", "oxygene"));

	private MusicStyleFilter() {
	}

	public static Sound pickClassic(Identifier eventId, WeighedSoundEvents events,
			Function<WeighedSoundEvents, Sound> draw, SoundManager soundManager) {
		String event = eventId.getPath();

		Set<String> custom = CUSTOM_POOLS.get(event);
		if (custom != null) {
			Sound found = drawMatching(soundManager.getSoundEvent(OVERWORLD_POOL), draw,
					sound -> isClassic(sound) && custom.contains(fileName(sound)));
			if (found != null) {
				return found;
			}
		}

		Sound first = draw.apply(events);
		if (isClassic(first)) {
			return first;
		}
		Sound found = drawMatching(events, draw, MusicStyleFilter::isClassic);
		if (found == null) {
			// Nether music must never fall back to overworld tracks (the warped forest has none at all).
			Identifier fallback = event.startsWith("music.nether") ? NETHER_POOL : OVERWORLD_POOL;
			WeighedSoundEvents pool = soundManager.getSoundEvent(fallback);
			if (pool != events) {
				found = drawMatching(pool, draw, MusicStyleFilter::isClassic);
			}
		}
		return found != null ? found : first;
	}

	private static Sound drawMatching(WeighedSoundEvents events, Function<WeighedSoundEvents, Sound> draw,
			Predicate<Sound> accept) {
		if (events == null) {
			return null;
		}
		for (int i = 0; i < MAX_DRAWS; i++) {
			Sound sound = draw.apply(events);
			if (accept.test(sound)) {
				return sound;
			}
		}
		return null;
	}

	private static String fileName(Sound sound) {
		String path = sound.getLocation().getPath();
		return path.substring(path.lastIndexOf('/') + 1);
	}

	/** Vanilla names tracks "Artist - Title" in its language files, e.g. "C418 - Sweden". */
	private static boolean isClassic(Sound sound) {
		if (sound == null || sound == SoundManager.EMPTY_SOUND) {
			return false;
		}
		String key = sound.getLocation().toShortLanguageKey().replace('/', '.');
		Language language = Language.getInstance();
		if (!language.has(key)) {
			return false;
		}
		String[] parts = TrackResolver.splitArtist(language.getOrDefault(key));
		return parts != null && parts[0].equals(CLASSIC_ARTIST);
	}
}
