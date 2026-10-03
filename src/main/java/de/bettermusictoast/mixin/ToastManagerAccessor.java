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
//?} else {
/*@Mixin(ToastComponent.class)
public interface ToastManagerAccessor {
	@Accessor("visible")
*///?}
	List<?> bettermusictoast$getVisibleToasts();
}
