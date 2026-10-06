package de.bettermusictoast.track;

import net.minecraft.item.ItemStack;

/** A resolved song: title, artist (may be null) and the disc to show (null for regular music). */
public final class TrackInfo {
	private final String title;
	private final String artist;
	private final ItemStack icon;

	public TrackInfo(String title, String artist, ItemStack icon) {
		this.title = title;
		this.artist = artist;
		this.icon = icon;
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
}
