package rikka.lanserverproperties;

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class IntegerEditBox extends EditBox {
	private boolean contentValid;

	public IntegerEditBox(Font textRenderer, int x, int y, int width, int height, Component name, int defaultVal,
			Consumer<IntegerEditBox> onChanged,
			Function<String, Boolean> validator,
			Component toolTipComponent) {
		super(textRenderer, x, y, width, height, name);

		if (toolTipComponent != null) {
			this.setTooltip(Tooltip.create(toolTipComponent));
		}

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
	 *
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
}
