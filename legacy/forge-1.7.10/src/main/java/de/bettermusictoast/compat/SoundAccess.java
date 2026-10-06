package de.bettermusictoast.compat;

import com.google.common.collect.Multimap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;
import paulscode.sound.SoundSystem;

/**
 * The parts of Minecraft's sound engine and music ticker the mod needs: the file a sound plays, music
 * that keeps playing in the pause menu (like 1.21.6+) and the "Music Frequency" pauses between songs.
 */
public final class SoundAccess {
	private static final Fields<SoundManager> SOUND_MANAGER = new Fields<SoundManager>(SoundHandler.class, "sndManager", "field_147694_f");
	private static final Fields<SoundSystem> SOUND_SYSTEM = new Fields<SoundSystem>(SoundManager.class, "sndSystem", "field_148620_e");
	private static final Fields<Boolean> LOADED = new Fields<Boolean>(SoundManager.class, "loaded", "field_148617_f");
	private static final Fields<Integer> PLAY_TIME = new Fields<Integer>(SoundManager.class, "playTime", "field_148618_g");
	private static final Fields<Map<String, ISound>> PLAYING_SOUNDS = new Fields<Map<String, ISound>>(SoundManager.class, "playingSounds", "field_148629_h");
	private static final Fields<Map<ISound, SoundPoolEntry>> POOL_ENTRIES = new Fields<Map<ISound, SoundPoolEntry>>(SoundManager.class, "playingSoundPoolEntries", "field_148627_j");
	private static final Fields<Multimap<SoundCategory, String>> CATEGORY_SOUNDS = new Fields<Multimap<SoundCategory, String>>(SoundManager.class, "categorySounds", "field_148628_k");
	private static final Fields<List<ITickableSound>> TICKABLE_SOUNDS = new Fields<List<ITickableSound>>(SoundManager.class, "tickableSounds", "field_148625_l");
	private static final Fields<Map<String, Integer>> STOP_TIMES = new Fields<Map<String, Integer>>(SoundManager.class, "playingSoundsStopTime", "field_148624_n");

	private static final Fields<MusicTicker> MUSIC_TICKER = new Fields<MusicTicker>(Minecraft.class, "mcMusicTicker", "field_147126_aw");
	private static final Fields<Random> MUSIC_RANDOM = new Fields<Random>(MusicTicker.class, "field_147679_a");
	private static final Fields<ISound> CURRENT_MUSIC = new Fields<ISound>(MusicTicker.class, "field_147678_c");
	private static final Fields<Integer> TIME_UNTIL_NEXT_MUSIC = new Fields<Integer>(MusicTicker.class, "field_147676_d");

	/** Songs that have been heard playing during the current pause. */
	private static final Set<String> STARTED_MUSIC = new HashSet<String>();

	private SoundAccess() {
	}

	/** The sound file a playing sound was started with, or null if the sound engine has no record of it (yet). */
	public static SoundPoolEntry entry(SoundManager manager, ISound sound) {
		return POOL_ENTRIES.get(manager).get(sound);
	}

	/**
	 * Called instead of SoundHandler.pauseSounds() when the pause menu opens (see the core mod): like
	 * pauseAllSounds, but music and menu clicks keep playing, like SoundEngine.pauseAllExcept(MUSIC, UI)
	 * in 1.21.6+. Menu clicks play in MASTER, which is not listed in categorySounds.
	 */
	public static void pauseAllButMusic(SoundHandler handler) {
		SoundManager manager = SOUND_MANAGER.get(handler);
		if (!LOADED.get(manager)) {
			return;
		}
		SoundSystem soundSystem = SOUND_SYSTEM.get(manager);
		Multimap<SoundCategory, String> categorySounds = CATEGORY_SOUNDS.get(manager);
		for (String id : PLAYING_SOUNDS.get(manager).keySet()) {
			if (categorySounds.containsValue(id) && !categorySounds.containsEntry(SoundCategory.MUSIC, id)) {
				soundSystem.pause(id);
			}
		}
		STARTED_MUSIC.clear();
	}

	/**
	 * Like SoundEngine.tickMusicWhenPaused in 1.21.6+. Minecraft does not look after its sounds while
	 * paused; without this, a song that ended during the pause would count as playing until the game
	 * goes on, and would even start over then.
	 */
	public static void cleanUpEndedMusic(SoundHandler handler) {
		SoundManager manager = SOUND_MANAGER.get(handler);
		if (!LOADED.get(manager)) {
			return;
		}
		SoundSystem soundSystem = SOUND_SYSTEM.get(manager);
		int playTime = PLAY_TIME.get(manager);
		Multimap<SoundCategory, String> categorySounds = CATEGORY_SOUNDS.get(manager);
		Map<String, Integer> stopTimes = STOP_TIMES.get(manager);
		Iterator<Map.Entry<String, ISound>> iterator = PLAYING_SOUNDS.get(manager).entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, ISound> entry = iterator.next();
			String id = entry.getKey();
			if (!categorySounds.containsEntry(SoundCategory.MUSIC, id)) {
				continue;
			}
			if (soundSystem.playing(id)) {
				STARTED_MUSIC.add(id);
				continue;
			}
			// A song that was just started may not be reported as playing yet.
			Integer stopTime = stopTimes.get(id);
			if (!STARTED_MUSIC.contains(id) && (stopTime == null || stopTime > playTime)) {
				continue;
			}
			ISound sound = entry.getValue();
			iterator.remove();
			soundSystem.removeSource(id);
			stopTimes.remove(id);
			POOL_ENTRIES.get(manager).remove(sound);
			categorySounds.remove(SoundCategory.MUSIC, id);
			if (sound instanceof ITickableSound) {
				TICKABLE_SOUNDS.get(manager).remove(sound);
			}
			STARTED_MUSIC.remove(id);
		}
	}

	/** Minecraft 1.7.10 has no getter for its music ticker. */
	public static MusicTicker musicTicker(Minecraft mc) {
		return MUSIC_TICKER.get(mc);
	}

	public static Random musicRandom(MusicTicker ticker) {
		return MUSIC_RANDOM.get(ticker);
	}

	public static boolean isMusicPlaying(MusicTicker ticker) {
		return CURRENT_MUSIC.get(ticker) != null;
	}

	public static int timeUntilNextMusic(MusicTicker ticker) {
		return TIME_UNTIL_NEXT_MUSIC.get(ticker);
	}

	public static void setTimeUntilNextMusic(MusicTicker ticker, int ticks) {
		TIME_UNTIL_NEXT_MUSIC.set(ticker, ticks);
	}
}
