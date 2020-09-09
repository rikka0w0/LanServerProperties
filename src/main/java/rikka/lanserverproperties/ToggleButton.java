package rikka.lanserverproperties;

import java.util.function.Function;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ToggleButton extends ButtonWidget {
	private final Function<Boolean, Text> textMapper;
	private boolean state;

	public ToggleButton(int x, int y, int width, int height,
			Function<Boolean, Text> textMapper, boolean defaultState,
			TooltipSupplier tooltipSupplier) {
		this(x, y, width, height, textMapper, defaultState, (button) -> {}, tooltipSupplier);
	}

	public ToggleButton(int x, int y, int width, int height,
			Function<Boolean, Text> textMapper, boolean defaultState,
			PressAction onCheck, TooltipSupplier tooltipSupplier) {
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
	public Text getMessage() {
		return textMapper.apply(this.state);
	}
}