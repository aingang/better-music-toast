package de.bettermusictoast.compat;

// Only before 1.21, where Minecraft's option lists can only hold options, not other buttons.
//? if <1.21 {
/*import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

// Wraps one of the mod's own buttons as an option, so it can sit in a vanilla option list. The list
// places and sizes it like any other option button.
public final class WidgetOption {
	private WidgetOption() {
	}

	public static OptionInstance<Boolean> of(AbstractWidget widget) {
		return new OptionInstance<>("", OptionInstance.noTooltip(), (caption, value) -> Component.empty(),
				new OptionInstance.ValueSet<Boolean>() {
					@Override
					public Function<OptionInstance<Boolean>, AbstractWidget> createButton(
							OptionInstance.TooltipSupplier<Boolean> tooltip, Options options, int x, int y, int width,
							Consumer<Boolean> onValueChanged) {
						return option -> {
							widget.setX(x);
							widget.setY(y);
							widget.setWidth(width);
							return widget;
						};
					}

					@Override
					public Optional<Boolean> validateValue(Boolean value) {
						return Optional.of(value);
					}

					@Override
					public Codec<Boolean> codec() {
						return Codec.BOOL;
					}
				}, false, value -> {
				});
	}
}
*///?}
