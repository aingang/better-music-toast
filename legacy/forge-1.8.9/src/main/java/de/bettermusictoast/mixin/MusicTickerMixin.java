package de.bettermusictoast.mixin;

import de.bettermusictoast.compat.MusicFrequency;
import de.bettermusictoast.compat.MusicTickerAccess;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Makes the pauses between songs follow the chosen "Music Frequency", the way MusicManager.tick
// does in 1.21.6+. "Default" leaves Minecraft 1.8.9's own timing untouched.
@Mixin(MusicTicker.class)
public abstract class MusicTickerMixin implements MusicTickerAccess {
	@Shadow
	@Final
	private Random rand;

	@Shadow
	@Final
	private Minecraft mc;

	@Shadow
	private ISound currentMusic;

	@Shadow
	private int timeUntilNextMusic;

	// The remaining pause is capped every tick, like 1.21.6+ does, so a new choice or a change of
	// music (e.g. entering the Nether) never waits longer than the frequency allows.
	@Inject(method = "update", at = @At("HEAD"))
	private void bettermusictoast$capPause(CallbackInfo ci) {
		MusicFrequency frequency = MusicFrequency.current();
		if (frequency != MusicFrequency.DEFAULT && currentMusic == null) {
			timeUntilNextMusic = Math.min(timeUntilNextMusic, frequency.nextSongDelay(mc.getAmbientMusicType(), rand));
		}
	}

	@Override
	public Random bettermusictoast$getRandom() {
		return rand;
	}

	@Override
	public void bettermusictoast$setTimeUntilNextMusic(int ticks) {
		timeUntilNextMusic = ticks;
	}
}
