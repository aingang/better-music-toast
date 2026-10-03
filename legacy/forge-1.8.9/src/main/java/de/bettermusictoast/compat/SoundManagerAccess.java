package de.bettermusictoast.compat;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundPoolEntry;

/** Implemented on SoundManager by de.bettermusictoast.mixin.SoundManagerMixin. */
public interface SoundManagerAccess {
	/** The sound file a playing sound was started with, or null. */
	SoundPoolEntry bettermusictoast$getEntry(ISound sound);

	/** Like pauseAllSounds, but music and menu clicks keep playing (1.21.6+ behaviour). */
	void bettermusictoast$pauseAllButMusic();

	/** Cleans up songs that ended while the game is paused, so the next one can start. */
	void bettermusictoast$cleanUpEndedMusic();
}
