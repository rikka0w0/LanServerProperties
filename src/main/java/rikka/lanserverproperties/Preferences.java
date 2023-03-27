package rikka.lanserverproperties;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;

// Format: <option name> = <default value>;
public class Preferences {
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final String preferenceFileName = "lsp.json";

	public boolean enablePreference = false;

	// Vanilla Configs
	public GameType gameMode = GameType.SURVIVAL;
	public boolean allowCheat = false;
	public int defaultPort = 25565;

	// LSP Configs
	public boolean onlineMode = true;
	public boolean fixUUID = true;
	public boolean allowPVP = true;
	public int maxPlayer = 8;

	public List<String> playersAlwaysOffline = new LinkedList<>();

	@SuppressWarnings("resource")
	public static Path getConfigFolder() {
		return Minecraft.getInstance().gameDirectory.toPath().resolve("config");
	}

	public static Path getFileName() {
		return getConfigFolder().resolve(preferenceFileName);
	}

	public static String getAlwaysOfflineString(List<String> playerList) {
		Iterator<String> iterator = playerList.iterator();

		String result = "";
		if (iterator.hasNext()) {
			// The first element
			result = iterator.next();
		} else {
			return result;
		}

		// Append remaining player names to the string
		while (iterator.hasNext()) {
			result += " " + iterator.next();
		}

		return result;
	}

	public static List<String> listOfAlwaysOffline(String alwaysOfflines) {
		return Arrays.stream(alwaysOfflines.split(" ")).toList();
	}

	public boolean save() {
		try {
			Files.createDirectories(getConfigFolder());
			Writer writer = Files.newBufferedWriter(getFileName());
			gson.toJson(this, writer);
			writer.flush();
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static Preferences read() {
		Preferences ret = null;
		try {
			Reader reader = Files.newBufferedReader(getFileName());
			ret = gson.fromJson(reader, Preferences.class);
			reader.close();
		} catch (NoSuchFileException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret == null ? new Preferences() : ret;
	}
}
