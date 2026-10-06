package de.bettermusictoast.core;

import java.util.Map;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

/**
 * A tiny core mod with a single change to Minecraft (see {@link PauseMenuTransformer}). Minecraft
 * 1.7.10 has no Mixin library of its own (modpacks add one, e.g. UniMixins); a core mod does the one
 * change the mod needs without shipping one, so it cannot clash with them.
 */
@IFMLLoadingPlugin.MCVersion("1.7.10")
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
