package rikka.lanserverproperties.mixin;

import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;

import rikka.lanserverproperties.OpenToLanScreenEx;

@Mixin(ShareToLanScreen.class)
public abstract class MixinOpenToLanScreen extends Screen {
	private final static String LambdaMethodSignature = "method_19851";
	private final static String NetworkUtils_findLocalPort = "net/minecraft/util/HttpUtil.getAvailablePort()I";

	@Shadow
	private final List<Widget> renderables = null;
	@Shadow
	private final List<GuiEventListener> children = null;
	@Shadow
	private final List<NarratableEntry> narratables = null;

	@Unique
	private final Consumer<GuiEventListener> add = (b) -> {
		if (b instanceof Widget w)
			this.renderables.add(w);
		if (b instanceof NarratableEntry ne)
			this.narratables.add(ne);
		this.children.add(b);
	};

	protected MixinOpenToLanScreen(Component text) {
		super(text);
	}

	@Inject(method = LambdaMethodSignature, at = @At("HEAD"))
	private void onOpenToLanClicked(CallbackInfo ci) {
		OpenToLanScreenEx.onOpenToLanClicked();
	}

	@Redirect(method = LambdaMethodSignature, at = @At(value = "INVOKE", ordinal = 0, target = NetworkUtils_findLocalPort))
	private int getServerPort() {
		return OpenToLanScreenEx.getServerPort();
	}

	@Inject(method = LambdaMethodSignature, at = @At("TAIL"))
	private void onLanServerStarted(CallbackInfo ci) {
		OpenToLanScreenEx.onOpenToLanClosed();
	}

	@Inject(method = "init", at = @At("TAIL"))
	protected void init_head(CallbackInfo ci) {
		OpenToLanScreenEx.init(this, this.font, this.children, add);
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		OpenToLanScreenEx.postDraw(this, this.font, matrixStack, mouseX, mouseY, partialTicks);
	}
}
