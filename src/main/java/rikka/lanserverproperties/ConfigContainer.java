package rikka.lanserverproperties;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;

public abstract class ConfigContainer {
	public Preferences preferences = Preferences.read();

	public boolean pvpAllowed;
	public OnlineMode onlineMode;
	public int maxPlayer;
	public String playersAlwaysOffline;

	protected abstract void setGameType(GameType gameType);
	protected abstract void setCommandEnabled(boolean commandEnabled);
	protected abstract void setGuiPort(int port);

	protected abstract GameType getGuiGameType();
	protected abstract boolean getGuiCommandEnabled();
	protected abstract int getGuiPort();

	public void loadFromCurrentServer(IntegratedServer server) {
		this.setGameType(server.getForcedGameType());
		this.setCommandEnabled(server.getPlayerList().isAllowCheatsForAllPlayers());
		this.setGuiPort(server.getPort());

		this.onlineMode = OnlineMode.of(server.usesAuthentication(), UUIDFixer.tryOnlineFirst);
		this.pvpAllowed = server.isPvpAllowed();
		this.maxPlayer = server.getMaxPlayers();
		this.playersAlwaysOffline = Preferences.getAlwaysOfflineString(UUIDFixer.alwaysOfflinePlayers);
	}

	public void applyToCurrentServer(IntegratedServer server) {
		server.setDefaultGameType(this.getGuiGameType());
		server.getPlayerList().setAllowCheatsForAllPlayers(this.getGuiCommandEnabled());
		// Cannot change port once the server is started

		server.setUsesAuthentication(this.onlineMode.onlineModeEnabled);
		server.setPvpAllowed(this.pvpAllowed);
		UUIDFixer.tryOnlineFirst = this.onlineMode.tryOnlineUUIDFirst;
		UUIDFixer.alwaysOfflinePlayers = Preferences.listOfAlwaysOffline(this.playersAlwaysOffline);
		LanServerProperties.setMaxPlayers(server, this.maxPlayer);
	}

	public void loadFromPreferences(boolean forceLoad) {
		preferences = Preferences.read();
		if (!forceLoad && !this.preferences.enablePreference) {
			boolean enablePreference = this.preferences.enablePreference;
			// Preference disabled, load default values except for enablePreference
			this.preferences = new Preferences();
			this.preferences.enablePreference = enablePreference;
		}

		this.setGameType(this.preferences.gameMode);
		this.setCommandEnabled(this.preferences.allowCheat);
		this.setGuiPort(this.preferences.defaultPort);

		this.onlineMode = OnlineMode.of(this.preferences.onlineMode, this.preferences.fixUUID);
		this.pvpAllowed = this.preferences.allowPVP;
		this.maxPlayer = this.preferences.maxPlayer;
		this.playersAlwaysOffline = Preferences.getAlwaysOfflineString(this.preferences.playersAlwaysOffline);
	}

	public void copyToPreferences() {
		this.preferences.gameMode = this.getGuiGameType();
		this.preferences.allowCheat = this.getGuiCommandEnabled();
		this.preferences.defaultPort = this.getGuiPort();

		this.preferences.onlineMode = this.onlineMode.onlineModeEnabled;
		this.preferences.fixUUID = this.onlineMode.tryOnlineUUIDFirst;
		this.preferences.allowPVP = this.pvpAllowed;
		this.preferences.maxPlayer = this.maxPlayer;
		this.preferences.playersAlwaysOffline = Preferences.listOfAlwaysOffline(this.playersAlwaysOffline);
	}

	public static class Modded extends ConfigContainer {
		private GameType gameMode;
		private boolean commands;
		private int listeningPort;

		@Override
		protected void setGameType(GameType gameType) {
			this.gameMode = gameType;
		}

		@Override
		protected void setCommandEnabled(boolean commandEnabled) {
			this.commands = commandEnabled;
		}

		@Override
		protected void setGuiPort(int port) {
			this.listeningPort = port;
		}

		@Override
		protected GameType getGuiGameType() {
			return this.gameMode;
		}

		@Override
		protected boolean getGuiCommandEnabled() {
			return this.commands;
		}

		@Override
		protected int getGuiPort() {
			return this.listeningPort;
		}
	}

	public static class Vanilla extends ConfigContainer {
		private final IShareToLanScreenParamAccessor stlParamAccessor;

		public Vanilla(IShareToLanScreenParamAccessor stlParamAccessor) {
			this.stlParamAccessor = stlParamAccessor;
		}

		@Override
		protected void setGameType(GameType gameType) {
			stlParamAccessor.setGameType(gameType);
		}

		@Override
		protected void setCommandEnabled(boolean commandEnabled) {
			stlParamAccessor.setCommandEnabled(commandEnabled);
		}

		@Override
		protected void setGuiPort(int port) {
			this.stlParamAccessor.setPort(port);
		}

		@Override
		protected GameType getGuiGameType() {
			return this.stlParamAccessor.getGameType();
		}

		@Override
		protected boolean getGuiCommandEnabled() {
			return this.stlParamAccessor.isCommandEnabled();
		}

		@Override
		protected int getGuiPort() {
			return this.stlParamAccessor.getPort();
		}
	}
}
