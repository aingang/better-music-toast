package de.bettermusictoast.compat;

import java.util.Random;

/** Implemented on MusicTicker by de.bettermusictoast.mixin.MusicTickerMixin. */
public interface MusicTickerAccess {
	Random bettermusictoast$getRandom();

	void bettermusictoast$setTimeUntilNextMusic(int ticks);
}
