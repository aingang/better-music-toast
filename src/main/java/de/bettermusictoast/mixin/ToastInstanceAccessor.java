package de.bettermusictoast.mixin;

import net.minecraft.client.gui.components.toasts.Toast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** ToastInstance is package-private, so it is targeted by name. */
@Mixin(targets = "net.minecraft.client.gui.components.toasts.ToastManager$ToastInstance")
public interface ToastInstanceAccessor {
	@Accessor("toast")
	Toast bettermusictoast$getToast();

	@Accessor("firstSlotIndex")
	int bettermusictoast$getFirstSlotIndex();

	@Accessor("visiblePortion")
	float bettermusictoast$getVisiblePortion();
}
