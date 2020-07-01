package rikka.lanserverproperties;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = LanServerProperties.MODID, clientSideOnly = true)
public class LanServerProperties {
	public static final String MODID = "lanserverproperties";

	public static LanServerProperties INSTANCE = null;

	public LanServerProperties() {
		if (INSTANCE == null)
			INSTANCE = this;
		else
			throw new RuntimeException("Duplicated Class Instantiation: CustomItems");
		
		// Register the GuiOpenEvent handler on client side only
		// If the mod is accidentally installed on a dedicated server then do nothing
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void guiOpenEventHandler(GuiOpenEvent event) {
		ShareToLanScreen2.guiOpenEventHandler.accept(event);
	}
}
