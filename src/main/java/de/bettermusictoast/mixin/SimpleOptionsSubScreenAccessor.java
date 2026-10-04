package de.bettermusictoast.mixin;

// Only before 1.19.3, where the option list of SimpleOptionsSubScreen is private (see ConfigScreen).
//? if <1.19.3 {
/*import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleOptionsSubScreen.class)
public interface SimpleOptionsSubScreenAccessor {
	@Accessor("list")
	OptionsList bettermusictoast$getList();
}
*///?}
