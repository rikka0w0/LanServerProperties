package rikka.lanserverproperties;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

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

	// LSP Configs
	public boolean onlineMode = true;
	public boolean fixUUID = true;
	public boolean allowPVP = true;
	public int defaultPort = 25565;
	public int maxPlayer = 8;

	// TODO:
	public boolean enableCustomUUIDMap = false;
	public HashMap<String, String> customUUIDMap = new HashMap<>();

	@SuppressWarnings("resource")
	public static Path getConfigFolder() {
		return Minecraft.getInstance().gameDirectory.toPath().resolve("config");
	}

	public static Path getFileName() {
		return getConfigFolder().resolve(preferenceFileName);
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
