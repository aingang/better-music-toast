package de.bettermusictoast.track;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Mods that play music with a player of their own, past Minecraft's sound engine, so the tracker never hears
 * their songs: Reactive Music, Music Triggers (6 and 7) and Music Player. They are read by reflection, so none
 * of them is needed; a mod that is missing, or whose insides changed, is simply not asked again.
 */
final class ExternalMusic {
	/** A song another mod plays. */
	static final class Song {
		/** Changes when another song starts. */
		final String key;
		final String title;
		final String artist;
		/** False while paused or stopped. */
		final boolean playing;
		/** Whether the mod plays it at Minecraft's music volume (else at its own volume). */
		final boolean musicVolume;

		Song(String key, String title, String artist, boolean playing, boolean musicVolume) {
			this.key = key;
			this.title = title;
			this.artist = artist;
			this.playing = playing;
			this.musicVolume = musicVolume;
		}
	}

	private static final Source[] SOURCES = {new ReactiveMusic(), new MusicTriggers7(), new MusicTriggers6(), new MusicPlayer()};
	private static final Map<String, Method> METHODS = new HashMap<String, Method>();

	private ExternalMusic() {
	}

	/** The song one of these mods plays right now (a playing one first), or null. */
	static Song current() {
		Song stopped = null;
		for (Source source : SOURCES) {
			Song song = source.poll();
			if (song != null && song.playing) {
				return song;
			}
			if (stopped == null) {
				stopped = song;
			}
		}
		return stopped;
	}

	private abstract static class Source {
		private boolean disabled;

		Song poll() {
			if (disabled) {
				return null;
			}
			try {
				return read();
			} catch (Exception e) {
				// The mod is not installed, or it works differently than expected: leave it alone from now on.
				disabled = true;
			} catch (LinkageError e) {
				disabled = true;
			}
			return null;
		}

		abstract Song read() throws Exception;
	}

	/** Reactive Music (1.19.2 – 1.21.11): one song at a time from the chosen songpack. */
	private static final class ReactiveMusic extends Source {
		private Field currentSong;
		private Field thread;
		private Field songpack;

		@Override
		Song read() throws Exception {
			if (currentSong == null) {
				Class<?> main = Class.forName("circuitlord.reactivemusic.ReactiveMusic");
				currentSong = Class.forName("circuitlord.reactivemusic.PlayerThread").getField("currentSong");
				thread = main.getField("thread");
				try {
					songpack = main.getField("currentSongpack");
				} catch (NoSuchFieldException e) {
					// Before Reactive Music 1.0 there is only one songpack and no name to show.
				}
			}
			String song = (String) currentSong.get(null);
			if (song == null) {
				return null;
			}
			Object player = thread.get(null);
			boolean playing = player != null && isPlaying(player);
			String pack = null;
			Object zip = songpack != null ? songpack.get(null) : null;
			Object config = zip != null ? field(zip, "config") : null;
			if (config != null) {
				pack = (String) field(config, "name");
			}
			return named(pack + "/" + song, song, pack, playing, true);
		}

		private static boolean isPlaying(Object player) {
			try {
				return (Boolean) call(player, "isPlaying");
			} catch (Exception e) {
				// The method is not public; if it cannot be reached, a song name means it plays.
				return true;
			}
		}
	}

	/** Music Triggers 7 (one jar for 1.12.2 – 1.21.1): songs on several channels. */
	private static final class MusicTriggers7 extends Source {
		private Method clientHelper;

		@Override
		Song read() throws Exception {
			if (clientHelper == null) {
				clientHelper = Class.forName("mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper")
						.getMethod("getClientHelper");
			}
			Object helper = clientHelper.invoke(null);
			if (helper == null) {
				return null;
			}
			Song stopped = null;
			for (Object channel : values(call(helper, "getChannels"))) {
				if (!(Boolean) call(channel, "isClientChannel")) {
					continue;
				}
				String name = (String) call(channel, "getPlayingSongName");
				if (name == null) {
					continue;
				}
				Object player = call(channel, "getPlayer");
				boolean playing = player != null && call(player, "getPlayingTrack") != null && !(Boolean) call(player, "isPaused");
				Song song = named(call(channel, "getName") + "/" + name, name, null, playing, true);
				if (playing) {
					return song;
				}
				if (stopped == null) {
					stopped = song;
				}
			}
			return stopped;
		}
	}

	/** Music Triggers 6 (one jar per Minecraft version, 1.12.2 – 1.19.2). */
	private static final class MusicTriggers6 extends Source {
		private Method orderedChannels;

		@Override
		Song read() throws Exception {
			if (orderedChannels == null) {
				orderedChannels = Class.forName("mods.thecomputerizer.musictriggers.client.channels.ChannelManager")
						.getMethod("getOrderedChannels");
			}
			Object channels = orderedChannels.invoke(null);
			if (channels == null) {
				return null;
			}
			Song stopped = null;
			for (Object channel : values(channels)) {
				Object audio = call(channel, "getCurTrack");
				String name = audio != null ? (String) call(audio, "getName") : null;
				if (name == null) {
					continue;
				}
				boolean playing = (Boolean) call(channel, "isPlaying") && !(Boolean) call(channel, "isPaused");
				Song song = named(call(channel, "getChannelName") + "/" + name, name, null, playing, true);
				if (playing) {
					return song;
				}
				if (stopped == null) {
					stopped = song;
				}
			}
			return stopped;
		}
	}

	/** Music Player (U-Team): YouTube, SoundCloud, radio and files at its own volume, with title and author. */
	private static final class MusicPlayer extends Source {
		private Method player;

		@Override
		Song read() throws Exception {
			if (player == null) {
				player = Class.forName("info.u_team.music_player.musicplayer.MusicPlayerManager").getMethod("getPlayer");
			}
			Object musicPlayer = player.invoke(null);
			Object manager = musicPlayer != null ? call(musicPlayer, "getTrackManager") : null;
			Object track = manager != null ? call(manager, "getCurrentTrack") : null;
			Object info = track != null ? call(track, "getInfo") : null;
			if (info == null) {
				return null;
			}
			String title = text(info, "getFixedTitle", "getTitle");
			if (title == null) {
				return null;
			}
			String author = text(info, "getFixedAuthor", "getAuthor");
			String key = text(info, "getIdentifier", "getURI");
			boolean playing = !(Boolean) call(manager, "isPaused");
			String[] split = author == null ? TrackResolver.splitArtist(title) : null;
			return split != null
					? new Song("musicplayer/" + key, split[1], split[0], playing, false)
					: new Song("musicplayer/" + key + "/" + title, title, author, playing, false);
		}

		/** The first of the two texts that is there, e.g. the cleaned-up title, else the raw one. */
		private static String text(Object info, String method, String fallback) throws Exception {
			String text = null;
			try {
				text = (String) call(info, method);
			} catch (NoSuchMethodException e) {
				// Older versions only have the raw text.
			}
			if (text == null || text.trim().isEmpty()) {
				text = (String) call(info, fallback);
			}
			return text == null || text.trim().isEmpty() ? null : text.trim();
		}
	}

	/** A song named by its file: "Artist - Title", or the file name made readable ("ACelticTale" → "A Celtic Tale"). */
	private static Song named(String key, String name, String artist, boolean playing, boolean musicVolume) {
		String file = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1);
		int dot = file.lastIndexOf('.');
		if (dot > 0 && file.length() - dot <= 5) {
			file = file.substring(0, dot);
		}
		String[] split = TrackResolver.splitArtist(file);
		if (split != null) {
			return new Song(key, split[1], split[0], playing, musicVolume);
		}
		return new Song(key, readable(file), artist, playing, musicVolume);
	}

	private static String readable(String file) {
		String spaced = file.replaceAll("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", " ").replaceAll("[_\\-]+", " ").trim();
		StringBuilder result = new StringBuilder();
		for (String word : spaced.split("\\s+")) {
			if (word.isEmpty()) {
				continue;
			}
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
		}
		return result.length() == 0 ? file : result.toString();
	}

	private static Collection<?> values(Object container) {
		if (container instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) container;
			synchronized (map) {
				return new ArrayList<Object>(map.values());
			}
		}
		if (container instanceof Collection) {
			Collection<?> collection = (Collection<?>) container;
			synchronized (collection) {
				return new ArrayList<Object>(collection);
			}
		}
		return new ArrayList<Object>();
	}

	private static Object field(Object target, String name) throws Exception {
		return target.getClass().getField(name).get(target);
	}

	/** Calls a method without arguments, also one the other mod's class does not make public. */
	private static Object call(Object target, String name) throws Exception {
		String id = target.getClass().getName() + "#" + name;
		Method method = METHODS.get(id);
		if (method == null) {
			method = find(target.getClass(), name);
			METHODS.put(id, method);
		}
		return method.invoke(target);
	}

	private static Method find(Class<?> type, String name) throws NoSuchMethodException {
		// A public method declared by a public type (often an interface) can be called from anywhere.
		for (Method method : type.getMethods()) {
			if (method.getName().equals(name) && method.getParameterTypes().length == 0
					&& Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
				return method;
			}
		}
		List<Class<?>> types = new ArrayList<Class<?>>();
		for (Class<?> current = type; current != null; current = current.getSuperclass()) {
			types.add(current);
		}
		for (Class<?> current : types) {
			for (Method method : current.getDeclaredMethods()) {
				if (method.getName().equals(name) && method.getParameterTypes().length == 0) {
					method.setAccessible(true);
					return method;
				}
			}
		}
		throw new NoSuchMethodException(type.getName() + "." + name);
	}
}
