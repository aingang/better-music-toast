# Better Music Toast

A Fabric mod that shows which song is currently playing, in a small box that fits the vanilla HUD.

You can choose where it shows up, how long it stays, and it moves out of the way when advancements or other notifications pop up.

## Requirements

- Minecraft 26.2 – 26.3 with Fabric Loader
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
