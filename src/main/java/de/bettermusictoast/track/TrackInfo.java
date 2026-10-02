package de.bettermusictoast.track;

import net.minecraft.world.item.ItemStack;

/**
 * @param title  song title, never null
 * @param artist composer, or null if unknown
 * @param icon   item to show instead of the music-note icon (music discs), or {@link ItemStack#EMPTY}
 */
public record TrackInfo(String title, String artist, ItemStack icon) {
}
