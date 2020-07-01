package rikka.lanserverproperties;

import java.io.IOException;
import java.net.InetAddress;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class ShareToLanScreen2 extends GuiShareToLan {
	public final static String onlineModeLangKey = "lanserverproperties.gui.online_mode";
	public final static String onlinemodeDescLangKey = "lanserverproperties.gui.online_mode_desc";
	public final static String portLangKey = "lanserverproperties.gui.port";
	public final static Consumer<GuiOpenEvent> guiOpenEventHandler = (e)-> {
		GuiScreen guiToBeOpened = e.getGui();
		if (guiToBeOpened instanceof GuiShareToLan) {
/*
			Screen lastScreen = ObfuscationReflectionHelper.getPrivateValue(
					ShareToLanScreen.class, 
					(ShareToLanScreen) guiToBeOpened, 
					"field_146598_a");
*/
			GuiScreen lastScreen = Minecraft.getMinecraft().currentScreen;
			e.setGui(new ShareToLanScreen2(lastScreen));
		}
	};
	
	protected final GuiScreen lastScreen;
	protected GuiTextField tfwPort = null;
	protected GuiButton onlineModeButton = null;
	protected boolean onlineMode = true;

	public ShareToLanScreen2(GuiScreen lastScreenIn) {
		super(lastScreenIn);
		this.lastScreen = lastScreenIn;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 101) {
			serverStartButtonClick(button);
		} else if (button.id == 233) {
			this.onlineMode = !this.onlineMode;
			button.displayString = this.getOnlineButtonText();
		} else { 
			super.actionPerformed(button);
		}
	}
	
	private void serverStartButtonClick(GuiButton button) throws IOException {
		this.mc.displayGuiScreen(null);

		String portStr = tfwPort.getText();
		int port = portStr.length() > 0 ? Integer.parseInt(portStr) : 25565;
		String gameMode = ObfuscationReflectionHelper.getPrivateValue(GuiShareToLan.class, this, "field_146599_h");
		boolean allowCheats = ObfuscationReflectionHelper.getPrivateValue(GuiShareToLan.class, this, "field_146600_i");

		ITextComponent itextcomponent;
		String newPort = this.mc.getIntegratedServer().shareToLAN(GameType.getByName(gameMode), allowCheats);
		if (newPort != null) {
			this.mc.getIntegratedServer().getNetworkSystem().addLanEndpoint((InetAddress)null, port);
			itextcomponent = new TextComponentTranslation("commands.publish.started", newPort + ", " + port);
			this.mc.getIntegratedServer().setOnlineMode(onlineMode);
		} else {
			itextcomponent = new TextComponentString("commands.publish.failed");
		}
		this.mc.ingameGUI.getChatGUI().printChatMessage(itextcomponent);
	}
	
	@Override
	public void initGui() {
		super.initGui();

		// Attempt to locate the old button
		GuiButton button = null;
		String msg = I18n.format("lanServer.start");
		for (GuiButton widget: this.buttonList) {
			if (widget.displayString.equals(msg)) {
				button = widget;
				break;
			}
		}

		if (button == null) {
			System.out.println("[LSP] Unable to locate start server button!");
			// If we cannot find the "start lan server" button
			// just leave everything else there
			return;
		}

		// Add our own widgets
		// Toggle button for onlineMode
		this.onlineModeButton = 
				new GuiButton(233, this.width / 2 - 155, 124, 150, 20, getOnlineButtonText());
		this.addButton(this.onlineModeButton);

		// Text field for port
		this.tfwPort = new GuiTextField(234, this.fontRenderer, this.width / 2 - 154, this.height - 54, 147, 20);
		this.tfwPort.setText("25565");
	}
	
//	@Override
//	public void tick() {
//		this.tfwPort.tick();
//	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (this.tfwPort.textboxKeyTyped(typedChar, keyCode)) {
			// Check the format, make sure the text is a valid integer
			this.tfwPort.setTextColor(validatePort(this.tfwPort.getText()) >= 0 ? 0xFFFFFF : 0xFF0000);
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.tfwPort.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		this.drawString(this.fontRenderer, I18n.format(portLangKey), this.width / 2 - 154, this.height - 66, 10526880);
		this.tfwPort.drawTextBox();

		if (this.onlineModeButton.isMouseOver())
			this.drawHoveringText(I18n.format(onlinemodeDescLangKey), mouseX, mouseY);
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
	
	private String getOnlineButtonText() {
		return I18n.format(ShareToLanScreen2.onlineModeLangKey) + ": "
				+ I18n.format(ShareToLanScreen2.this.onlineMode ? "options.on" : "options.off");
	}
}
