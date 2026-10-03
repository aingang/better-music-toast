package de.bettermusictoast;

// Forge entry point. Forge 1.20.1 cannot mark a mod as client-only, so on a dedicated server the
// mod simply does nothing instead of loading client classes.
//? if forge {
/*import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(BetterMusicToastClient.MOD_ID)
public final class BetterMusicToastForge {
	public BetterMusicToastForge() {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			BetterMusicToastClient.initForge(FMLJavaModLoadingContext.get().getModEventBus());
		}
	}
}
*///?}
