package rikka.lanserverproperties.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import rikka.lanserverproperties.OpenToLanScreenEx;

@Mixin(PauseScreen.class)
public abstract class MixinPauseScreen extends Screen {
	protected MixinPauseScreen(Component component) {
		super(component);
	}

	@SuppressWarnings("unchecked")
	@Unique
	private final <R extends Renderable, G extends GuiEventListener & NarratableEntry> void add(GuiEventListener b) {
		if (b instanceof GuiEventListener) {
			if (b instanceof NarratableEntry) {
				this.addWidget((G) b);
			} else {
				((List<GuiEventListener>)this.children()).add(b);
			}
		}
		if (b instanceof Renderable)
			this.addRenderableOnly((R) b);
	}

	@Inject(method = "init", at = @At("TAIL"))
	protected void mimicForge_GuiPostInit(CallbackInfo ci) {
		OpenToLanScreenEx.initPauseScreen(this, this.children(), this::add);
	}
}
