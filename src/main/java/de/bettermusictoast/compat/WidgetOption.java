package de.bettermusictoast.compat;

// Only before 1.21, where Minecraft's option lists can only hold options, not other buttons.
//? if >=1.19 && <1.21 {
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
					//? if >=1.19.3 {
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
					//?} else {
					/^// Before 1.19.3 there is no change callback, and the position is a pair of public fields.
					@Override
					public Function<OptionInstance<Boolean>, AbstractWidget> createButton(
							OptionInstance.TooltipSupplier<Boolean> tooltip, Options options, int x, int y, int width) {
						return option -> {
							widget.x = x;
							widget.y = y;
							widget.setWidth(width);
							return widget;
						};
					}
					^///?}

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
*///?} else if <1.19 {
/*import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;

// Before 1.19 options are subclasses of Option, so one that hands out the button is enough.
public final class WidgetOption {
	private WidgetOption() {
	}

	public static Option of(AbstractWidget widget) {
		return new Option("") {
			@Override
			public AbstractWidget createButton(Options options, int x, int y, int width) {
				widget.x = x;
				widget.y = y;
				widget.setWidth(width);
				return widget;
			}
		};
	}
}
*///?}
