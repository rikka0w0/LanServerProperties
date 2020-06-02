package rikka.lanserverproperties;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ShareToLanScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
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
	public final static Consumer<GuiOpenEvent> guiOpenEventHandler = (e)-> {
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
		this.minecraft.displayGuiScreen((Screen) null);

		String portStr = tfwPort.getText();
		int port = portStr.length() > 0 ? Integer.parseInt(portStr) : 25565;
		String gameMode = ObfuscationReflectionHelper.getPrivateValue(ShareToLanScreen.class, ShareToLanScreen2.this, "field_146599_h");
		boolean allowCheats = ObfuscationReflectionHelper.getPrivateValue(ShareToLanScreen.class, ShareToLanScreen2.this, "field_146600_i");

		ITextComponent itextcomponent;
		if (this.minecraft.getIntegratedServer().shareToLAN(GameType.getByName(gameMode), allowCheats, port)) {
			itextcomponent = new TranslationTextComponent("commands.publish.started", port);
		} else {
			itextcomponent = new TranslationTextComponent("commands.publish.failed");
		}
		
		this.minecraft.getIntegratedServer().setOnlineMode(onlineMode);
		this.minecraft.getIntegratedServer().getMaxPlayers();
		this.minecraft.ingameGUI.getChatGUI().printChatMessage(itextcomponent);
		this.minecraft.func_230150_b_();	
	};
	
	@Override
	protected void init() {
		super.init();

		// Attempt to locate the old button
		Button button = null;
		String msg = I18n.format("lanServer.start");
		for (Widget widget: this.buttons) {
			if (widget instanceof Button && widget.getMessage().equals(msg)) {
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

		Field fieldOnPress = ObfuscationReflectionHelper.findField(Button.class, "onPress");
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
				new Button(this.width / 2 - 155, 124, 150, 20, I18n.format(ShareToLanScreen2.onlineModeLangKey),
				(p_214315_1_) -> this.onlineMode = !this.onlineMode) {
			@Override
			public String getMessage() {
				return I18n.format(ShareToLanScreen2.onlineModeLangKey) + ": "
						+ I18n.format(ShareToLanScreen2.this.onlineMode ? "options.on" : "options.off");
			}
		};
		this.addButton(this.onlineModeButton);

		// Text field for port
		this.tfwPort = new TextFieldWidget(this.font, this.width / 2 - 154, this.height - 54, 147, 20, I18n.format(portLangKey));
		this.tfwPort.setText("25565");
		// Check the format, make sure the text is a valid integer
		this.tfwPort.setResponder((text)->this.tfwPort.setTextColor(validatePort(text) >= 0 ? 0xFFFFFF : 0xFF0000));
		this.children.add(tfwPort);
	}
	
	@Override
	public void tick() {
		this.tfwPort.tick();
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);

		this.drawString(this.font, I18n.format(portLangKey), this.width / 2 - 154, this.height - 66, 10526880);
		this.tfwPort.render(mouseX, mouseY, partialTicks);

		if (this.onlineModeButton.isHovered())
			this.renderTooltip(I18n.format(onlinemodeDescLangKey), mouseX, mouseY);
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
}
