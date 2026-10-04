package de.bettermusictoast.track;

import net.minecraft.world.item.ItemStack;

/**
 * @param title  song title, never null
 * @param artist composer, or null if unknown
 * @param icon   item to show instead of the music-note icon (music discs), or {@link ItemStack#EMPTY}
 */
// Before 1.17 the jar targets Java 8, where Jabel turns records into plain classes.
//? if <1.17
/*@com.github.bsideup.jabel.Desugar*/
public record TrackInfo(String title, String artist, ItemStack icon) {
}
