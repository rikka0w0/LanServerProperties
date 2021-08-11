package rikka.lanserverproperties.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import rikka.lanserverproperties.OpenToLanScreenEx;

@Mixin(PauseScreen.class)
public abstract class MixinPauseScreen extends Screen {
	protected MixinPauseScreen(Component component) {
		super(component);
	}

	@Inject(method = "init", at = @At("TAIL"))
	protected void init_tail(CallbackInfo ci) {
		OpenToLanScreenEx.initPauseScreen(this, this.children());
	}
}
