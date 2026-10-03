package de.bettermusictoast.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.bettermusictoast.BetterMusicToastClient;
import de.bettermusictoast.config.ModConfig;
import de.bettermusictoast.track.MusicStyleFilter;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractSoundInstance.class)
public abstract class AbstractSoundInstanceMixin {
	@Shadow
	@Final
	protected SoundSource source;

	/** Picks only C418 tracks for game music when "Music Selection: Classic" is set. */
	@WrapOperation(method = "resolve", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/sounds/WeighedSoundEvents;getSound(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/client/resources/sounds/Sound;"))
	private Sound bettermusictoast$pickMusicStyle(WeighedSoundEvents events, RandomSource random,
			Operation<Sound> original, @Local(argsOnly = true) SoundManager soundManager) {
		if (source != SoundSource.MUSIC || BetterMusicToastClient.config().musicStyle != ModConfig.MusicStyle.CLASSIC) {
			return original.call(events, random);
		}
		return MusicStyleFilter.pickClassic(bettermusictoast$eventId(), events, e -> original.call(e, random), soundManager);
	}

	/** The music event being played, e.g. minecraft:music.overworld.swamp. */
	@Unique
	private Identifier bettermusictoast$eventId() {
		SoundInstance self = (SoundInstance) (Object) this;
		// Renamed together with ResourceLocation -> Identifier in 1.21.11.
		//? if >=1.21.11 {
		return self.getIdentifier();
		//?} else {
		/*return self.getLocation();
		*///?}
	}
}
