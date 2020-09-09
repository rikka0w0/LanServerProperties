package rikka.lanserverproperties;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ShareToLanScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.FMLNetworkConstants;

@Mod(LanServerProperties.MODID)
public class LanServerProperties {
	public static final String MODID = "lanserverproperties";

	public static LanServerProperties INSTANCE = null;

	public LanServerProperties() {
		if (INSTANCE == null)
			INSTANCE = this;
		else
			throw new RuntimeException("Duplicated Class Instantiation: CustomItems");

		// Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, 
				() -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true)
				);

		// Register the GuiOpenEvent handler on client side only
		// If the mod is accidentally installed on a dedicated server then do nothing
		DistExecutor.safeRunWhenOn(Dist.CLIENT, ()->ClientHandler::registerGuiEventHandler);
	}

	private static class ClientHandler {
		public static void registerGuiEventHandler() {
			MinecraftForge.EVENT_BUS.addListener(ClientHandler::onGuiInit);
			MinecraftForge.EVENT_BUS.addListener(ClientHandler::onGuiDraw);
		}

		public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
			Screen gui = event.getGui();
			if (!(gui instanceof ShareToLanScreen))
				return;

			// Attempt to locate the old button
			Button button = null;
			String msg = I18n.format("lanServer.start");
			for (Widget widget: event.getWidgetList()) {
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

			// Replace the vanilla button click handler
			// Todo: replace this with a Mixin for better compatibility with other mods
			Field fieldOnPress = ObfuscationReflectionHelper.findField(Button.class, "field_230697_t_");
			Field modifiersField;
			try {
				modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(fieldOnPress, fieldOnPress.getModifiers() & ~Modifier.FINAL & ~Modifier.PRIVATE);
				modifiersField.setAccessible(false);
				
				fieldOnPress.set(button, new LanServerStartButtonHandler((ShareToLanScreen) gui));
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}

			FontRenderer textRenderer = ObfuscationReflectionHelper.getPrivateValue(Screen.class, gui, "field_230712_o_");
			OpenToLanScreenEx.init(gui, textRenderer, event.getWidgetList(), event::addWidget);
		}

		public static void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
			Screen gui = event.getGui();
			if (!(gui instanceof ShareToLanScreen))
				return;

			FontRenderer textRenderer = ObfuscationReflectionHelper.getPrivateValue(Screen.class, gui, "field_230712_o_");
			OpenToLanScreenEx.postDraw(gui, textRenderer, event.getMatrixStack(),
					event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
		}
	}

	/**
	 * Logic copied from vanilla {@link SahreToLanScreen}.
	 */
	private static class LanServerStartButtonHandler implements Button.IPressable {
		public final ShareToLanScreen owner;

		private LanServerStartButtonHandler(ShareToLanScreen owner) {
			this.owner = owner;
		}

		@Override
		public void onPress(Button theButton) {
			Minecraft mc = Minecraft.getInstance();
			mc.displayGuiScreen((Screen) null);

			int port = OpenToLanScreenEx.getServerPort(owner);
			String gameMode = ObfuscationReflectionHelper.getPrivateValue(ShareToLanScreen.class, owner, "field_146599_h");
			boolean allowCheats = ObfuscationReflectionHelper.getPrivateValue(ShareToLanScreen.class, owner, "field_146600_i");

			ITextComponent itextcomponent;
			if (mc.getIntegratedServer().shareToLAN(GameType.getByName(gameMode), allowCheats, port)) {
				OpenToLanScreenEx.onOpenToLanSuccess(owner);
				itextcomponent = new TranslationTextComponent("commands.publish.started", port);
			} else {
				itextcomponent = new TranslationTextComponent("commands.publish.failed");
			}

			mc.ingameGUI.getChatGUI().printChatMessage(itextcomponent);
			mc.func_230150_b_(); // Update window title
		}
	}
}
