package de.bettermusictoast.mixin;

import net.minecraft.client.gui.GuiIngame;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Record "Now playing" message and held-item name, so the box can move out of their way. */
@Mixin(GuiIngame.class)
public interface GuiIngameAccessor {
	@Accessor("recordPlaying")
	String bettermusictoast$getRecordPlaying();

	@Accessor("recordPlayingUpFor")
	int bettermusictoast$getRecordPlayingUpFor();

	@Accessor("remainingHighlightTicks")
	int bettermusictoast$getRemainingHighlightTicks();

	@Accessor("highlightingItemStack")
	ItemStack bettermusictoast$getHighlightingItemStack();
}
