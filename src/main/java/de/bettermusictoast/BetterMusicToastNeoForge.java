package de.bettermusictoast;

// Only NeoForge 1.20.5: it cannot mark a mod as client-only yet (@Mod has no "dist" before 1.20.6), so on a
// dedicated server the mod simply does nothing instead of loading client classes, like on Forge.
//? if neoforge && <1.20.6 {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(BetterMusicToastClient.MOD_ID)
public final class BetterMusicToastNeoForge {
	public BetterMusicToastNeoForge(IEventBus modBus, ModContainer container) {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			new BetterMusicToastClient(modBus, container);
		}
	}
}
*///?}
