package rikka.lanserverproperties;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

public class OpenToLanScreenEx {
	private final static Component lanServerOptionsLabel = Component.translatable("lanserverproperties.button.lan_server_options");
	private final static Component preferenceEnabledLabel = Component.translatable("lanserverproperties.options.preference_enabled");
	private final static Component preferenceEnabledTooltip = Component.translatable("lanserverproperties.options.preference_enabled.message");
	private final static Component preferenceLoadLabel = Component.translatable("lanserverproperties.button.preference_load");
	private final static Component preferenceSaveLabel = Component.translatable("lanserverproperties.button.preference_save");
	private final static Component pvpAllowedLabel = Component.translatable("lanserverproperties.gui.pvp_allowed");
	private final static Component portDescLabel = Component.translatable("lanserverproperties.gui.port");
	private final static Component portListeningLabel = Component.translatable("lanserverproperties.gui.port_listening");
	private final static Component maxPlayerDescLabel = Component.translatable("lanserverproperties.gui.max_player");

	private final static Function<String, Boolean> maxPlayerValidator = IntegerEditBox.makeValidator(0, 16);

	private final ShareToLanScreen screen;
	private final IShareToLanScreenParamAccessor stlParamAccessor;

	private Button startButton = null;
	private boolean lastPortValidity = false;

	private Button doneButton;
	private IntegerEditBox maxPlayerEditBox;
	private Button savePreferenceButton;

	private Preferences preferences;
	private boolean pvpAllowed;
	private OnlineMode onlineMode;
	private int maxPlayer;

	public OpenToLanScreenEx(ShareToLanScreen screen, IShareToLanScreenParamAccessor stlParamAccessor) {
		this.screen = screen;
		this.stlParamAccessor = stlParamAccessor;

		final IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server.isPublished()) { // Show actual state
			// For "this.preferences.enablePreference"
			this.preferences = Preferences.read();

			// Vanilla Configs
			stlParamAccessor.setDefault(server.getForcedGameType(), server.getPlayerList().isAllowCheatsForAllPlayers(),
					server.getPort());

			// LSP Configs
			this.onlineMode = OnlineMode.of(server.usesAuthentication(), UUIDFixer.try_online_first);
			this.pvpAllowed = server.isPvpAllowed();
			this.maxPlayer = server.getMaxPlayers();
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
		stlParamAccessor.setDefault(this.preferences.gameMode, this.preferences.allowCheat,
				this.preferences.defaultPort);

		// LSP Configs
		this.onlineMode = OnlineMode.of(this.preferences.onlineMode, this.preferences.fixUUID);
		this.pvpAllowed = this.preferences.allowPVP;
		this.maxPlayer = this.preferences.maxPlayer;
	}

	private void copyToPreference() {
		// Vanilla Configs
		this.preferences.gameMode = this.stlParamAccessor.getGameType();
		this.preferences.allowCheat = this.stlParamAccessor.isCommandEnabled();

		// LSP Configs
		this.preferences.onlineMode = this.onlineMode.onlineModeEnabled;
		this.preferences.fixUUID = this.onlineMode.tryOnlineUUIDFirst;
		this.preferences.allowPVP = this.pvpAllowed;
		this.preferences.defaultPort = this.stlParamAccessor.getPort();
		this.preferences.maxPlayer = this.maxPlayer;
	}

	private void applyServerConfig(IntegratedServer server, boolean setVanillaOptions) {
		if (setVanillaOptions) {
			server.setDefaultGameType(this.stlParamAccessor.getGameType());
			server.getPlayerList().setAllowCheatsForAllPlayers(this.stlParamAccessor.isCommandEnabled());
		}
		server.setUsesAuthentication(this.onlineMode.onlineModeEnabled);
		server.setPvpAllowed(this.pvpAllowed);
		UUIDFixer.try_online_first = this.onlineMode.tryOnlineUUIDFirst;
		this.stlParamAccessor.setMaxPlayer(this.maxPlayer);
	}

	private static Button findButton(List<? extends GuiEventListener> list, String vanillaLangKey) {
		for (GuiEventListener child: list) {
			if (child instanceof Button) {
				Button button = (Button) child;
				Component component = button.getMessage();
				if (component.getContents() instanceof TranslatableContents &&
						((TranslatableContents)component.getContents()).getKey().equals(vanillaLangKey)) {
					return button;
				}
			}
		}

		return null;
	}

	private void updateButtonStatus() {
		boolean shouldEnableButtons = this.lastPortValidity && maxPlayerEditBox.isContentValid();

		Minecraft mc = Minecraft.getInstance();
		if (!mc.hasSingleplayerServer())
			return;

		final IntegratedServer server = mc.getSingleplayerServer();
		if (server.isPublished()) {
			this.doneButton.active = shouldEnableButtons;
		} else {
			this.startButton.active = shouldEnableButtons;
		}

		this.savePreferenceButton.active = shouldEnableButtons;
	}

	/**
	 * Forge: GuiScreenEvent.InitGuiEvent.Post
	 */
	public void postInitShareToLanScreen(Font textRenderer, List<? extends GuiEventListener> list,
			Consumer<GuiEventListener> widgetAdder, Consumer<GuiEventListener> widgetRemover) {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.hasSingleplayerServer())
			return;

		final IntegratedServer server = mc.getSingleplayerServer();
		this.startButton = findButton(list, "lanServer.start");
		if (server.isPublished()) {
			// Remove the original button if the server is already published
			if (this.startButton != null) {
				widgetRemover.accept(this.startButton);
			}

			this.doneButton = Button.builder(CommonComponents.GUI_DONE,
				(btn) -> {
					this.applyServerConfig(server, true);
					Minecraft.getInstance().setScreen(stlParamAccessor.getLastScreen());
				}
			).bounds(this.screen.width / 2 - 155, this.screen.height - 28, 150, 20).build();

			widgetAdder.accept(this.doneButton);

			stlParamAccessor.setPortEditBoxReadonly("" + server.getPort());
		} else {
			this.doneButton = null;
		}

		// Move the port field
		stlParamAccessor.movePortEditBox(this.screen.width / 2 - 154, this.screen.height - 54, 147, 20);

		// Add our own widgets
		// Load Preference Button
		final ImageButton loadPrefButton = new ImageButton(this.screen.width / 2 - 180, 16, 20, 20, 0, 0, 20,
				new ResourceLocation("textures/gui/accessibility.png"), 32, 64,
				(button) -> {
					this.readFromPreference(true);
					this.screen.init(mc, this.screen.width, this.screen.height);
				}, preferenceLoadLabel);
		loadPrefButton.setTooltip(Tooltip.create(preferenceLoadLabel));
		widgetAdder.accept(loadPrefButton);

		// Save Preference Button
		this.savePreferenceButton = Button.builder(preferenceSaveLabel, (btn) -> {
			this.copyToPreference();
			this.preferences.save();
		}).bounds(this.screen.width / 2 - 155, 16, 150, 20).build();

		widgetAdder.accept(this.savePreferenceButton);

		// Enable Preference Button
		widgetAdder.accept(CycleButton.onOffBuilder()
				.withInitialValue(this.preferences.enablePreference)
				.withTooltip((curState) -> Tooltip.create(preferenceEnabledTooltip))
				.create(this.screen.width / 2 + 5, 16, 150, 20, preferenceEnabledLabel,
					(cycleButton, newVal) -> {
						this.preferences.enablePreference = newVal;
					}
				)
		);

		// Toggle button for onlineMode
		widgetAdder.accept(new CycleButton.Builder<OnlineMode>((state) -> state.stateName)
			.withValues(OnlineMode.values())
			.withInitialValue(this.onlineMode).withTooltip((curState) -> Tooltip.create(curState.tooltip))
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

		// Text field for maxPlayer
		this.maxPlayerEditBox = new IntegerEditBox(textRenderer, this.screen.width / 2 + 5, this.screen.height - 54, 147, 20,
			maxPlayerDescLabel, this.maxPlayer, (ieb) -> {
				this.updateButtonStatus();
				if (ieb.isContentValid()) {
					this.maxPlayer = ieb.getValueAsInt();
				}
			}, maxPlayerValidator, null);
		widgetAdder.accept(this.maxPlayerEditBox);
	}

	/**
	 * Forge: GuiScreenEvent.InitGuiEvent.Post
	 */
	public static void initPauseScreen(Screen gui, List<? extends GuiEventListener> list,
			Consumer<GuiEventListener> widgetAdder) {
		final Minecraft mc = Minecraft.getInstance();
		Button shareToLanButton = findButton(list, "menu.shareToLan");

		if (shareToLanButton != null) {
			shareToLanButton.active = mc.hasSingleplayerServer();
		}

		if (mc.getSingleplayerServer().isPublished()) {
			Button optionButton = findButton(list, "menu.options");

			if (optionButton != null) {
				ImageButton lanServerSettings = new ImageButton(gui.width / 2 - 124, optionButton.getY(), 20, 20, 0, 106, 20,
						Button.WIDGETS_LOCATION, 256, 256, (button) -> mc.setScreen(new ShareToLanScreen(gui)),
						lanServerOptionsLabel);
				lanServerSettings.setTooltip(Tooltip.create(lanServerOptionsLabel));
				widgetAdder.accept(lanServerSettings);
			}
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
				Screen.drawString(matrixStack, textRenderer, portListeningLabel, gui.width / 2 - 155, gui.height - 66, 10526880);
			} else {
				Screen.drawString(matrixStack, textRenderer, portDescLabel, gui.width / 2 - 155, gui.height - 66, 10526880);
			}
			Screen.drawString(matrixStack, textRenderer, maxPlayerDescLabel, gui.width / 2 + 5, gui.height - 66, 10526880);
		}
	}

	/**
	 *  Mixin/ Coremod callback
	 */
	public void onPortEditBoxChanged() {
		/**
		 * This callback is only supposed to be called when the port changes.
		 * The vanilla responder sets the state of the button just before entering this callback.
		 * So we assume the state of the button represents the validity of port field.
		 */
		if (Minecraft.getInstance().getSingleplayerServer().isPublished()) {
			this.lastPortValidity = true;
		} else {
			this.lastPortValidity = this.startButton.active;
			this.updateButtonStatus();
		}
	}

	/**
	 *  Mixin/ Coremod callback
	 */
	public int getDefaultPort() {
		return this.preferences.defaultPort;
	}

	/**
	 *  Mixin/ Coremod callback
	 */
	public void onOpenToLanClosed() {
		IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
		applyServerConfig(server, false);
	}
}
