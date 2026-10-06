package de.bettermusictoast.config;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

/**
 * "Config" button in Forge's mod list. Forge asks for the screen in two ways: newer builds (late
 * 1.11.2 and 1.12) call createConfigGui, older ones mainConfigGuiClass; before 1.12 both are here,
 * so the button works with every Forge build of a version.
 */
public final class GuiFactory implements IModGuiFactory {
	@Override
	public void initialize(Minecraft minecraft) {
	}

	public boolean hasConfigGui() {
		return true;
	}

	public GuiScreen createConfigGui(GuiScreen parent) {
		return new ConfigScreen(parent);
	}

	//#if MC<11200
	//$$ public Class<? extends GuiScreen> mainConfigGuiClass() {
	//$$ 	return ConfigScreen.class;
	//$$ }
	//$$
	//$$ public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
	//$$ 	return null;
	//$$ }
	//#endif

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}
}
