package de.bettermusictoast.mixin;

// Only before 1.21.6: lets music keep playing while the game is paused (see MinecraftMixin).
//? if <1.21.6 {
/*import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SoundManager.class)
public interface SoundManagerAccessor {
	@Accessor("soundEngine")
	SoundEngine bettermusictoast$getSoundEngine();
}
*///?}
