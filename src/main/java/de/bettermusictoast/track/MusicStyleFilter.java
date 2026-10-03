package de.bettermusictoast.track;

import java.util.function.Function;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;

/**
 * Restricts game music to C418's original soundtrack. Music events can reference other events,
 * so instead of walking their contents we simply draw again until a classic track comes up.
 */
public final class MusicStyleFilter {
	private static final int MAX_DRAWS = 40;
	/** Pool with C418's overworld tracks, used when the current biome's music has none. */
	private static final Identifier FALLBACK_EVENT = Identifier.withDefaultNamespace("music.game");
	private static final String CLASSIC_ARTIST = "C418";

	private MusicStyleFilter() {
	}

	public static Sound pickClassic(WeighedSoundEvents events, Function<WeighedSoundEvents, Sound> draw,
			SoundManager soundManager) {
		Sound first = draw.apply(events);
		if (isClassic(first)) {
			return first;
		}
		Sound found = drawClassic(events, draw);
		if (found == null) {
			WeighedSoundEvents fallback = soundManager.getSoundEvent(FALLBACK_EVENT);
			if (fallback != null && fallback != events) {
				found = drawClassic(fallback, draw);
			}
		}
		return found != null ? found : first;
	}

	private static Sound drawClassic(WeighedSoundEvents events, Function<WeighedSoundEvents, Sound> draw) {
		for (int i = 0; i < MAX_DRAWS; i++) {
			Sound sound = draw.apply(events);
			if (isClassic(sound)) {
				return sound;
			}
		}
		return null;
	}

	/** Vanilla names tracks "Artist - Title" in its language files, e.g. "C418 - Sweden". */
	private static boolean isClassic(Sound sound) {
		if (sound == null || sound == SoundManager.EMPTY_SOUND) {
			return false;
		}
		String key = sound.getLocation().toShortLanguageKey().replace('/', '.');
		Language language = Language.getInstance();
		return language.has(key) && language.getOrDefault(key).startsWith(CLASSIC_ARTIST + " - ");
	}
}
