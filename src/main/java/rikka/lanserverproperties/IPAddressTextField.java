package rikka.lanserverproperties;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class IPAddressTextField extends TextFieldWidget {
	private final int defaultPort;

	public IPAddressTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text name, int defaultPort) {
		super(textRenderer, x, y, width, height, name);
		this.defaultPort = defaultPort;
		this.setText(String.valueOf(defaultPort));
		// Check the format, make sure the text is a valid integer
		this.setChangedListener((text) -> this.setEditableColor(validatePort(text) >= 0 ? 0xFFFFFF : 0xFF0000));
	}

	public int getServerPort() {
		String portStr = getText();
		return portStr.length() > 0 ? Integer.parseInt(portStr) : defaultPort;
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
