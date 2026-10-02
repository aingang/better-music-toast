package de.bettermusictoast.mixin;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Hud.class)
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
