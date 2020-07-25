package rikka.lanserverproperties.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import rikka.lanserverproperties.fabric.ILSPScreen;

@Mixin(Screen.class)
public abstract class MixinScreen implements ILSPScreen {
	@Shadow
	protected abstract <T extends AbstractButtonWidget> T addButton(T button);

	@Shadow
	protected abstract <T extends Element> T addChild(T child);

	@Shadow
	protected TextRenderer textRenderer;

	@Override
	public <T extends AbstractButtonWidget> T LSP$addButton(T button) {
		return addButton(button);
	}

	@Override
	public TextRenderer LSP$getTextRenderer() {
		return textRenderer;
	}

	@Override
	public <T extends Element> T LSP$addChild(T child) {
		return addChild(child);
	}
}
