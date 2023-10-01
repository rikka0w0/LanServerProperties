package rikka.lanserverproperties;

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class CommonWidgets {
	private final static Component preferenceEnabledLabel = Component.translatable("lanserverproperties.options.preference_enabled");
	private final static Component preferenceEnabledTooltip = Component.translatable("lanserverproperties.options.preference_enabled.message");
	private final static Component preferenceLoadLabel = Component.translatable("lanserverproperties.button.preference_load");
	private final static Component preferenceSaveLabel = Component.translatable("lanserverproperties.button.preference_save");
	private final static Component pvpAllowedLabel = Component.translatable("lanserverproperties.gui.pvp_allowed");
	private final static Component maxPlayerDescLabel = Component.translatable("lanserverproperties.gui.max_player");
	private final static Component alwaysOfflineLabel = Component.translatable("lanserverproperties.gui.always_offline");
	private final static Component alwaysOfflineDescLabel = Component.translatable("lanserverproperties.gui.always_offline.message");

	private final static Function<String, Boolean> maxPlayerValidator = IntegerEditBox.makeValidator(0, 16);
	private static final WidgetSprites PAGE_FORWARD_SPRITES = new WidgetSprites(
			new ResourceLocation("recipe_book/page_forward"),
			new ResourceLocation("recipe_book/page_forward_highlighted"));

	private final Button savePreferenceButton;
	private final CycleButton<Boolean> enablePreferenceOption;
	private final CycleButton<OnlineMode> onlineModeOption;
	private final CycleButton<Boolean> pvpAllowedOption;
	private final IntegerEditBox maxPlayerEditBox;
	private final EditBox alwaysOfflinesEditBox;

	private final ConfigContainer configContainer;

	public CommonWidgets(Screen screen, ConfigContainer configContainer, Font textRenderer, Consumer<GuiEventListener> widgetAdder) {
		this.configContainer = configContainer;

		// Save Preference Button
		this.savePreferenceButton = Button.builder(preferenceSaveLabel, (btn) -> {
			configContainer.copyToPreferences();
			configContainer.preferences.save();
		}).bounds(screen.width / 2 - 155, 16, 150, 20).build();
		widgetAdder.accept(this.savePreferenceButton);

		// Enable Preference Button
		this.enablePreferenceOption = CycleButton.onOffBuilder()
			.withInitialValue(configContainer.preferences.enablePreference)
			.withTooltip((curState) -> Tooltip.create(preferenceEnabledTooltip))
			.create(screen.width / 2 + 5, 16, 150, 20, preferenceEnabledLabel,
				(cycleButton, newVal) -> {
					configContainer.preferences.enablePreference = newVal;
				}
			);
		widgetAdder.accept(this.enablePreferenceOption);

		// Toggle button for onlineMode
		this.onlineModeOption = new CycleButton.Builder<OnlineMode>((state) -> state.stateName)
			.withValues(OnlineMode.values())
			.withInitialValue(configContainer.onlineMode).withTooltip((curState) -> Tooltip.create(curState.tooltip))
			.displayOnlyValue()
			.create(screen.width / 2 - 155, 124, 150, 20, OnlineMode.translation,
				(dummyButton, newVal) -> configContainer.onlineMode = newVal);
		widgetAdder.accept(this.onlineModeOption);

		// Toggle button for pvpAllowed
		this.pvpAllowedOption = CycleButton
			.onOffBuilder(configContainer.pvpAllowed)
			.create(screen.width / 2 + 5, 124, 150, 20, pvpAllowedLabel,
				(dummyButton, newVal) -> configContainer.pvpAllowed = newVal);
		widgetAdder.accept(this.pvpAllowedOption);

		// Text field for maxPlayer
		this.maxPlayerEditBox = new IntegerEditBox(textRenderer, screen.width / 2 + 5, screen.height - 54, 147, 20,
			maxPlayerDescLabel, configContainer.maxPlayer, (ieb) -> {
				this.updateButtonStatus();
				if (ieb.isContentValid()) {
					configContainer.maxPlayer = ieb.getValueAsInt();
				}
			}, maxPlayerValidator, null);
		widgetAdder.accept(this.maxPlayerEditBox);

		// Text field for always offline players
		this.alwaysOfflinesEditBox = new EditBox(textRenderer, screen.width / 2 - 180, 146, 360, 20,
				alwaysOfflineLabel);
		this.alwaysOfflinesEditBox.setVisible(false);
		this.alwaysOfflinesEditBox.setTooltip(Tooltip.create(alwaysOfflineDescLabel));
		this.alwaysOfflinesEditBox.setMaxLength(1024);
		this.alwaysOfflinesEditBox.setValue(configContainer.playersAlwaysOffline);
		this.alwaysOfflinesEditBox.setResponder((newValue) -> {
			configContainer.playersAlwaysOffline = newValue;
		});
		widgetAdder.accept(this.alwaysOfflinesEditBox);

		// Button to toggle visibility of the player list
		final SpriteIconButton showAOEButton = SpriteIconButton
				.builder(alwaysOfflineLabel, (button) -> alwaysOfflinesEditBox.visible ^= true, true)
				.width(20)
				.sprite(new ResourceLocation("icon/accessibility"), 15, 15)
				.build();
		showAOEButton.setPosition(screen.width / 2 - 180, 124);
		showAOEButton.setTooltip(Tooltip.create(alwaysOfflineLabel));
		widgetAdder.accept(showAOEButton);

		// Add our own widgets
		// Load Preference Button
		final ImageButton loadPrefButton = new ImageButton(screen.width / 2 - 180, 16, 12, 18,
				PAGE_FORWARD_SPRITES,
				(button) -> {
					configContainer.loadFromPreferences(true);
					syncWidgetValues();
				}, preferenceLoadLabel);
		loadPrefButton.setTooltip(Tooltip.create(preferenceLoadLabel));
		widgetAdder.accept(loadPrefButton);
	}

	public void syncWidgetValues() {
		enablePreferenceOption.setValue(this.configContainer.preferences.enablePreference);
		onlineModeOption.setValue(this.configContainer.onlineMode);
		pvpAllowedOption.setValue(this.configContainer.pvpAllowed);
		maxPlayerEditBox.setValue("" + this.configContainer.maxPlayer);
		alwaysOfflinesEditBox.setValue(this.configContainer.playersAlwaysOffline);
	}

	protected abstract void onButtonStatusUpdated(boolean shouldEnableButtons);

	public void updateButtonStatus() {
		updateButtonStatus(true);
	}

	public void updateButtonStatus(boolean lastPortValidity) {
		boolean shouldEnableButtons = lastPortValidity && this.maxPlayerEditBox.isContentValid();

		Minecraft mc = Minecraft.getInstance();
		if (!mc.hasSingleplayerServer())
			return;

		this.onButtonStatusUpdated(shouldEnableButtons);

		this.savePreferenceButton.active = shouldEnableButtons;
	}
}
