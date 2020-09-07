package rikka.lanserverproperties;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class OpenToLanScreenEx {
	private final static int defaultPort = 25565;
	private final static String onlineModeLangKey = "lanserverproperties.gui.online_mode";
	private final static String onlinemodeDescLangKey = "lanserverproperties.gui.online_mode_desc";
	private final static String portLangKey = "lanserverproperties.gui.port";
	private final static Text onlinemodeDescTooltip = new TranslatableText(onlinemodeDescLangKey);
	private final static Text portDescLabel = new TranslatableText(portLangKey);

	private static Text getOnlineButtonText(boolean onlineMode) {
		return new LiteralText(I18n.translate(onlineModeLangKey) + ": "
				+ I18n.translate(onlineMode ? "options.on" : "options.off"));
	}

	public static class WidgetGroup extends InvisibleWidgetGroup {
		public ToggleButton onlineModeButton;
		public IPAddressTextField tfwPort;
	}

	/**
	 * Forge: GuiScreenEvent.InitGuiEvent.Post
	 */
	public static void init(Screen gui, TextRenderer textRenderer, List<AbstractButtonWidget> widgets, Consumer<AbstractButtonWidget> widgetAdder) {
		WidgetGroup group = new WidgetGroup();
		widgetAdder.accept(group);

		// Add our own widgets
		// Toggle button for onlineMode
		group.onlineModeButton = 
				new ToggleButton(gui.width / 2 - 155, 124, 150, 20,
					OpenToLanScreenEx::getOnlineButtonText, true, 
					(screen, matrixStack, mouseX, mouseY) -> gui.renderTooltip(matrixStack, onlinemodeDescTooltip, mouseX, mouseY)
				);
		widgetAdder.accept(group.onlineModeButton);

		// Text field for port
		group.tfwPort =
				new IPAddressTextField(textRenderer, gui.width / 2 - 154, gui.height - 54, 147, 20,
					portDescLabel, defaultPort);
		widgetAdder.accept(group.tfwPort);
	}

	/**
	 * Forge: GuiScreenEvent.DrawScreenEvent.Post
	 */
	public static void postDraw(Screen gui, TextRenderer textRenderer, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		Screen.drawTextWithShadow(matrixStack, textRenderer, portDescLabel, gui.width / 2 - 155, gui.height - 66, 10526880);
	}

	public static int getServerPort(Screen gui) {
		WidgetGroup group = WidgetGroup.fromScreen(gui, WidgetGroup.class);

		if (group != null) {
			return group.tfwPort.getServerPort();
		}

		return defaultPort;
	}

	public static void onOpenToLanSuccess(Screen gui) {
		WidgetGroup group = WidgetGroup.fromScreen(gui, WidgetGroup.class);

		if (group != null) {
			boolean onlineMode = group.onlineModeButton.getState();
			MinecraftClient.getInstance().getServer().setOnlineMode(onlineMode);
		}
	}
}
