package rikka.lanserverproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

public class ModifyLanScreen extends Screen {
	private static final Component ALLOW_COMMANDS_LABEL = Component.translatable("selectWorld.allowCommands");
	private static final Component GAME_MODE_LABEL = Component.translatable("selectWorld.gameMode");
	private static final Component INFO_TEXT = Component.translatable("lanServer.otherPlayers");

	private final static Component portListeningLabel = Component.translatable("lanserverproperties.gui.port_listening");
	public final static Component maxPlayerDescLabel = Component.translatable("lanserverproperties.gui.max_player");

	public final Screen lastScreen;
	protected EditBox portEdit;

	private final ConfigContainer.Modded configContainer = new ConfigContainer.Modded();

	protected ModifyLanScreen(Screen lastScreen) {
		super(Component.translatable("lanServer.title"));
		this.lastScreen = lastScreen;
	}

	@Override
	protected void init() {
		IntegratedServer server = this.minecraft.getSingleplayerServer();
		configContainer.loadFromCurrentServer(server);

		CycleButton<GameType> gameModeSelector =
				CycleButton.builder(GameType::getShortDisplayName)
				.withValues(GameType.SURVIVAL, GameType.SPECTATOR, GameType.CREATIVE, GameType.ADVENTURE)
				.withInitialValue(configContainer.getGuiGameType())
				.create(this.width / 2 - 155, 100, 150, 20, GAME_MODE_LABEL, (oldValue, newValue) -> {
					configContainer.setGameType(newValue);
				});
		this.addRenderableWidget(gameModeSelector);

		CycleButton<Boolean> allowCommandsSelector =
				CycleButton.onOffBuilder(configContainer.getGuiCommandEnabled())
				.create(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_LABEL, (oldValue, newValue) -> {
					configContainer.setCommandEnabled(newValue);
				});
		this.addRenderableWidget(allowCommandsSelector);

		// A readonly textbox to display the listening port
		this.portEdit = new EditBox(this.font, this.width / 2 - 154, this.height - 54, 147, 20,
				Component.translatable("lanServer.port"));
		this.portEdit.setEditable(false);
		this.portEdit.setValue(String.valueOf(server.getPort()));
		this.portEdit.setTooltip(null);
		this.portEdit.setResponder(null);
		this.addRenderableWidget(this.portEdit);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_280824_) -> {
			this.minecraft.setScreen(this.lastScreen);
		}).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());

		final Button doneButton = Button.builder(CommonComponents.GUI_DONE,
			(btn) -> {
				this.configContainer.applyToCurrentServer(server);
				Minecraft.getInstance().setScreen(this.lastScreen);
			}
		).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build();
		this.addRenderableWidget(doneButton);

		new CommonWidgets(this, this.configContainer, this.font, this::widgetAdder) {
			@Override
			protected void onButtonStatusUpdated(boolean shouldEnableButtons) {
				doneButton.active = shouldEnableButtons;
			}

			@Override
			public void syncWidgetValues() {
				super.syncWidgetValues();
				allowCommandsSelector.setValue(configContainer.getGuiCommandEnabled());
				gameModeSelector.setValue(configContainer.getGuiGameType());
				portEdit.setValue(String.valueOf(configContainer.getGuiPort()));
			}
		};
	}

	@SuppressWarnings("unchecked")
	private <T extends GuiEventListener & Renderable & NarratableEntry> void widgetAdder(GuiEventListener widget) {
		this.addRenderableWidget((T) widget);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.portEdit != null) {
			this.portEdit.tick();
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int p_96653_, int p_96654_, float p_96655_) {
		this.renderBackground(guiGraphics);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 50, 16777215);
		guiGraphics.drawCenteredString(this.font, INFO_TEXT, this.width / 2, 82, 16777215);
		guiGraphics.drawString(this.font, portListeningLabel, this.width / 2 - 155, this.height - 66, 10526880);
		guiGraphics.drawString(this.font, maxPlayerDescLabel, this.width / 2 + 5, this.height - 66, 10526880);
		super.render(guiGraphics, p_96653_, p_96654_, p_96655_);
	}
}
