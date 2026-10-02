package de.bettermusictoast.mixin;

import java.util.List;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ToastManager.class)
public interface ToastManagerAccessor {
	/** Elements are ToastManager$ToastInstance; cast them to {@link ToastInstanceAccessor}. */
	@Accessor("visibleToasts")
	List<?> bettermusictoast$getVisibleToasts();
}
