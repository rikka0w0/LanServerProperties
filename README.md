# LAN Server Properties
For Minecraft 1.12–1.16, Forge and Fabric.

When this mod is installed, it enhances the vanilla Minecraft "Open to LAN" screen, which now also:
* Allows for a port customization
* Allows a user to disable the online mode, so that also unauthenticated players can join the LAN server.
* This mod is client only, installing on a dedicated server will not affact anything.
* Only the Lan server owner need to install this mod.
* Installing this mod does not prevent you from joining other vanilla or mod servers.

##Dependencies
### Forge Version
You need to install Forge and then install this mod.
### 
You need to install Fabric Loader and then install this mod. Fabric API is optional.

## For developers
To modify and debug the code, first import the repo as a Gradle project in Eclipse IDE, and then run the gradle task `genEclipseRuns`.
Most design work should happen in Fabric using yarn mapping. However, after remapping (see the following section), the forge folder can be imported to IDE as well.
If the forge part cannot be imported into Eclipse, please copy `gradlew`, `gradlew.bat` and `gradle` folder into `forge` folder and retry.

Windows users need to replace `./` and `../` with `.\` and `..\` respectively.

### Compile Fabric artifact
```
git clone git@github.com:rikka0w0/yarnforge-plugin.git
cd LanServerProperties
./gradlew build
```

### Compile Forge artifact
```
git clone git@github.com:rikka0w0/yarnforge-plugin.git
cd LanServerProperties
git submodule update --init

# Create vanilla-to-MCP tinyV2 mapping for Fabric Loom
pushd forge
# This will fail with `java.nio.file.NoSuchFileException`, it is normal.
# The mappings and mc-version should match gradle.properties.
../gradlew userRemapYarn --mappings net.fabricmc:yarn:1.16.2+build.47 --mc-version 1.16.2 --no-daemon --stacktrace --debug
../gradlew packMapping
popd

# Fabric Loom migrateMappings
./gradlew migrateMappings --mappings rikka:obf2mcp --input src/main/java --output forge/src/main/java
# Mapping to MCP is done

# Forge gradle build
pushd forge
../gradlew build
popd
```