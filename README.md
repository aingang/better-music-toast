# Better Music Toast

A mod for Fabric, NeoForge and Forge that shows which song is currently playing, in a small box that fits the vanilla HUD.

You can choose where it shows up, how long it stays, and it moves out of the way when advancements or other notifications pop up.

## Requirements

- **Fabric:** Minecraft 1.16 – 1.16.5 and 1.18 – 26.3 with Fabric Loader and [Fabric API](https://modrinth.com/mod/fabric-api),
  [Mod Menu](https://modrinth.com/mod/modmenu) optional
- **NeoForge:** Minecraft 1.20.1, 1.20.6 and 1.21 – 26.3
- **Forge:** Minecraft 1.8.9, 1.16.1 – 1.16.5, 1.18 – 1.19.4 and 1.20.1 (on 1.16.1 Forge 32.0.72 or newer,
  on 1.18.2 Forge 40.1.60 or newer)

One jar per loader and Minecraft version range.

Client-side only, not needed on servers.

## Settings

Options → Music & Sounds → **Better Music Toast**, or through Mod Menu (Fabric) or the Mods list (NeoForge, Forge).
Settings are saved in `config/bettermusictoast.json`.

## Building

The project uses [Stonecutter](https://stonecutter.kikugie.dev/) to build one jar per mod loader and Minecraft version
from a single codebase: Fabric versions use `build.gradle.kts`, NeoForge versions (`<version>-neoforge`) use
`build.neoforge.gradle.kts`, and the code picks the loader with `//? if fabric` / `//? if neoforge`.
Versions, supported Minecraft ranges and the mod version are set in `stonecutter.properties.toml`.

```
gradlew buildAndCollect
```

All jars end up in `build/libs/<mod version>/`.

Minecraft 1.8.9 (Forge) is a separate Gradle build in `legacy/forge-1.8.9`, because almost none of the modern code
fits 1.8.9 and its tooling ([Essential's Loom fork](https://github.com/EssentialGG/architectury-loom)) needs Gradle 8
running on Java 17 or 21. It shares the mod version, the texts and the pictures with the main project:

```
cd legacy/forge-1.8.9
gradlew build
```

Forge before 1.17 cannot be set up by ModDevGradle. For Forge 1.16.x the Stonecutter project only writes the
sources (`gradlew :1.16.5-forge:stonecutterGenerate`), and the Gradle 8 build in `legacy/forge-1.16`
(Essential's Loom fork) compiles them, one version per run:

```
cd legacy/forge-1.16
gradlew build "-Pmc=1.16.5"
```

The 1.16 jars target Java 8 like Minecraft 1.16 itself; the code is compiled with JDK 17 and
[Jabel](https://github.com/bsideup/jabel) turns the newer syntax into Java 8 bytecode.

Releases are uploaded to Modrinth with [mod-publish-plugin](https://github.com/modmuss50/mod-publish-plugin), using
the text in `RELEASE_NOTES.md` as changelog. The access token is read from `modrinthToken` in
`~/.gradle/gradle.properties`, outside the project.

```
gradlew publishMods -PdryRun
gradlew publishMods
```

## License

All rights reserved, see [LICENSE](LICENSE). Modpacks may include the mod without asking. Versions
before 1.6.7 were released under the MIT License.

Minecraft only added song names, the music notes icon and the Music Frequency option in 1.21.6. The jars
for older versions bring these themselves: the song names and option texts as Mojang wrote them, and the
icon taken from Minecraft 1.21.6 (© Mojang). Music Frequency works and is saved exactly like in 1.21.6+,
and like in 1.21.6+ the music keeps playing while the game is paused. On 1.8.9, Music Frequency is saved in
`config/bettermusictoast.json`, because Minecraft 1.8.9 removes unknown entries from `options.txt`.
