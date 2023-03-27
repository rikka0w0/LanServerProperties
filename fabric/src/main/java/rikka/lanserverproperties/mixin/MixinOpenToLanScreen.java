package rikka.lanserverproperties.mixin;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;

import rikka.lanserverproperties.IShareToLanScreenParamAccessor;
import rikka.lanserverproperties.OpenToLanScreenEx;

@Mixin(ShareToLanScreen.class)
public abstract class MixinOpenToLanScreen extends Screen implements IShareToLanScreenParamAccessor {
	private OpenToLanScreenEx lsp_objects;

	@Shadow
	@Final
	private Screen lastScreen;
	@Shadow
	private GameType gameMode;
	@Shadow
	private int port;
	@Shadow
	private EditBox portEdit;
	@Shadow
	private boolean commands;

	@Unique
	private void remove(GuiEventListener widget) {
		this.removeWidget(widget);
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

	protected MixinOpenToLanScreen(Component text) {
		super(text);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void patchInit_ShareToLanScreen(CallbackInfo ci) {
		this.lsp_objects = new OpenToLanScreenEx((ShareToLanScreen)(Object)this, (IShareToLanScreenParamAccessor)this);
	}

	@Inject(method = "method_19850", at = @At("TAIL"))
	private void patch_startButton_OnClick(CallbackInfo ci) {
		this.lsp_objects.onOpenToLanClosed();
	}

	@Inject(method = "method_47416", at = @At("TAIL"))
	private void patch_portEditBox_OnChange(CallbackInfo ci) {
		this.lsp_objects.onPortEditBoxChanged();
	}

	@Redirect(method = "tryParsePort",
			at = @At(value = "INVOKE", target = "net/minecraft/util/HttpUtil.getAvailablePort()I"))
	private int redirect_tryParsePort() {
		return this.lsp_objects.getDefaultPort();
	}

	@Redirect(method = "render",
			slice = @Slice(from = @At(value = "FIELD", ordinal = 0, opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/client/gui/screens/ShareToLanScreen;PORT_INFO_TEXT:Lnet/minecraft/network/chat/Component;")),
			at = @At(value = "INVOKE", target = "net/minecraft/client/gui/screens/ShareToLanScreen.drawCenteredString(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V")
	)
	private void bypass_render_PortText(PoseStack poseStack, Font font, Component component, int i, int j, int k) {
	}

	@Inject(method = "init", at = @At("TAIL"))
	protected void mimicForge_GuiPostInit(CallbackInfo ci) {
		this.lsp_objects.postInitShareToLanScreen(this.font, this.children(), this::add, this::remove);
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void mimicForge_GuiPostRender(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
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
	public void setDefault(GameType gameType, boolean commandEnabled, int port) {
		this.gameMode = gameType;
		this.commands = commandEnabled;
		this.port = port;
	}

	@Override
	public void setMaxPlayer(int num) {
		PlayerList playerList = Minecraft.getInstance().getSingleplayerServer().getPlayerList();
		((PlayerListAccessor)playerList).setMaxPlayers(num);
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public void movePortEditBox(int x, int y, int width, int height) {
		this.portEdit.setPosition(x, y);
		this.portEdit.setWidth(width);
		((WidgetHeightAccessor) this.portEdit).setHeight(height);
	}

	@Override
	public void setPortEditBoxReadonly(String value) {
		this.portEdit.setEditable(false);
		this.portEdit.setValue(value);
		this.portEdit.setTooltip(null);
		this.portEdit.setResponder(null);
	}
}
