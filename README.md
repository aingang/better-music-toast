# Better Music Toast

A Fabric mod that shows which song is currently playing, in a small box that fits the vanilla HUD.

You can choose where it shows up, how long it stays, and it moves out of the way when advancements or other notifications pop up.

## Requirements

- Minecraft 1.21.5 – 26.3 with Fabric Loader (one jar per version range)
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional)

Client-side only, not needed on servers.

## Settings

Options → Music & Sounds → **Better Music Toast**, or through Mod Menu.
Settings are saved in `config/bettermusictoast.json`.

## Building

The project uses [Stonecutter](https://stonecutter.kikugie.dev/) to build one jar per Minecraft version from a single codebase.
Versions, supported Minecraft ranges and the mod version are set in `stonecutter.properties.toml`.

```
gradlew buildAndCollect
```

All jars end up in `build/libs/<mod version>/`.

## License

MIT, see [LICENSE](LICENSE).

Minecraft only added song names, the music notes icon and the Music Frequency option in 1.21.6. The jars
for older versions bring these themselves: the song names and option texts as Mojang wrote them, and the
icon taken from Minecraft 1.21.6 (© Mojang). Music Frequency works and is saved exactly like in 1.21.6+,
and like in 1.21.6+ the music keeps playing while the game is paused.
