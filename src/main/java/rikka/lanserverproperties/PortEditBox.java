package rikka.lanserverproperties;

import java.util.function.BiConsumer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class PortEditBox extends EditBox {
	public PortEditBox(Font textRenderer, int x, int y, int width, int height, Component name, int defaultPort, BiConsumer<PortEditBox, Boolean> onChanged) {
		super(textRenderer, x, y, width, height, name);
		this.setValue(String.valueOf(defaultPort));
		// Check the format, make sure the text is a valid integer
		this.setResponder((text) -> {
			boolean isFormatOk = validatePort(text) >= 0;
			this.setTextColor(isFormatOk ? 0xFFFFFF : 0xFF0000);
			onChanged.accept(this, isFormatOk);
		});
	}

	/**
	 * This function assumes that the context is a valid port number.
	 * @return the port number as an integer
	 */
	public int getServerPort() {
		return Integer.parseInt(getValue());
	}

	/**
	 * @param text
	 * @return negative if port is invalid, otherwise the port number
	 */
	public static int validatePort(String text) {
		boolean valid = true;
		int port = -1;
		try {
			if (text.length() > 0) {
				port = Integer.parseInt(text);
				if (port < 0 || port > 65535)
					valid = false;
			}
		} catch (NumberFormatException e) {
			valid = false;
		}

		return valid ? port : -1;
	}
}
