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
		// Before 1.19 mods.toml has no displayTest yet: the same "client-only, any server" mark in code.
		//? if >=1.18.2 && <1.19 {
		/^net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(
				net.minecraftforge.fml.IExtensionPoint.DisplayTest.class,
				net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORE_ALL_VERSION);
		^///?} else if >=1.17 && <1.18.2 {
		/^// What DisplayTest.IGNORE_ALL_VERSION (only since 1.18.2) stands for.
		net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(
				net.minecraftforge.fml.IExtensionPoint.DisplayTest.class,
				() -> new net.minecraftforge.fml.IExtensionPoint.DisplayTest(
						() -> net.minecraftforge.network.NetworkConstants.IGNORESERVERONLY, (remote, isServer) -> true));
		^///?} else if <1.17 {
		/^// Before 1.17 the same as a pair of version supplier and version test.
		net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fml.ExtensionPoint.DISPLAYTEST,
				() -> org.apache.commons.lang3.tuple.Pair.of(
						() -> net.minecraftforge.fml.network.FMLNetworkConstants.IGNORESERVERONLY, (remote, isServer) -> true));
		^///?}
		if (FMLEnvironment.dist == Dist.CLIENT) {
			// Since 1.21.6 (EventBus 7) the mod's events live in a group of buses instead of one bus.
			//? if >=1.21.6 {
			BetterMusicToastClient.initForge(FMLJavaModLoadingContext.get().getModBusGroup());
			//?} else {
			/^BetterMusicToastClient.initForge(FMLJavaModLoadingContext.get().getModEventBus());
			^///?}
		}
	}
}
*///?}
