package rikka.lanserverproperties;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class OpenToLanScreenEx {
	private final static boolean defaultOnlineMode = true;
	private final static boolean defaultPvpAllowed = true;
	private final static int defaultPort = 25565;
	private final static TranslatableComponent onlinemodeLabel = new TranslatableComponent("lanserverproperties.gui.online_mode");
	private final static TranslatableComponent onlinemodeDescTooltip = new TranslatableComponent("lanserverproperties.gui.online_mode_desc");
	private final static TranslatableComponent pvpAllowedLabel = new TranslatableComponent("lanserverproperties.gui.pvp_allowed");
	private final static TranslatableComponent portDescLabel = new TranslatableComponent("lanserverproperties.gui.port");
	private final static TranslatableComponent ip4ListeningLabel = new TranslatableComponent("lanserverproperties.gui.ip4_listening");

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
		public ToggleButton pvpAllowedButton;
		public ToggleButton onlineModeButton;
		public IPAddressTextField tfwPort;
	}

	private static void applyServerConfig(IntegratedServer server, IShareToLanScreenParamAccessor stlParamAccessor, ReferenceHolder ref) {
		if (stlParamAccessor != null) {
			server.setDefaultGameType(stlParamAccessor.getGameType());
			server.getPlayerList().setAllowCheatsForAllPlayers(stlParamAccessor.isCommandEnabled());
		}
		server.setUsesAuthentication(ref.onlineModeButton.getState());
		server.setPvpAllowed(ref.pvpAllowedButton.getState());
	}

	private static Button findButton(List<? extends GuiEventListener> list, String vanillaLangKey) {
		for (GuiEventListener child: list) {
			if (child instanceof Button) {
				Button button = (Button) child;
				Component component = button.getMessage();
				if (component instanceof TranslatableComponent &&
						((TranslatableComponent)component).getKey().equals(vanillaLangKey)) {
					return button;
				}
			}
		}

		return null;
	}

	/**
	 * Forge: GuiScreenEvent.InitGuiEvent.Pre
	 */
	public static void preInitShareToLanScreen(Screen gui, Font textRenderer, List<? extends GuiEventListener> list,
			Consumer<GuiEventListener> widgetAdder, Consumer<GuiEventListener> widgetRemover,
			IShareToLanScreenParamAccessor stlParamAccessor) {
		if (gui.getMinecraft().hasSingleplayerServer()) {
			IntegratedServer server = gui.getMinecraft().getSingleplayerServer();
			if (server.isPublished()) {
				stlParamAccessor.setDefault(server.getForcedGameType(), server.getPlayerList().isAllowCheatsForAllPlayers());
			}
		}
	}

	/**
	 * Forge: GuiScreenEvent.InitGuiEvent.Post
	 */
	public static void postInitShareToLanScreen(Screen gui, Font textRenderer, List<? extends GuiEventListener> list,
			Consumer<GuiEventListener> widgetAdder, Consumer<GuiEventListener> widgetRemover,
			IShareToLanScreenParamAccessor stlParamAccessor) {
		final ReferenceHolder group = new ReferenceHolder();
		widgetAdder.accept(group);

		if (gui.getMinecraft().hasSingleplayerServer()) {
			final IntegratedServer server = gui.getMinecraft().getSingleplayerServer();
			if (server.isPublished()) {
				// Remove the original button if the server is already published
				Button openToLanButton = findButton(list, "lanServer.start");
				if (openToLanButton != null) {
					widgetRemover.accept(openToLanButton);
				}

				openToLanButton = new Button(gui.width / 2 - 155, gui.height - 28, 150, 20,
						new TranslatableComponent("gui.done"),
						(btn) -> {
							applyServerConfig(server, stlParamAccessor, group);
							gui.getMinecraft().setScreen(stlParamAccessor.getLastScreen());
						});
				widgetAdder.accept(openToLanButton);
			} else {
				// Text field for port
				group.tfwPort =
						new IPAddressTextField(textRenderer, gui.width / 2 - 154, gui.height - 54, 147, 20,
								portDescLabel, defaultPort);
				widgetAdder.accept(group.tfwPort);
			}

			// Add our own widgets
			// Toggle button for onlineMode
			group.onlineModeButton =
					new ToggleButton(gui.width / 2 - 155, 124, 150, 20,
							onlinemodeLabel, server.isPublished() ? server.usesAuthentication() : defaultOnlineMode,
							(screen, matrixStack, mouseX, mouseY) -> gui.renderTooltip(matrixStack, onlinemodeDescTooltip, mouseX, mouseY)
							);
			widgetAdder.accept(group.onlineModeButton);

			// Toggle button for pvpAllowed
			group.pvpAllowedButton =
					new ToggleButton(gui.width / 2 + 5, 124, 150, 20,
							pvpAllowedLabel, server.isPublished() ? server.isPvpAllowed() : defaultPvpAllowed,
									(screen, matrixStack, mouseX, mouseY) -> {});
			widgetAdder.accept(group.pvpAllowedButton);
		}
	}

	/**
	 * Forge: GuiScreenEvent.InitGuiEvent.Post
	 */
	public static void initPauseScreen(Screen gui, Font textRenderer, List<? extends GuiEventListener> list,
			Consumer<GuiEventListener> widgetAdder, Consumer<GuiEventListener> widgetRemover) {
		Button shareToLanButton = findButton(list, "menu.shareToLan");
		if (shareToLanButton != null) {
			shareToLanButton.active = gui.getMinecraft().hasSingleplayerServer();
		}
	}

	/**
	 * Forge: GuiScreenEvent.DrawScreenEvent.Post
	 */
	public static void postDraw(Screen gui, Font textRenderer, PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {

		if (gui.getMinecraft().hasSingleplayerServer()) {
			final IntegratedServer server = gui.getMinecraft().getSingleplayerServer();
			if (server.isPublished()) {
				Screen.drawString(matrixStack, textRenderer, ip4ListeningLabel, gui.width / 2 - 155, gui.height - 66, 10526880);
				try {
					String ipv4 = InetAddress.getLocalHost().toString();
					ipv4 += ":" + String.valueOf(server.getPort());
					Screen.drawString(matrixStack, textRenderer, ipv4, gui.width / 2 - 155, gui.height - 48, 16777215);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Screen.drawString(matrixStack, textRenderer, portDescLabel, gui.width / 2 - 155, gui.height - 66, 10526880);
			}
		}
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
			applyServerConfig(server, null, ref);
		}
	}
}
