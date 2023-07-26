package rikka.lanserverproperties;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.players.PlayerList;
import rikka.lanserverproperties.mixin.PlayerListAccessor;

public class LanServerProperties {
	public static void setMaxPlayers(IntegratedServer server, int num) {
		PlayerList playerList = server.getPlayerList();
		((PlayerListAccessor)playerList).setMaxPlayers(num);
	}
}
