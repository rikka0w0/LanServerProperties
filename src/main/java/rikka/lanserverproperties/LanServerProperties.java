package rikka.lanserverproperties;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
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

	public static class ClientHandler {
		public static void registerGuiEventHandler() {
			MinecraftForge.EVENT_BUS.addListener(ShareToLanScreen2::guiOpenEventHandler);
		}
	}
	
/*
	@Mod.EventBusSubscriber(modid = LanServerProperties.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ForgeEventHandler {
		@SubscribeEvent
		public static void onServerStarting(FMLServerStartingEvent e) {
			MinecraftServer server = e.getServer();
			Path path = Paths.get(FMLPaths.CONFIGDIR.get().toString(), "server.properties");
			ServerPropertiesProvider serverpropertiesprovider = new ServerPropertiesProvider(path);
			if (!Files.exists(path))
				serverpropertiesprovider.save();

			ServerProperties props = serverpropertiesprovider.getProperties();
			server.setOnlineMode(props.onlineMode);
			server.setServerPort(props.serverPort);
		}
	}
*/
}
