package rikka.lanserverproperties;

import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class IntegerEditBox extends EditBox {
	public IntegerEditBox(Font textRenderer, int x, int y, int width, int height, Component name, int defaultPort,
			BiConsumer<IntegerEditBox, Boolean> onChanged, Function<String, Boolean> validator) {
		super(textRenderer, x, y, width, height, name);
		this.setValue(String.valueOf(defaultPort));
		// Check the format, make sure the text is a valid integer
		this.setResponder((text) -> {
			boolean isFormatOk = validator.apply(text);
			this.setTextColor(isFormatOk ? 0xFFFFFF : 0xFF0000);
			onChanged.accept(this, isFormatOk);
		});
	}

	/**
	 * This function assumes that the context is a valid number.
	 * @return the port number as an integer
	 */
	public int getValueAsInt() {
		return Integer.parseInt(getValue());
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
