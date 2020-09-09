package rikka.lanserverproperties;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ShareToLanScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@OnlyIn(Dist.CLIENT)
public class ShareToLanScreen2 extends ShareToLanScreen {
	public final static String onlineModeLangKey = "lanserverproperties.gui.online_mode";
	public final static String onlinemodeDescLangKey = "lanserverproperties.gui.online_mode_desc";
	public final static String portLangKey = "lanserverproperties.gui.port";
	public final static ImmutableList<ITextComponent> onlinemodeDescTooltip = ImmutableList.of(new TranslationTextComponent(onlinemodeDescLangKey));

	public static void guiOpenEventHandler(GuiOpenEvent e) {
		Screen guiToBeOpened = e.getGui();
		if (guiToBeOpened instanceof ShareToLanScreen) {
/*
			Screen lastScreen = ObfuscationReflectionHelper.getPrivateValue(
					ShareToLanScreen.class, 
					(ShareToLanScreen) guiToBeOpened, 
					"field_146598_a");
*/
			Screen lastScreen = Minecraft.getInstance().currentScreen;
			e.setGui(new ShareToLanScreen2(lastScreen));
		}
	};
	
	protected final Screen lastScreen;
	protected TextFieldWidget tfwPort = null;
	protected Button onlineModeButton = null;
	protected boolean onlineMode = true;
	
	public ShareToLanScreen2(Screen lastScreenIn) {
		super(lastScreenIn);
		this.lastScreen = lastScreenIn;
	}
	
	Button.IPressable startServerButtonPressed = (button) -> {
		Minecraft mc = this.field_230706_i_;
		mc.displayGuiScreen((Screen) null);

		String portStr = tfwPort.getText();
		int port = portStr.length() > 0 ? Integer.parseInt(portStr) : 25565;
		String gameMode = ObfuscationReflectionHelper.getPrivateValue(ShareToLanScreen.class, ShareToLanScreen2.this, "field_146599_h");
		boolean allowCheats = ObfuscationReflectionHelper.getPrivateValue(ShareToLanScreen.class, ShareToLanScreen2.this, "field_146600_i");

		ITextComponent itextcomponent;
		if (mc.getIntegratedServer().shareToLAN(GameType.getByName(gameMode), allowCheats, port)) {
			mc.getIntegratedServer().setOnlineMode(onlineMode);
			mc.getIntegratedServer().getMaxPlayers();
			itextcomponent = new TranslationTextComponent("commands.publish.started", port);
		} else {
			itextcomponent = new TranslationTextComponent("commands.publish.failed");
		}

		mc.ingameGUI.getChatGUI().printChatMessage(itextcomponent);
		mc.func_230150_b_(); // Update window title
	};

	@Override
	protected void func_231160_c_() {
		super.func_231160_c_();

		// Attempt to locate the old button
		Button button = null;
		String msg = I18n.format("lanServer.start");
		for (Widget widget: this.field_230710_m_) {
			if (widget instanceof Button && widget.func_230458_i_().getString().equals(msg)) {
				button = (Button) widget;
				break;
			}
		}
		
		if (button == null) {
			System.out.println("[LSP] Unable to locate start server button!");
			// If we cannot find the "start lan server" button
			// just leave everything else there
			return;
		}

		Field fieldOnPress = ObfuscationReflectionHelper.findField(Button.class, "field_230697_t_");
		Field modifiersField;
		try {
			modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(fieldOnPress, fieldOnPress.getModifiers() & ~Modifier.FINAL & ~Modifier.PRIVATE);
			modifiersField.setAccessible(false);
			
			fieldOnPress.set(button, startServerButtonPressed);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		// Add our own widgets
		// Toggle button for onlineMode
		this.onlineModeButton = 
				new Button(this.field_230708_k_ / 2 - 155, 124, 150, 20, this.getOnlineButtonText(),
				(p_214315_1_) -> this.onlineMode = !this.onlineMode) {
			@Override
			public ITextComponent func_230458_i_() {
				return ShareToLanScreen2.this.getOnlineButtonText();
			}
		};
		this.func_230480_a_(this.onlineModeButton);

		// Text field for port
		this.tfwPort = new TextFieldWidget(this.field_230712_o_, this.field_230708_k_ / 2 - 154, this.field_230709_l_ - 54, 147, 20, new TranslationTextComponent(portLangKey));
		this.tfwPort.setText("25565");
		// Check the format, make sure the text is a valid integer
		this.tfwPort.setResponder((text)->this.tfwPort.setTextColor(validatePort(text) >= 0 ? 0xFFFFFF : 0xFF0000));
		this.field_230705_e_.add(tfwPort);
	}

	@Override
	public void func_231023_e_() {
		this.tfwPort.tick();
	}
	
	@Override
	public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);

		this.func_238471_a_(matrixStack, this.field_230712_o_, this.tfwPort.func_230458_i_().getString(), this.field_230708_k_ / 2 - 135, this.field_230709_l_ - 66, 10526880);
		this.tfwPort.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);

		if (this.onlineModeButton.func_230449_g_()) // ????
			this.func_238654_b_(matrixStack, onlinemodeDescTooltip, mouseX, mouseY);
	}
	
	/**
	 * @param text
	 * @return negative if port is invalid, otherwise the port number
	 */
	public static int validatePort(String text) {
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

	private ITextComponent getOnlineButtonText() {
		return new StringTextComponent(I18n.format(ShareToLanScreen2.onlineModeLangKey) + ": "
				+ I18n.format(ShareToLanScreen2.this.onlineMode ? "options.on" : "options.off"));
	}
}
