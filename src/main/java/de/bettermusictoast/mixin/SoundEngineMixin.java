package de.bettermusictoast.mixin;

// Only before 1.21.6: lets music keep playing while the game is paused (see MinecraftMixin).
//? if <1.21.6 {
/*import com.google.common.collect.Multimap;
import com.mojang.blaze3d.audio.Channel;
import de.bettermusictoast.compat.PausesAllButMusic;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin implements PausesAllButMusic {
	@Shadow
	private boolean loaded;

	@Shadow
	@Final
	private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

	@Shadow
	@Final
	private Multimap<SoundSource, SoundInstance> instanceBySource;

	@Shadow
	@Final
	private Map<SoundInstance, Integer> soundDeleteTime;

	// Like SoundEngine.pauseAllExcept(MUSIC, UI) in 1.21.6+. The UI category only exists since
	// 1.21.6; before that menu clicks play in MASTER.
	@Override
	public void bettermusictoast$pauseAllButMusic() {
		if (!loaded) {
			return;
		}
		for (Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry : instanceToChannel.entrySet()) {
			SoundSource source = entry.getKey().getSource();
			if (source != SoundSource.MUSIC && source != SoundSource.MASTER) {
				entry.getValue().execute(Channel::pause);
			}
		}
	}

	// Like SoundEngine.tickMusicWhenPaused in 1.21.6+: a song that ends during the pause is cleaned
	// up, so the next one can start.
	@Inject(method = "tick(Z)V", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/sounds/ChannelAccess;scheduleTick()V"))
	private void bettermusictoast$tickMusicWhenPaused(boolean paused, CallbackInfo ci) {
		if (!paused) {
			return;
		}
		Iterator<Map.Entry<SoundInstance, ChannelAccess.ChannelHandle>> iterator = instanceToChannel.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry = iterator.next();
			SoundInstance sound = entry.getKey();
			if (sound.getSource() == SoundSource.MUSIC && entry.getValue().isStopped()) {
				iterator.remove();
				soundDeleteTime.remove(sound);
				instanceBySource.remove(sound.getSource(), sound);
			}
		}
	}
}
*///?}
