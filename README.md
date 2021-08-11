# LAN Server Properties
For Minecraft 1.12.2–1.7.1, Forge and Fabric.

When this mod is installed, it enhances the vanilla Minecraft "Open to LAN" screen, which now also:
* Allows for a port customization.
* Allows a user to disable PvP.
* Allows a user to disable the online mode, so that also unauthenticated players can join the LAN server.
* Allows editing server configuration from the same "Open to LAN" button after the server is published.

## Note
* This mod is client-only, installing on a dedicated server will not affact anything.
* Only the Lan server owner need to install this mod.
* Installing this mod does not prevent you from joining other vanilla or mod servers.

## Dependencies
### Forge Version
You need to install Forge and then install this mod.

### Fabric Version
You need to install Fabric Loader and then install this mod. Fabric API is optional.

## For developers
To modify and debug the code, first import the repo as a Gradle project in Eclipse IDE, and then run the gradle task `genEclipseRuns`.
Most design work should happen in Fabric using yarn mapping. However, after remapping (see the following section), the forge folder can be imported to IDE as well.
If the forge part cannot be imported into Eclipse, please copy `gradlew`, `gradlew.bat` and `gradle` folder into `forge` folder and retry.

Windows users need to replace `./` and `../` with `.\` and `..\` respectively.

Since 1.17.1, LSP for Fabric and Forge share common code as much as possible. The entire code base uses Minecraft official mapping.

### Compile Fabric artifact
```
git clone https://github.com/rikka0w0/LanServerProperties.git
cd LanServerProperties/fabric
./gradlew build
```

### Compile Forge artifact
```
git clone https://github.com/rikka0w0/LanServerProperties.git
cd LanServerProperties/forge
./gradlew build
```

### To specify JRE path (Since 1.17.1, Minecraft requires Java 16 or above):
```
./gradlew -Dorg.gradle.java.home=/path_to_jdk_directory <commands>
```
