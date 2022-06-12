package rikka.lanserverproperties;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class OpenToLanScreenEx {
	private final static TranslatableComponent preferenceEnabledLabel = new TranslatableComponent("lanserverproperties.options.preference_enabled");
	private final static TranslatableComponent preferenceEnabledTooltip = new TranslatableComponent("lanserverproperties.options.preference_enabled.message");
	private final static TranslatableComponent preferenceLoadLabel = new TranslatableComponent("lanserverproperties.button.preference_load");
	private final static TranslatableComponent preferenceSaveLabel = new TranslatableComponent("lanserverproperties.button.preference_save");
	private final static TranslatableComponent pvpAllowedLabel = new TranslatableComponent("lanserverproperties.gui.pvp_allowed");
	private final static TranslatableComponent portDescLabel = new TranslatableComponent("lanserverproperties.gui.port");
	private final static TranslatableComponent ip4ListeningLabel = new TranslatableComponent("lanserverproperties.gui.ip4_listening");

	private final ShareToLanScreen screen;
	private final IShareToLanScreenParamAccessor stlParamAccessor;
	private Preferences preferences;
	private boolean pvpAllowed;
	private OnlineMode onlineMode;
	private int port;

	public OpenToLanScreenEx(ShareToLanScreen screen, IShareToLanScreenParamAccessor stlParamAccessor) {
		this.screen = screen;
		this.stlParamAccessor = stlParamAccessor;

		final IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server.isPublished()) { // Show actual state
			// Vanilla Configs
			stlParamAccessor.setDefault(server.getForcedGameType(), server.getPlayerList().isAllowCheatsForAllPlayers());

			// LSP Configs
			this.onlineMode = OnlineMode.of(server.usesAuthentication(), UUIDFixer.try_online_first);
			this.pvpAllowed = server.isPvpAllowed();
			this.port = server.getPort();
		} else {
			readFromPreference(false);
		}
	}

	private void readFromPreference(boolean forceLoad) {
		this.preferences = Preferences.read();
		if (!forceLoad && !this.preferences.enablePreference) {
			this.preferences = new Preferences();
		}

		// Vanilla Configs
		stlParamAccessor.setDefault(this.preferences.gameMode, this.preferences.allowCheat);

		// LSP Configs
		this.onlineMode = OnlineMode.of(this.preferences.onlineMode, this.preferences.fixUUID);
		this.pvpAllowed = this.preferences.allowPVP;
		this.port = this.preferences.defaultPort;
	}

	private void copyToPreference() {
		// Vanilla Configs
		this.preferences.gameMode = this.stlParamAccessor.getGameType();
		this.preferences.allowCheat = this.stlParamAccessor.isCommandEnabled();

		// LSP Configs
		this.preferences.onlineMode = this.onlineMode.onlineModeEnabled;
		this.preferences.fixUUID = this.onlineMode.tryOnlineUUIDFirst;
		this.preferences.allowPVP = this.pvpAllowed;
		this.preferences.defaultPort = this.port;
	}

	private void applyServerConfig(IntegratedServer server, boolean setVanillaOptions) {
		if (setVanillaOptions) {
			server.setDefaultGameType(stlParamAccessor.getGameType());
			server.getPlayerList().setAllowCheatsForAllPlayers(stlParamAccessor.isCommandEnabled());
		}
		server.setUsesAuthentication(this.onlineMode.onlineModeEnabled);
		server.setPvpAllowed(this.pvpAllowed);
		UUIDFixer.try_online_first = this.onlineMode.tryOnlineUUIDFirst;
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
	 * Forge: GuiScreenEvent.InitGuiEvent.Post
	 */
	public void postInitShareToLanScreen(Font textRenderer, List<? extends GuiEventListener> list,
			Consumer<GuiEventListener> widgetAdder, Consumer<GuiEventListener> widgetRemover) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.hasSingleplayerServer()) {
			final IntegratedServer server = mc.getSingleplayerServer();
			final Button openToLanButton = findButton(list, "lanServer.start");
			if (server.isPublished()) {
				// Remove the original button if the server is already published
				if (openToLanButton != null) {
					widgetRemover.accept(openToLanButton);
				}

				widgetAdder.accept(new Button(this.screen.width / 2 - 155, this.screen.height - 28, 150, 20,
						new TranslatableComponent("gui.done"),
						(btn) -> {
							this.applyServerConfig(server, true);
							Minecraft.getInstance().setScreen(stlParamAccessor.getLastScreen());
						}));
			} else {
				// Text field for port
				widgetAdder.accept(new PortEditBox(textRenderer, this.screen.width / 2 - 154, this.screen.height - 54, 147, 20,
						portDescLabel, this.port, (portField, isFormatOk) -> {
							openToLanButton.active = isFormatOk;
							if (isFormatOk) {
								this.port = portField.getServerPort();
							}
						}));
			}

			// Add our own widgets
			// Load Preference Button
			widgetAdder.accept(
				new ImageButton(this.screen.width / 2 - 180, 16, 20, 20, 0, 0, 20,
						new ResourceLocation("textures/gui/accessibility.png"), 32, 64,
						(btn) -> {
							this.readFromPreference(true);
							this.screen.init(mc, this.screen.width, this.screen.height);
						},
						(btn, poseStack, x, y) -> {
							this.screen.renderTooltip(poseStack, textRenderer.split(preferenceLoadLabel, 200), x, y);
						},
						preferenceLoadLabel));

			// Save Preference Button
			widgetAdder.accept(
				new Button(this.screen.width / 2 - 155, 16, 150, 20, preferenceSaveLabel,
					(btn) -> {
						this.copyToPreference();
						this.preferences.save();
					}));

			// Enable Preference Button
			widgetAdder.accept(CycleButton
					.onOffBuilder(this.preferences.enablePreference)
					.withTooltip((curState) -> textRenderer.split(preferenceEnabledTooltip, 200))
					.create(this.screen.width / 2 + 5, 16, 150, 20, preferenceEnabledLabel,
							(dummyButton, newVal) -> this.preferences.enablePreference = newVal)
				);

			// Toggle button for onlineMode
			widgetAdder.accept(new CycleButton.Builder<OnlineMode>((state) -> state.stateName)
				.withValues(OnlineMode.values())
				.withInitialValue(this.onlineMode).withTooltip((curState) -> textRenderer.split(curState.tooltip, 200))
				.displayOnlyValue()
				.create(this.screen.width / 2 - 155, 124, 150, 20, OnlineMode.translation,
						(dummyButton, newVal) -> this.onlineMode = newVal)
			);

			// Toggle button for pvpAllowed
			widgetAdder.accept(CycleButton
				.onOffBuilder(this.pvpAllowed)
				.create(this.screen.width / 2 + 5, 124, 150, 20, pvpAllowedLabel,
						(dummyButton, newVal) -> this.pvpAllowed = newVal)
			);
		}
	}

	/**
	 * Forge: GuiScreenEvent.InitGuiEvent.Post
	 */
	public static void initPauseScreen(Screen gui, List<? extends GuiEventListener> list) {
		Button shareToLanButton = findButton(list, "menu.shareToLan");
		if (shareToLanButton != null) {
			shareToLanButton.active = Minecraft.getInstance().hasSingleplayerServer();
		}
	}

	/**
	 * Forge: GuiScreenEvent.DrawScreenEvent.Post
	 */
	public static void postDraw(Screen gui, Font textRenderer, PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.hasSingleplayerServer()) {
			final IntegratedServer server = mc.getSingleplayerServer();
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

		Optional<GuiEventListener> mouserOverControl = gui.getChildAt(mouseX, mouseY);
		if (mouserOverControl.isPresent() && mouserOverControl.get() instanceof TooltipAccessor) {
			List<FormattedCharSequence> tooltips = ((TooltipAccessor)(mouserOverControl.get())).getTooltip();
			gui.renderTooltip(matrixStack, tooltips, mouseX, mouseY);
		}
	}

	/**
	 *  Mixin/ Coremod callback
	 */
	public int getServerPort() {
		return this.port;
	}

	/**
	 *  Mixin/ Coremod callback
	 */
	public void onOpenToLanClosed() {
		IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
		applyServerConfig(server, false);
	}
}
