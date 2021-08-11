package rikka.lanserverproperties;

import java.util.function.Function;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ToggleButton extends Button {
	private final Function<Boolean, Component> textMapper;
	private boolean state;

	public final static MutableComponent onLangKey = (new TextComponent(": ")).append(new TranslatableComponent("options.on"));
	public final static MutableComponent offLangKey = (new TextComponent(": ")).append(new TranslatableComponent("options.off"));

	public ToggleButton(int x, int y, int width, int height,
			Component text, boolean defaultState,
			Button.OnTooltip tooltipSupplier) {
		this(x, y, width, height, (state) -> text.copy().append(state ? onLangKey : offLangKey),
				defaultState, (button) -> {}, tooltipSupplier);
	}

	public ToggleButton(int x, int y, int width, int height,
			Function<Boolean, Component> textMapper, boolean defaultState,
			Button.OnTooltip tooltipSupplier) {
		this(x, y, width, height, textMapper, defaultState, (button) -> {}, tooltipSupplier);
	}

	public ToggleButton(int x, int y, int width, int height,
			Function<Boolean, Component> textMapper, boolean defaultState,
			Button.OnPress onCheck, Button.OnTooltip tooltipSupplier) {
		super(x, y, width, height, textMapper.apply(defaultState), onCheck, tooltipSupplier);
		this.textMapper = textMapper;
		this.state = defaultState;
	}

	public boolean getState() {
		return state;
	}

	public boolean setState(boolean newState) {
		boolean oldState = this.state;
		this.state = newState;
		this.onStateChanged(oldState);
		return oldState;
	}

	protected void onStateChanged(boolean oldState) {
	}

	@Override
	public void onPress() {
		setState(!this.state);
		super.onPress();
	}

	@Override
	public Component getMessage() {
		return textMapper.apply(this.state);
	}
}