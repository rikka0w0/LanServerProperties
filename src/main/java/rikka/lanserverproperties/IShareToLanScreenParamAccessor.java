package rikka.lanserverproperties;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.GameType;

public interface IShareToLanScreenParamAccessor {
	Screen getLastScreen();
	GameType getGameType();
	boolean isCommandEnabled();
	void setGameType(GameType gameType);
	void setCommandEnabled(boolean commandEnabled);
	void setPort(int port);
	int getPort();
	void movePortEditBox(int x, int y, int width, int height);
}
