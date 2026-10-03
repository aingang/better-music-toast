package de.bettermusictoast.mixin;

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Action bar and held-item-name state; on 26.1 these fields still live in Gui. */
//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public interface HudAccessor {
	@Accessor("overlayMessageString")
	Component bettermusictoast$getOverlayMessage();

	@Accessor("overlayMessageTime")
	int bettermusictoast$getOverlayMessageTime();

	@Accessor("toolHighlightTimer")
	int bettermusictoast$getToolHighlightTimer();

	@Accessor("lastToolHighlight")
	ItemStack bettermusictoast$getLastToolHighlight();
}
