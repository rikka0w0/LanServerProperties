package rikka.lanserverproperties.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Mixin(OpenToLanScreen.class)
public abstract class MixinOpenToLanScreen extends Screen {
	private final static String onlineModeLangKey = "lanserverproperties.gui.online_mode";
	private final static String onlinemodeDescLangKey = "lanserverproperties.gui.online_mode_desc";
	private final static String portLangKey = "lanserverproperties.gui.port";
	private final static ImmutableList<Text> onlinemodeDescTooltip = ImmutableList.of(new TranslatableText(onlinemodeDescLangKey));

	protected MixinOpenToLanScreen(Text title) {
		super(title);
	}

	@Unique
	private TextFieldWidget tfwPort = null;
	@Unique
	private ButtonWidget onlineModeButton = null;
	@Unique
	private boolean onlineMode = true;

	@Redirect(method = "method_19851", at = @At(value = "INVOKE", ordinal = 0, target = "net/minecraft/client/util/NetworkUtils.findLocalPort()I"))
	private int getServerPort() {
		String portStr = tfwPort.getText();
		return portStr.length() > 0 ? Integer.parseInt(portStr) : 25565;
	}

	@Inject(method = "method_19851", at = @At(value = "NEW", ordinal = 0, shift = Shift.BEFORE, target = "net/minecraft/text/TranslatableText"))
	private void onLanServerStarted(CallbackInfo ci) {
		MinecraftClient.getInstance().getServer().setOnlineMode(onlineMode);
	}

	@Inject(method = "init", at = @At("HEAD"))
	protected void init_head(CallbackInfo ci) {
		OpenToLanScreen me = (OpenToLanScreen) (Object) this;

		// Add our own widgets
		// Toggle button for onlineMode
		this.onlineModeButton = 
				new ButtonWidget(me.width / 2 - 155, 124, 150, 20, getOnlineButtonText(),
				(p_214315_1_) -> this.onlineMode = !this.onlineMode) {
			@Override
			public Text getMessage() {
				return getOnlineButtonText();
			}
		};
		this.addButton(this.onlineModeButton);

		// Text field for port
		this.tfwPort = new TextFieldWidget(this.textRenderer, me.width / 2 - 154, me.height - 54, 147, 20, new TranslatableText(portLangKey));
		this.tfwPort.setText("25565");
		// Check the format, make sure the text is a valid integer
		this.tfwPort.setChangedListener((text)->this.tfwPort.setEditableColor(validatePort(text) >= 0 ? 0xFFFFFF : 0xFF0000));
		this.addChild(tfwPort);
	}

	@Override
	public void tick() {
		this.tfwPort.tick();
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		OpenToLanScreen me = (OpenToLanScreen) (Object) this;

		this.drawTextWithShadow(matrixStack, this.textRenderer, this.tfwPort.getMessage(), this.width / 2 - 155, this.height - 66, 10526880);
		this.tfwPort.render(matrixStack, mouseX, mouseY, partialTicks);

		if (this.onlineModeButton.isMouseOver(mouseX, mouseY)) // ????
			me.renderTooltip(matrixStack, onlinemodeDescTooltip, mouseX, mouseY);
	}

	/**
	 * @param text
	 * @return negative if port is invalid, otherwise the port number
	 */
	@Unique
	private static int validatePort(String text) {
		boolean valid = true;
		int port = -1;
		try {
			if (text.length() > 0) {
				port = Integer.parseInt(text);
				if (port < 0 || port > 65535)
					valid = false;
			}
		} catch (NumberFormatException e) {
			valid = false;
		}

		return valid ? port : -1;
	}

	@Unique
	private Text getOnlineButtonText() {
		return new LiteralText(I18n.translate(onlineModeLangKey) + ": "
				+ I18n.translate(onlineMode ? "options.on" : "options.off"));
	}
}
