package rikka.lanserverproperties.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
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
import net.minecraft.world.level.GameType;

import rikka.lanserverproperties.IShareToLanScreenParamAccessor;
import rikka.lanserverproperties.OpenToLanScreenEx;

@Mixin(ShareToLanScreen.class)
public abstract class MixinOpenToLanScreen extends Screen implements IShareToLanScreenParamAccessor {
	private final static String lambda$init$2 = "method_19851"; // Use lambda$init$2 in dev env
	private final static String HttpUtil_getAvailablePort = "net/minecraft/util/HttpUtil.getAvailablePort()I";

	private OpenToLanScreenEx lsp_objects;

	@Shadow
	@Final
	private Screen lastScreen;
	@Shadow
	private GameType gameMode;
	@Shadow
	private boolean commands;

	@Unique
	private <T extends Widget> void remove(GuiEventListener widget) {
		this.removeWidget(widget);
	}

	@SuppressWarnings("unchecked")
	@Unique
	private final <R extends Widget, G extends GuiEventListener & NarratableEntry> void add(GuiEventListener b) {
		if (b instanceof GuiEventListener) {
			if (b instanceof NarratableEntry) {
				this.addWidget((G) b);
			} else {
				((List<GuiEventListener>)this.children()).add(b);
			}
		}
		if (b instanceof Widget)
			this.addRenderableOnly((R) b);
	}

	protected MixinOpenToLanScreen(Component text) {
		super(text);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void hookConstructor(CallbackInfo ci) {
		this.lsp_objects = new OpenToLanScreenEx((ShareToLanScreen)(Object)this, (IShareToLanScreenParamAccessor)this);
	}

	@Redirect(method = lambda$init$2, at = @At(value = "INVOKE", ordinal = 0, target = HttpUtil_getAvailablePort))
	private int getServerPort() {
		return this.lsp_objects.getServerPort();
	}

	@Inject(method = lambda$init$2, at = @At("TAIL"))
	private void onLanServerStarted(CallbackInfo ci) {
		this.lsp_objects.onOpenToLanClosed();
	}

	@Inject(method = "init", at = @At("TAIL"))
	protected void init_tail(CallbackInfo ci) {
		this.lsp_objects.postInitShareToLanScreen(this.font, this.children(), this::add, this::remove);
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		OpenToLanScreenEx.postDraw(this, this.font, matrixStack, mouseX, mouseY, partialTicks);
	}

	///////////////////////////////////////////////////////////////////
	/// IShareToLanScreenParamAccessor
	///////////////////////////////////////////////////////////////////
	@Override
	public Screen getLastScreen() {
		return this.lastScreen;
	}

	@Override
	public GameType getGameType() {
		return this.gameMode;
	}

	@Override
	public boolean isCommandEnabled() {
		return this.commands;
	}

	@Override
	public void setDefault(GameType gameType, boolean commandEnabled) {
		this.gameMode = gameType;
		this.commands = commandEnabled;
	}
}
