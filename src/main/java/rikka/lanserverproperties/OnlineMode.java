package rikka.lanserverproperties;

import net.minecraft.network.chat.Component;

public enum OnlineMode {
	online_onlineUUIDOnly(true, false, "on"),
	offline_tryOnlineUUIDFirst(false, true, "off.fixed"),
	offline_offlineUUIDOnly(false, false, "off.vanilla");

	private final static String translationKey = "lanserverproperties.options.online_mode";
	public final static Component translation = Component.translatable(translationKey);

	public final boolean onlineModeEnabled, tryOnlineUUIDFirst;
	public final Component stateName, tooltip;

	private OnlineMode(boolean onlineModeEnabled, boolean tryOnlineUUIDFirst, String key) {
		this.onlineModeEnabled = onlineModeEnabled;
		this.tryOnlineUUIDFirst = tryOnlineUUIDFirst;
		this.stateName = Component.translatable(translationKey + "." + key);
		this.tooltip = Component.translatable(translationKey + "." + key + ".message");
	}

	public static OnlineMode of(boolean onlineModeEnabled, boolean tryOnlineUUIDFirst) {
		if (onlineModeEnabled) {
			return online_onlineUUIDOnly;
		} else {
			return tryOnlineUUIDFirst ? offline_tryOnlineUUIDFirst : offline_offlineUUIDOnly;
		}
	}
}
