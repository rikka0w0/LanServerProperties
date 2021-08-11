package rikka.lanserverproperties;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.GameType;

public interface IShareToLanScreenParamAccessor {
	Screen getLastScreen();
	GameType getGameType();
	boolean isCommandEnabled();
	void setDefault(GameType gameType, boolean commandEnabled);
}
