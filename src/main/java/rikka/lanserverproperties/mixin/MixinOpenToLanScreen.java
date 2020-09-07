package rikka.lanserverproperties.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import rikka.lanserverproperties.OpenToLanScreenEx;

@Mixin(OpenToLanScreen.class)
public abstract class MixinOpenToLanScreen extends Screen {
	private final static String NetworkUtils_findLocalPort = "net/minecraft/client/util/NetworkUtils.findLocalPort()I";

	protected MixinOpenToLanScreen(Text text) {
		super(text);
	}

	@Redirect(method = "method_19851", at = @At(value = "INVOKE", ordinal = 0, target = NetworkUtils_findLocalPort))
	private int getServerPort() {
		return OpenToLanScreenEx.getServerPort(this);
	}

	@Inject(method = "method_19851",
			slice = @Slice(from = @At(value = "INVOKE", ordinal = 0, target = NetworkUtils_findLocalPort)),
			at = @At(value = "JUMP", ordinal = 0, opcode = Opcodes.IFEQ, shift = Shift.AFTER))
	private void onLanServerStarted(CallbackInfo ci) {
		OpenToLanScreenEx.onOpenToLanSuccess(this);
	}

	@Inject(method = "init", at = @At("TAIL"))
	protected void init_head(CallbackInfo ci) {
		OpenToLanScreenEx.init(this, this.textRenderer, this.buttons, this::addButton);
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		OpenToLanScreenEx.postDraw(this, this.textRenderer, matrixStack, mouseX, mouseY, partialTicks);
	}
}
