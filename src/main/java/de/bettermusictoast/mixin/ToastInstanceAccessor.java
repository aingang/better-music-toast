package de.bettermusictoast.mixin;

import net.minecraft.client.gui.components.toasts.Toast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
//? if <1.21.2
/*import org.spongepowered.asm.mixin.gen.Invoker;*/

/** ToastInstance is package-private, so it is targeted by name. */
//? if >=1.21.2 {
@Mixin(targets = "net.minecraft.client.gui.components.toasts.ToastManager$ToastInstance")
public interface ToastInstanceAccessor {
	@Accessor("toast")
	Toast bettermusictoast$getToast();

	@Accessor("firstSlotIndex")
	int bettermusictoast$getFirstSlotIndex();

	@Accessor("visiblePortion")
	float bettermusictoast$getVisiblePortion();
}
//?} else {
/*// Before 1.21.2: ToastComponent$ToastInstance, which works out the slide-in progress from the
// current time instead of storing it.
@Mixin(targets = "net.minecraft.client.gui.components.toasts.ToastComponent$ToastInstance")
public interface ToastInstanceAccessor {
	@Accessor("toast")
	Toast bettermusictoast$getToast();

	@Accessor("index")
	int bettermusictoast$getFirstSlotIndex();

	@Invoker("getVisibility")
	float bettermusictoast$getVisibility(long now);
}
*///?}
