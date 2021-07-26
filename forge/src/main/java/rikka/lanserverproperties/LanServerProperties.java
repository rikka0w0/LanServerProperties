package rikka.lanserverproperties;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;

@Mod(LanServerProperties.MODID)
public class LanServerProperties {
	public static final String MODID = "lanserverproperties";

	public static LanServerProperties INSTANCE = null;

	public LanServerProperties() {
		if (INSTANCE == null)
			INSTANCE = this;
		else
			throw new RuntimeException("Duplicated Class Instantiation: CustomItems");

		// Make sure servers without this Mod are not recognized as incompatible by the client
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
				() -> new IExtensionPoint.DisplayTest(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true)
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

			// Made public by access transformer, f_96547_, Screen.font
			OpenToLanScreenEx.init(gui, gui.font, event.getWidgetList(), event::addWidget);
		}

		public static void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
			Screen gui = event.getGui();
			if (!(gui instanceof ShareToLanScreen))
				return;

			OpenToLanScreenEx.postDraw(gui, gui.font, event.getMatrixStack(),
					event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
		}
	}
}
