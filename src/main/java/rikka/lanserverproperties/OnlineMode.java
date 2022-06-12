package rikka.lanserverproperties;

import net.minecraft.network.chat.TranslatableComponent;

public enum OnlineMode {
	online_onlineUUIDOnly(true, false, "on"),
	offline_tryOnlineUUIDFirst(false, true, "off.vanilla"),
	offline_offlineUUIDOnly(false, false, "off.fixed");

	private final static String translationKey = "lanserverproperties.options.online_mode";
	public final static TranslatableComponent translation = new TranslatableComponent(translationKey);

	public final boolean onlineModeEnabled, tryOnlineUUIDFirst;
	public final TranslatableComponent stateName, tooltip;

	private OnlineMode(boolean onlineModeEnabled, boolean tryOnlineUUIDFirst, String key) {
		this.onlineModeEnabled = onlineModeEnabled;
		this.tryOnlineUUIDFirst = tryOnlineUUIDFirst;
		this.stateName = new TranslatableComponent(translationKey + "." + key);
		this.tooltip = new TranslatableComponent(translationKey + "." + key + ".message");
	}

	public static OnlineMode of(boolean onlineModeEnabled, boolean tryOnlineUUIDFirst) {
		if (onlineModeEnabled) {
			return online_onlineUUIDOnly;
		} else {
			return tryOnlineUUIDFirst ? offline_tryOnlineUUIDFirst : offline_offlineUUIDOnly;
		}
	}
}
