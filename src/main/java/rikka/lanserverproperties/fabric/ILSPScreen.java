package rikka.lanserverproperties.fabric;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;

public interface ILSPScreen {
	<T extends AbstractButtonWidget> T LSP$addButton(T button);
	public <T extends Element> T LSP$addChild(T child);
	TextRenderer LSP$getTextRenderer();
}
