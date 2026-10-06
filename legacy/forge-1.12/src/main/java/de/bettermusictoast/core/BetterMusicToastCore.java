package de.bettermusictoast.core;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

/**
 * A tiny core mod with a single change to Minecraft (see {@link PauseMenuTransformer}). Minecraft
 * 1.9.4 to 1.12.2 have no Mixin library of their own; a core mod does the one change the mod needs
 * without shipping one, so it cannot clash with other mods' Mixin versions.
 */
// No MCVersion: one jar can cover several patch versions (Forge only logs a note about it).
@IFMLLoadingPlugin.Name("Better Music Toast")
// After Forge has renamed Minecraft's methods, so the transformer sees the same names everywhere.
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions("de.bettermusictoast.core")
public final class BetterMusicToastCore implements IFMLLoadingPlugin {
	@Override
	public String[] getASMTransformerClass() {
		return new String[] {PauseMenuTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
