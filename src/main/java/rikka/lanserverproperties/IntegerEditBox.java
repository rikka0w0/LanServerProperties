package rikka.lanserverproperties;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class IntegerEditBox extends EditBox implements TooltipAccessor {
	private final Function<IntegerEditBox, List<FormattedCharSequence>> toolTipSupplier;
	private boolean contentValid;

	public IntegerEditBox(Font textRenderer, int x, int y, int width, int height, Component name, int defaultVal,
			Consumer<IntegerEditBox> onChanged,
			Function<String, Boolean> validator,
			Function<IntegerEditBox, List<FormattedCharSequence>> toolTipSupplier) {
		super(textRenderer, x, y, width, height, name);
		this.toolTipSupplier = toolTipSupplier == null ? (dummy) -> ImmutableList.of() : toolTipSupplier;
		this.setValue(String.valueOf(defaultVal));
		// Check the format, make sure the text is a valid integer
		this.setResponder((text) -> {
			this.contentValid = validator.apply(text);
			this.setTextColor(this.contentValid ? 0xFFFFFF : 0xFF0000);
			onChanged.accept(this);
		});
		this.contentValid = validator.apply(this.getValue());
	}

	/**
	 * This function assumes that the context is a valid number.
	 * @return the port number as an integer
	 */
	public int getValueAsInt() {
		return Integer.parseInt(this.getValue());
	}

	public boolean isContentValid() {
		return this.contentValid;
	}

	/**
	 * @param min minimum accepted value, inclusive
	 * @param max maximum accepted value, inclusive
	 * @return validator instance
	 */
	public static Function<String, Boolean> makeValidator(int min, int max) {
		return (text) -> {
			boolean valid = true;
			try {
				if (text.length() > 0) {
					int port = Integer.parseInt(text);
					if (port < min || port > max)
						valid = false;
				} else {
					valid = false;
				}
			} catch (NumberFormatException e) {
				valid = false;
			}

			return valid;
		};
	}

	@Override
	public List<FormattedCharSequence> getTooltip() {
		List<FormattedCharSequence> ret = this.toolTipSupplier.apply(this);
		return ret == null ? ImmutableList.of() : ret;
	}
}
