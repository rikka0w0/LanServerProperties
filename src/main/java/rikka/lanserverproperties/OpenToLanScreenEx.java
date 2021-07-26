package rikka.lanserverproperties;

import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class OpenToLanScreenEx {
	private final static int defaultPort = 25565;
	private final static String onlineModeLangKey = "lanserverproperties.gui.online_mode";
	private final static String onlinemodeDescLangKey = "lanserverproperties.gui.online_mode_desc";
	private final static String portLangKey = "lanserverproperties.gui.port";
	private final static Component onlinemodeDescTooltip = new TranslatableComponent(onlinemodeDescLangKey);
	private final static Component portDescLabel = new TranslatableComponent(portLangKey);

	private final static ThreadLocal<Stack<ReferenceHolder>> references = ThreadLocal.withInitial(Stack::new);

	private static ReferenceHolder peekReference() {
		if (references.get().empty()) {
			System.err.println("[LSP] OpenToLanScreenEx.references is not balanced!");
			return null;
		} else {
			return references.get().peek();
		}
	}

	private static ReferenceHolder popReference() {
		if (references.get().empty()) {
			System.err.println("[LSP] OpenToLanScreenEx.references is not balanced!");
			return null;
		} else {
			return references.get().pop();
		}
	}

	private static void pushReference(ReferenceHolder ref) {
		if (ref == null) {
			System.err.println("[LSP] References is invalid!");
		} else {
			references.get().push(ref);
		}
	}

	private static class ReferenceHolder extends InvisibleWidgetGroup {
		public ToggleButton onlineModeButton;
		public IPAddressTextField tfwPort;
	}

	private static Component getOnlineButtonText(boolean onlineMode) {
		return new TextComponent(I18n.get(onlineModeLangKey) + ": "
				+ I18n.get(onlineMode ? "options.on" : "options.off"));
	}

	/**
	 * Forge: GuiScreenEvent.InitGuiEvent.Post
	 */
	public static void init(Screen gui, Font textRenderer, List<? extends GuiEventListener> list, Consumer<GuiEventListener> widgetAdder) {
		ReferenceHolder group = new ReferenceHolder();
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
	public static void postDraw(Screen gui, Font textRenderer, PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		Screen.drawString(matrixStack, textRenderer, portDescLabel, gui.width / 2 - 155, gui.height - 66, 10526880);
	}

	/**
	 *  Mixin/ Coremod callback
	 */
	public static int getServerPort() {
		ReferenceHolder ref = peekReference();

		if (ref != null) {
			return ref.tfwPort.getServerPort();
		}

		return defaultPort;
	}

	/**
	 *  Mixin/ Coremod callback
	 */
	public static void onOpenToLanClicked() {
		@SuppressWarnings("resource")
		Screen gui = Minecraft.getInstance().screen;
		ReferenceHolder ref = InvisibleWidgetGroup.fromScreen(gui, ReferenceHolder.class);
		pushReference(ref);
	}

	/**
	 *  Mixin/ Coremod callback
	 */
	public static void onOpenToLanClosed() {
		ReferenceHolder ref = popReference();
		IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();

		if (ref != null && server != null) {
			boolean onlineMode = ref.onlineModeButton.getState();
			server.setUsesAuthentication(onlineMode);
		}
	}
}
