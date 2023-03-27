package rikka.lanserverproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

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
				() -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true)
				);

		// Register the GuiOpenEvent handler on client side only
		// If the mod is accidentally installed on a dedicated server then do nothing
		DistExecutor.safeRunWhenOn(Dist.CLIENT, ()->ClientHandler::registerGuiEventHandler);
	}

	private static class ForgeSTLParamAccessor implements IShareToLanScreenParamAccessor {
		private final ShareToLanScreen gui;
		private ForgeSTLParamAccessor(ShareToLanScreen gui) {
			this.gui = gui;
		}

		@Override
		public Screen getLastScreen() {
			return gui.lastScreen;
		}

		@Override
		public GameType getGameType() {
			return gui.gameMode;
		}

		@Override
		public boolean isCommandEnabled() {
			return gui.commands;
		}

		@Override
		public void setDefault(GameType gameType, boolean commandEnabled, int port) {
			gui.gameMode = gameType;
			gui.commands = commandEnabled;
			gui.port = port;
		}

		@Override
		public void setMaxPlayer(int num) {
			PlayerList playerList = Minecraft.getInstance().getSingleplayerServer().getPlayerList();
			playerList.maxPlayers = num;
		}

		@Override
		public int getPort() {
			return gui.port;
		}

		@Override
		public void movePortEditBox(int x, int y, int width, int height) {
			gui.portEdit.setPosition(x, y);
			gui.portEdit.setWidth(width);
			gui.portEdit.setHeight(height);
		}

		@Override
		public void setPortEditBoxReadonly(String value) {
			gui.portEdit.setEditable(false);
			gui.portEdit.setValue(value);
		}
	}

	private static class ClientHandler {
		public static void registerGuiEventHandler() {
			MinecraftForge.EVENT_BUS.addListener(ClientHandler::onGuiPostInit);
			MinecraftForge.EVENT_BUS.addListener(ClientHandler::onGuiDraw);
		}

		public static void onGuiPostInit(ScreenEvent.Init.Post event) {
			Screen gui = event.getScreen();
			if (gui instanceof ShareToLanScreen) {
				OpenToLanScreenEx hook = getLSPData((ShareToLanScreen) gui);
				// Made public by access transformer, f_96547_, Screen.font
				hook.postInitShareToLanScreen(gui.font, event.getListenersList(), event::addListener, event::removeListener);
			} else if (gui instanceof PauseScreen) {
				OpenToLanScreenEx.initPauseScreen(gui, event.getListenersList(), event::addListener);
			}
		}

		public static void onGuiDraw(ScreenEvent.Render.Post event) {
			Screen gui = event.getScreen();
			if (!(gui instanceof ShareToLanScreen))
				return;

			OpenToLanScreenEx.postDraw(gui, gui.font, event.getPoseStack(),
					event.getMouseX(), event.getMouseY(), event.getPartialTick());
		}
	}

	/**
	 *  Coremod callback
	 */
	public static OpenToLanScreenEx attachLSPData(ShareToLanScreen screen) {
		return new OpenToLanScreenEx(screen, new ForgeSTLParamAccessor(screen));
	}

	/**
	 *  Populated by Forge coremod
	 */
	private static OpenToLanScreenEx getLSPData(ShareToLanScreen screen) {
		throw new RuntimeException("Coremod implementation failed!");
	}
}
