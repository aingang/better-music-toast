package de.bettermusictoast.track;

import net.minecraft.item.ItemStack;

/** A resolved song: title, artist (may be null) and the disc to show (null for regular music). */
public final class TrackInfo {
	private final String title;
	private final String artist;
	private final ItemStack icon;
	/** Whether the name only comes from the file or pack name, so a better one may replace it. */
	private final boolean guessed;

	public TrackInfo(String title, String artist, ItemStack icon) {
		this(title, artist, icon, false);
	}

	public TrackInfo(String title, String artist, ItemStack icon, boolean guessed) {
		this.title = title;
		this.artist = artist;
		this.icon = icon;
		this.guessed = guessed;
	}

	public String title() {
		return title;
	}

	public String artist() {
		return artist;
	}

	public ItemStack icon() {
		return icon;
	}

	public boolean guessed() {
		return guessed;
	}
}