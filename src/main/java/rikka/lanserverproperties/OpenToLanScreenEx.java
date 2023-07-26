package rikka.lanserverproperties;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.GameType;

public class OpenToLanScreenEx {
	private final static Component lanServerOptionsLabel = Component.translatable("lanserverproperties.button.lan_server_options");
	private final static Component portDescLabel = Component.translatable("lanserverproperties.gui.port");

	private final ShareToLanScreen screen;
	private final IShareToLanScreenParamAccessor stlParamAccessor;

	private Button startButton = null;

	private ConfigContainer configContainer;
	private CommonWidgets commonWidgets;

	public OpenToLanScreenEx(ShareToLanScreen screen, IShareToLanScreenParamAccessor stlParamAccessor) {
		this.screen = screen;
		this.stlParamAccessor = stlParamAccessor;

		this.configContainer = new ConfigContainer.Vanilla(stlParamAccessor);
	}

	@SuppressWarnings("unchecked")
	private static <T extends AbstractWidget> T findWidget(List<? extends GuiEventListener> list, Class<T> cls, String vanillaLangKey) {
		for (GuiEventListener child: list) {
			if (!(child instanceof AbstractWidget))
				continue;

			AbstractWidget widget = (AbstractWidget) child;
			// We only look for AbstractWidget
			if (cls.isAssignableFrom(widget.getClass())) {
				Component component = widget.getMessage();
				if (component.getContents() instanceof TranslatableContents) {
					TranslatableContents content = (TranslatableContents) component.getContents();
					if (content.getKey().equals(vanillaLangKey)) {
						return (T) widget;
					} else {
						Object[] args = content.getArgs();
						if (args.length == 0)
							continue;

						if (!(args[0] instanceof MutableComponent))
							continue;

						MutableComponent mutableComponent = (MutableComponent) args[0];
						if (!(component.getContents() instanceof TranslatableContents))
							continue;

						if (!(mutableComponent.getContents() instanceof TranslatableContents))
							continue;

						content = (TranslatableContents) mutableComponent.getContents();
						if (content.getKey().equals(vanillaLangKey)) {
							return (T) widget;
						}
					}
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
		if (!mc.hasSingleplayerServer())
			return;

		this.configContainer.loadFromPreferences(false);

		// Find the "Start Lan Server" button
		this.startButton = findWidget(list, Button.class, "lanServer.start");

		// Set the widget displays from the configContainer
		@SuppressWarnings("unchecked")
		CycleButton<Boolean> allowCommandsSelector = findWidget(list, CycleButton.class, "selectWorld.allowCommands");
		@SuppressWarnings("unchecked")
		CycleButton<GameType> gameModeSelector = findWidget(list, CycleButton.class, "selectWorld.gameMode");
		EditBox portEdit = findWidget(list, EditBox.class, "lanServer.port");

		this.commonWidgets = new CommonWidgets(screen, configContainer, textRenderer, widgetAdder) {
			@Override
			protected void onButtonStatusUpdated(boolean shouldEnableButtons) {
				startButton.active = shouldEnableButtons;
			}

			@Override
			public void syncWidgetValues() {
				super.syncWidgetValues();
				allowCommandsSelector.setValue(configContainer.getGuiCommandEnabled());
				gameModeSelector.setValue(configContainer.getGuiGameType());
				portEdit.setValue(String.valueOf(configContainer.getGuiPort()));
			}
		};

		this.commonWidgets.syncWidgetValues();

		// Move the port field
		stlParamAccessor.movePortEditBox(this.screen.width / 2 - 154, this.screen.height - 54, 147, 20);
	}

	/**
	 * Forge: GuiScreenEvent.InitGuiEvent.Post
	 */
	public static void initPauseScreen(Screen gui, List<? extends GuiEventListener> list,
			Consumer<GuiEventListener> widgetAdder) {
		final Minecraft mc = Minecraft.getInstance();
		Button shareToLanButton = findWidget(list, Button.class, "menu.shareToLan");

		if (shareToLanButton != null) {
			shareToLanButton.active = mc.hasSingleplayerServer();
		}

		// If there is a published server, add a new button to the pause screen.
		if (mc.hasSingleplayerServer() && mc.getSingleplayerServer().isPublished()) {
			Button optionButton = findWidget(list, Button.class, "menu.options");

			if (optionButton != null) {
				ImageButton lanServerSettings = new ImageButton(gui.width / 2 - 124, optionButton.getY(), 20, 20, 0, 106, 20,
						Button.WIDGETS_LOCATION, 256, 256, (button) -> mc.setScreen(new ModifyLanScreen(gui)),
						lanServerOptionsLabel);
				lanServerSettings.setTooltip(Tooltip.create(lanServerOptionsLabel));
				widgetAdder.accept(lanServerSettings);
			}
		}
	}

	/**
	 * Forge: GuiScreenEvent.DrawScreenEvent.Post
	 */
	public static void postDraw(Screen gui, Font textRenderer, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.hasSingleplayerServer()) {
			guiGraphics.drawString(textRenderer, portDescLabel, gui.width / 2 - 155, gui.height - 66, 10526880);
			guiGraphics.drawString(textRenderer, ModifyLanScreen.maxPlayerDescLabel, gui.width / 2 + 5, gui.height - 66, 10526880);
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
		this.commonWidgets.updateButtonStatus(this.startButton.active);
	}

	/**
	 *  Mixin/ Coremod callback
	 */
	public int getDefaultPort() {
		return this.configContainer.preferences.defaultPort;
	}

	/**
	 *  Mixin/ Coremod callback
	 */
	public void onOpenToLanClosed() {
		IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
		this.configContainer.applyToCurrentServer(server);
	}
}
