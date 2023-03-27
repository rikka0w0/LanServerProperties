package rikka.lanserverproperties;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class UUIDFixer {
	public static boolean tryOnlineFirst = false;
	public static List<String> alwaysOfflinePlayers = Collections.emptyList();

	/**
	 *  Mixin/ Coremod callback
	 */
	public static UUID hookEntry(String playerName) {
		if (alwaysOfflinePlayers.contains(playerName))
			return null;

		if (tryOnlineFirst)
			return getOfficialUUID(playerName);

		return null;
	}

	@Nullable
	public static UUID getOfficialUUID(String playerName) {
		String url = "https://api.mojang.com/users/profiles/minecraft/" + playerName;
		try {
			String UUIDJson = IOUtils.toString(new URL(url), Charset.defaultCharset());
			if (!UUIDJson.isEmpty()) {
				JsonObject root = JsonParser.parseString(UUIDJson).getAsJsonObject();
				String playerName2 = root.getAsJsonPrimitive("name").getAsString();
				String uuidString = root.getAsJsonPrimitive("id").getAsString();
				// com.mojang.util.UUIDTypeAdapter.fromString(String)
				long uuidMSB = Long.parseLong(uuidString.substring(0, 8), 16);
				uuidMSB <<= 32;
				uuidMSB |= Long.parseLong(uuidString.substring(8, 16), 16);
				long uuidLSB = Long.parseLong(uuidString.substring(16, 24), 16);
				uuidLSB <<= 32;
				uuidLSB |= Long.parseLong(uuidString.substring(24, 32), 16);
				UUID uuid = new UUID(uuidMSB, uuidLSB);

				if (playerName2.equalsIgnoreCase(playerName))
					return uuid;
			}
		} catch (IOException | JsonSyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}
}
