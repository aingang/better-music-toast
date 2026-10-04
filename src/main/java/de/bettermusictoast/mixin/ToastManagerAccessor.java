package de.bettermusictoast.mixin;

import java.util.List;
//? if >=1.21.2 {
import net.minecraft.client.gui.components.toasts.ToastManager;
//?} else {
/*import net.minecraft.client.gui.components.toasts.ToastComponent;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Before 1.21.2 the toast manager is called ToastComponent and its list "visible".
//? if >=1.21.2 {
@Mixin(ToastManager.class)
public interface ToastManagerAccessor {
	/** Elements are ToastManager$ToastInstance; cast them to {@link ToastInstanceAccessor}. */
	@Accessor("visibleToasts")
//?} else if >=1.19.1 {
/*@Mixin(ToastComponent.class)
public interface ToastManagerAccessor {
	@Accessor("visible")
*///?} else {
/*// 1.19 keeps the toasts in a fixed array of slots; the class is made public by the access widener.
@Mixin(ToastComponent.class)
public interface ToastManagerAccessor {
	@Accessor("visible")
	ToastComponent.ToastInstance<?>[] bettermusictoast$getVisibleSlots();
}
*///?}
//? if >=1.19.1 {
	List<?> bettermusictoast$getVisibleToasts();
}
//?}
